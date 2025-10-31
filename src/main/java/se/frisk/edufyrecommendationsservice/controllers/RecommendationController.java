package se.frisk.edufyrecommendationsservice.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/recommendations")
public class RecommendationController {

    private final se.frisk.edufyrecommendationsservice.services.RecommendationService recommendationService;

    public RecommendationController(se.frisk.edufyrecommendationsservice.services.RecommendationService recommendationService) {
        this.recommendationService = recommendationService;
    }

    @GetMapping("/test")
    public String test() { return "Recommendations service up and running!"; }

    @GetMapping("/next")
    public ResponseEntity<String> next(@RequestParam String userId,
                                       @RequestParam String currentMediaId) {
        String id = recommendationService.pickNext(userId, currentMediaId);
        return (id != null && !id.isBlank()) ? ResponseEntity.ok(id) : ResponseEntity.noContent().build();
    }

    @GetMapping("/top")
    public ResponseEntity<java.util.List<String>> top(@RequestParam String userId,
                                                      @RequestParam(defaultValue = "10") int limit) {
        var list = recommendationService.pickTop(userId, limit);
        return (list == null || list.isEmpty()) ? ResponseEntity.noContent().build() : ResponseEntity.ok(list);
    }
}
