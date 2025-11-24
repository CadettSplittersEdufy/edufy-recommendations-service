package se.frisk.edufyrecommendationsservice.services;

import org.springframework.stereotype.Service;
import se.frisk.edufyrecommendationsservice.clients.*;
import se.frisk.edufyrecommendationsservice.dto.HistoryItem;
import se.frisk.edufyrecommendationsservice.dto.MediaType;
import se.frisk.edufyrecommendationsservice.exceptions.DependencyUnavailableException;
import se.frisk.edufyrecommendationsservice.clients.UserClient;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class RecommendationService {

    private final HistoryClient historyClient;
    private final RatingsClient ratingsClient;
    private final MusicClient musicClient;
    private final PodClient podClient;
    private final VideoClient videoClient;
    private final UserClient userClient;

    public RecommendationService(HistoryClient historyClient,
                                 RatingsClient ratingsClient,
                                 MusicClient musicClient,
                                 PodClient podClient,
                                 VideoClient videoClient,
                                 UserClient userClient) {
        this.historyClient = historyClient;
        this.ratingsClient = ratingsClient;
        this.musicClient = musicClient;
        this.podClient = podClient;
        this.videoClient = videoClient;
        this.userClient = userClient;
    }

    public String pickNext(String userId, String currentMediaId, MediaType mediaType) {
        List<String> candidates = calculateRecommendations(userId, mediaType, 10);

        if (candidates.isEmpty()) {
            return "";
        }

        return candidates.stream()
                .filter(id -> !Objects.equals(id, currentMediaId))
                .findFirst()
                .orElse("");
    }

    public List<String> pickTop(String userId, int limit, MediaType mediaType) {
        if (limit <= 0) {
            throw new IllegalArgumentException("limit must be greater than 0");
        }
        return calculateRecommendations(userId, mediaType, limit);
    }

    private List<String> calculateRecommendations(String userId, MediaType mediaType, int limit) {

        record Candidate(String id, String category) {}

        List<HistoryItem> fullHistory;
        try {
            fullHistory = Optional.ofNullable(historyClient.getHistory(userId, mediaType))
                    .orElse(List.of());
        } catch (Exception e) {
            System.out.println("Failed to get history for user: " + userId + ": " + e.getMessage());
            fullHistory = List.of();
        }

        int historyWindow = 50;
        List<HistoryItem> recentHistory = fullHistory.size() > historyWindow
                ? fullHistory.subList(fullHistory.size() - historyWindow, fullHistory.size())
                : fullHistory;

        Set<String> recentPlayedIds = recentHistory.stream()
                .map(HistoryItem::getItemId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Set<String> dislikedSet;
        Set<String> likedIdSet;

        try {
            List<String> disliked = Optional.ofNullable(ratingsClient.getDislikedIds(userId)).orElse(List.of());
            List<String> liked = Optional.ofNullable(ratingsClient.getLikedIds(userId)).orElse(List.of());
            dislikedSet = new HashSet<>(disliked);
            likedIdSet = new HashSet<>(liked);
        } catch (Exception e) {
            System.out.println("Failed to get Ratings for user:  " + userId + ": " + e.getMessage());
            dislikedSet = Set.of();
            likedIdSet = Set.of();
        }

        final Set<String> finalDislikedSet = dislikedSet;
        final Set<String> finalLikedIdSet = likedIdSet;

        List<Candidate> candidates = new ArrayList<>();
        Map<String, String> idToCategory = new HashMap<>();

        try {
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
                case VIDEO -> videoClient.getAvailableVideos().forEach(v -> {
                    if (v != null && v.getId() != null) {
                        String id = v.getId().toString();
                        String category = v.getCategory();
                        candidates.add(new Candidate(id, category));
                        idToCategory.put(id, category);
                    }
                });
            }
        } catch (Exception e) {
            String msg;
            switch (mediaType) {
                case MUSIC -> msg = "No connection to Music – Song can not be selected.";
                case POD   -> msg = "No connection to Pod – Pod can not be selected .";
                case VIDEO -> msg = "No connection to Video – Video can not be selected .";
                default    -> msg = "No connection to Media – Media can not be selected.";
            }
            throw new DependencyUnavailableException(msg, e);
        }

        if (candidates.isEmpty()) {
            return List.of();
        }

        if (fullHistory.isEmpty() && likedIdSet.isEmpty() && dislikedSet.isEmpty()) {
            Collections.shuffle(candidates);
            return candidates.stream()
                    .map(Candidate::id)
                    .limit(limit)
                    .toList();
        }

        Map<String, Long> historyCategoryCounts = recentHistory.stream()
                .map(HistoryItem::getItemId)
                .map(idToCategory::get)
                .filter(Objects::nonNull)
                .collect(Collectors.groupingBy(c -> c, Collectors.counting()));

        List<String> topHistoryCategories = historyCategoryCounts.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(3)
                .map(Map.Entry::getKey)
                .toList();

        List<Candidate> filtered = candidates.stream()
                .filter(c -> !finalDislikedSet.contains(c.id()))
                .filter(c -> !recentPlayedIds.contains(c.id()))
                .toList();

        if (filtered.isEmpty()) {
            return List.of();
        }

        Map<String, Long> likedCategoryCounts = filtered.stream()
                .filter(c -> finalLikedIdSet.contains(c.id()))
                .map(Candidate::category)
                .filter(Objects::nonNull)
                .collect(Collectors.groupingBy(c -> c, Collectors.counting()));

        List<String> topLikedCategories = likedCategoryCounts.entrySet().stream()
                .filter(e -> !topHistoryCategories.contains(e.getKey()))
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(3)
                .map(Map.Entry::getKey)
                .toList();

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
                newCategoryItems.add(c);
            }
        }

        Collections.shuffle(historyItems);
        Collections.shuffle(likedItems);
        Collections.shuffle(newCategoryItems);

        int historyQuota = (int) Math.round(limit * 0.6);
        int likedQuota   = (int) Math.round(limit * 0.2);
        int newQuota     = limit - historyQuota - likedQuota;

        List<String> result = new ArrayList<>(limit);

        int usedHistory = Math.min(historyQuota, historyItems.size());
        for (int i = 0; i < usedHistory; i++) {
            result.add(historyItems.get(i).id());
        }

        int usedLiked = Math.min(likedQuota, likedItems.size());
        for (int i = 0; i < usedLiked; i++) {
            result.add(likedItems.get(i).id());
        }

        int usedNew = Math.min(newQuota, newCategoryItems.size());
        for (int i = 0; i < usedNew; i++) {
            result.add(newCategoryItems.get(i).id());
        }

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