package se.frisk.edufyrecommendationsservice.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import se.frisk.edufyrecommendationsservice.clients.*;
import se.frisk.edufyrecommendationsservice.dto.*;
import se.frisk.edufyrecommendationsservice.services.RecommendationService;

import java.util.List;

@RestController
@RequestMapping("/api/recommendations")
public class RecommendationController {

    private final RecommendationService recommendationService;
    private final HistoryClient historyClient;
    private final MusicClient musicClient;
    private final RatingsClient ratingsClient;
    private final PodClient podClient;
    private final VideoClient videoClient;

    public RecommendationController(RecommendationService recommendationService, HistoryClient historyClient,
                                    MusicClient musicClient, RatingsClient ratingsClient, PodClient podClient,
                                    VideoClient videoClient) {
        this.recommendationService = recommendationService;
        this.historyClient = historyClient;
        this.musicClient = musicClient;
        this.ratingsClient = ratingsClient;
        this.podClient = podClient;
        this.videoClient = videoClient;
    }

    //---------------Tests--------------------------------
    @GetMapping("/test")
    public String test() { return "Recommendations service up and running!"; }

    @GetMapping("/test/history")
    public ResponseEntity<List<HistoryItem>> testHistory(@RequestParam String userId,
                                                         @RequestParam MediaType mediaType) {
        List<HistoryItem> history = historyClient.getHistory(userId, mediaType);

        System.out.println("History items for " + userId + " (" + mediaType + "): " + history.size());

        return ResponseEntity.ok(history);
    }

    @GetMapping("/test/music")
    public ResponseEntity<List<MusicItem>> testMusic() {
        List<MusicItem> items = musicClient.getAvailableMusic();

        System.out.println("Available music: " + items.size());

        return ResponseEntity.ok(items);
    }

    @GetMapping("/test/ratings")
    public ResponseEntity<?> testRatings(@RequestParam String userId) {
        var liked = ratingsClient.getLikedIds(userId);
        var disliked = ratingsClient.getDislikedIds(userId);

        System.out.println("Ratings for " + userId +
                " | liked: " + liked.size() +
                " | disliked: " + disliked.size());

        return ResponseEntity.ok(
                java.util.Map.of(
                        "liked", liked,
                        "disliked", disliked
                )
        );
    }

    @GetMapping("/test/pod")
    public ResponseEntity<List<PodItem>> testPod() {
        var pods = podClient.getAvailablePods();
        System.out.println("Available pods: " + pods.size());
        return ResponseEntity.ok(pods);
    }

    @GetMapping("/test/video")
    public ResponseEntity<List<VideoItem>> testVideo() {
        var videos = videoClient.getAvailableVideos();
        System.out.println("Videos: " + videos.size());
        return ResponseEntity.ok(videos);
    }

    //---------------Real Stuff-------------------
    @GetMapping("/next")
    public ResponseEntity<String> next(@RequestParam String userId,
                                       @RequestParam String currentMediaId,
                                       @RequestParam MediaType mediaType) {
        String id = recommendationService.pickNext(userId, currentMediaId, mediaType);
        return (id != null && !id.isBlank()) ? ResponseEntity.ok(id) : ResponseEntity.noContent().build();
    }

    @GetMapping("/top")
    public ResponseEntity<List<String>> top(@RequestParam String userId,
                                            @RequestParam(defaultValue = "10") int limit,
                                            @RequestParam MediaType mediaType) {
        var list = recommendationService.pickTop(userId, limit, mediaType);
        return (list == null || list.isEmpty())
                ? ResponseEntity.noContent().build()
                : ResponseEntity.ok(list);
    }
}
