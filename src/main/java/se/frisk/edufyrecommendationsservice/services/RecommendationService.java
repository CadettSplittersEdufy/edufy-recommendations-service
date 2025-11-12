package se.frisk.edufyrecommendationsservice.services;

import org.springframework.stereotype.Service;
import se.frisk.edufyrecommendationsservice.clients.*;
import se.frisk.edufyrecommendationsservice.dto.HistoryItem;
import se.frisk.edufyrecommendationsservice.dto.MediaType;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class RecommendationService {

    private final HistoryClient historyClient;
    private final RatingsClient ratingsClient;
    private final MusicClient musicClient;
    private final PodClient podClient;
    private final VideoClient videoClient;

    public RecommendationService(HistoryClient historyClient,
                                 RatingsClient ratingsClient,
                                 MusicClient musicClient,
                                 PodClient podClient,
                                 VideoClient videoClient) {
        this.historyClient = historyClient;
        this.ratingsClient = ratingsClient;
        this.musicClient = musicClient;
        this.podClient = podClient;
        this.videoClient = videoClient;
    }

    // ----------------------------------------------------
    // 1. pickNext – EN föreslagen media
    //    Samma urvalslogik som pickTop, men vi tar bara första
    //    som inte är samma som currentMediaId.
    // ----------------------------------------------------
    public String pickNext(String userId, String currentMediaId, MediaType mediaType) {
        // Ta fram t.ex. 10 rekommendationer med vår urvalslogik
        List<String> candidates = calculateRecommendations(userId, mediaType, 10);

        if (candidates.isEmpty()) {
            return "";
        }

        // Ta första som inte är samma som det som spelas nu
        return candidates.stream()
                .filter(id -> !Objects.equals(id, currentMediaId))
                .findFirst()
                .orElse("");
    }

    // ----------------------------------------------------
    // 2. pickTop – lista med top N rekommendationer
    //    Samma urvalslogik som pickNext, men vi returnerar
    //    hela listan (upp till limit).
    // ----------------------------------------------------
    public List<String> pickTop(String userId, int limit, MediaType mediaType) {
        if (limit <= 0) {
            return List.of();
        }
        return calculateRecommendations(userId, mediaType, limit);
    }

    // ----------------------------------------------------
    // 3. Gemensam urvalsprocess
    //
    //    Steg:
    //    1) Hämta historik (senaste ~50) och räkna top 3 kategorier.
    //    2) Hämta liked/disliked IDs.
    //    3) Hämta all media för aktuell typ (music/pod/video).
    //    4) Filtrera bort dislikade + nyligen spelade.
    //    5) Räkna top 3 liked-kategorier.
    //    6) Dela upp i:
    //          - historyItems (top 3 historik-kategorier)
    //          - likedItems   (top 3 liked-kategorier)
    //          - newItems     (övriga kategorier = "nya"/inte rekommenderade)
    //    7) Försök ta ~60% / 20% / 20% (history/liked/new).
    //    8) Fyll upp med rester om någon grupp tar slut.
    // ----------------------------------------------------
    private List<String> calculateRecommendations(String userId, MediaType mediaType, int limit) {

        // Lokal record för att hålla ihop id + kategori
        record Candidate(String id, String category) {}

        // ---------- 1. Hämta historik ----------
        var fullHistory = Optional.ofNullable(historyClient.getHistory(userId, mediaType))
                .orElse(List.of());

        int historyWindow = 50;
        List<HistoryItem> recentHistory = fullHistory.size() > historyWindow
                ? fullHistory.subList(fullHistory.size() - historyWindow, fullHistory.size())
                : fullHistory;

        // IDs som nyligen spelats (från historiken)
        Set<String> recentPlayedIds = recentHistory.stream()
                .map(HistoryItem::getItemId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        // ---------- 2. Likes & dislikes ----------
        Set<String> dislikedSet = new HashSet<>(
                Optional.ofNullable(ratingsClient.getDislikedIds(userId)).orElse(List.of())
        );

        Set<String> likedIdSet = new HashSet<>(
                Optional.ofNullable(ratingsClient.getLikedIds(userId)).orElse(List.of())
        );

        // ---------- 3. Bygg kandidater (id + kategori) + id->kategori-karta ----------
        List<Candidate> candidates = new ArrayList<>();
        Map<String, String> idToCategory = new HashMap<>();

        switch (mediaType) {
            case MUSIC -> musicClient.getAvailableMusic().forEach(m -> {
                if (m != null && m.getId() != null) {
                    String id = m.getId().toString();
                    String category = m.getCategory();
                    candidates.add(new Candidate(id, category));
                    idToCategory.put(id, category);
                }
            });
            case POD -> podClient.getAvailablePods().forEach(p -> {
                if (p != null && p.getId() != null) {
                    String id = p.getId().toString();
                    String category = p.getCategory();
                    candidates.add(new Candidate(id, category));
                    idToCategory.put(id, category);
                }
            });
            case VIDEO -> videoClient.getAllVideos().forEach(v -> {
                if (v != null && v.getId() != null
                        && (v.getAvailable() == null || Boolean.TRUE.equals(v.getAvailable()))) {
                    String id = v.getId().toString();
                    String category = v.getCategory();
                    candidates.add(new Candidate(id, category));
                    idToCategory.put(id, category);
                }
            });
        }

        if (candidates.isEmpty()) {
            return List.of();
        }

        // ---------- 4. Räkna historiska kategorier via idToCategory ----------
        Map<String, Long> historyCategoryCounts = recentHistory.stream()
                .map(HistoryItem::getItemId)       // ta id från historiken
                .map(idToCategory::get)            // slå upp kategori i katalogmapen
                .filter(Objects::nonNull)
                .collect(Collectors.groupingBy(c -> c, Collectors.counting()));

        List<String> topHistoryCategories = historyCategoryCounts.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(3)
                .map(Map.Entry::getKey)
                .toList();

        // ---------- 5. Filtrera bort dislikade + nyligen spelade ----------
        List<Candidate> filtered = candidates.stream()
                .filter(c -> !dislikedSet.contains(c.id()))
                .filter(c -> !recentPlayedIds.contains(c.id()))
                .toList();

        if (filtered.isEmpty()) {
            return List.of();
        }

        // ---------- 6. Top 3 liked-kategorier ----------
        Map<String, Long> likedCategoryCounts = filtered.stream()
                .filter(c -> likedIdSet.contains(c.id()))
                .map(Candidate::category)
                .filter(Objects::nonNull)
                .collect(Collectors.groupingBy(c -> c, Collectors.counting()));

        List<String> topLikedCategories = likedCategoryCounts.entrySet().stream()
                .filter(e -> !topHistoryCategories.contains(e.getKey())) // undvik att samma kategori hamnar i båda
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(3)
                .map(Map.Entry::getKey)
                .toList();

        // ---------- 7. Dela upp i grupper ----------
        List<Candidate> historyItems = new ArrayList<>();
        List<Candidate> likedItems = new ArrayList<>();
        List<Candidate> newCategoryItems = new ArrayList<>();

        for (Candidate c : filtered) {
            String cat = c.category();
            if (cat == null) {
                newCategoryItems.add(c);
            } else if (topHistoryCategories.contains(cat)) {
                historyItems.add(c);
            } else if (topLikedCategories.contains(cat)) {
                likedItems.add(c);
            } else {
                // Inte i history-top3, inte i liked-top3 = "nya"/inte rekommenderade kategorier
                newCategoryItems.add(c);
            }
        }

        Collections.shuffle(historyItems);
        Collections.shuffle(likedItems);
        Collections.shuffle(newCategoryItems);

        // ---------- 8. 60% / 20% / 20% ----------
        int historyQuota = (int) Math.round(limit * 0.6);
        int likedQuota   = (int) Math.round(limit * 0.2);
        int newQuota     = limit - historyQuota - likedQuota;

        List<String> result = new ArrayList<>(limit);

        // history
        int usedHistory = Math.min(historyQuota, historyItems.size());
        for (int i = 0; i < usedHistory; i++) {
            result.add(historyItems.get(i).id());
        }

        // liked
        int usedLiked = Math.min(likedQuota, likedItems.size());
        for (int i = 0; i < usedLiked; i++) {
            result.add(likedItems.get(i).id());
        }

        // nya
        int usedNew = Math.min(newQuota, newCategoryItems.size());
        for (int i = 0; i < usedNew; i++) {
            result.add(newCategoryItems.get(i).id());
        }

        // ---------- 9. Fyll upp om vi inte nått limit ----------
        if (result.size() < limit) {
            List<Candidate> leftovers = new ArrayList<>();
            leftovers.addAll(historyItems.subList(usedHistory, historyItems.size()));
            leftovers.addAll(likedItems.subList(usedLiked, likedItems.size()));
            leftovers.addAll(newCategoryItems.subList(usedNew, newCategoryItems.size()));

            Collections.shuffle(leftovers);

            int remaining = limit - result.size();
            int extra = Math.min(remaining, leftovers.size());
            for (int i = 0; i < extra; i++) {
                result.add(leftovers.get(i).id());
            }
        }

        return result;
    }
}
