package se.frisk.edufyrecommendationsservice.services;

import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class RecommendationService {

    private final HistoryClient historyClient;
    private final LikesClient likesClient;

    public RecommendationService(HistoryClient historyClient, LikesClient likesClient) {
        this.historyClient = historyClient;
        this.likesClient = likesClient;
    }

    public Optional<String> pickNextMediaId(String userId, String currentMediaId) {

        List<String> candidates = historyClient.getCandidates(userId, currentMediaId, 50);
        Set<String> played = new HashSet<>(historyClient.getPlayedIds(userId));
        Set<String> disliked = new HashSet<>(likesClient.getDislikedIds(userId));
        Set<String> liked = new HashSet<>(likesClient.getLikedIds(userId));

        List<String> filtered = candidates.stream()
                .distinct()
                .filter(id -> !id.equalsIgnoreCase(currentMediaId))
                .filter(id -> !played.contains(id))
                .filter(id -> !disliked.contains(id))
                .toList();

        if(filtered.isEmpty()) return Optional.empty();

        List<String> sorted = new ArrayList<>(filtered);
        sorted.sort((a,b) -> Boolean.compare(liked.contains(b), liked.contains(a)));
        return Optional.of(sorted.get(0));
    }



    public List<String> toppTenRecommendation(String userId, int limit) {

        List<String> fromTopGenres = historyClient.getTopGeneres(userId);
        int mainCount = (int) Math.ceil(limit * 0.8);
        int otherCount = limit - mainCount;

        List<String> fromOtherGenres = historyClient.getCandidatesFromOtherGeneres(userId, otherCount * 2);


        Set<String> played = new HashSet<>(historyClient.getPlayedIds(userId));
        Set<String> disliked = new HashSet<>(likesClient.getDislikedIds(userId));
        Set<String> liked = new HashSet<>(likesClient.getLikedIds(userId));

        List<String> combined = new ArrayList<>();
        combined.addAll(fromTopGenres);
        combined.addAll(fromOtherGenres);

        List<String> filtered = combined.stream()
                .distinct()
                .filter(id -> !played.contains(id))
                .filter(id -> !disliked.contains(id))
                .toList();

        List<String> sorted = new ArrayList<>(filtered);
        sorted.sort((a,b) -> Boolean.compare(played.contains(b), played.contains(a)));
        return sorted.stream().limit(limit).toList();

    }

}
