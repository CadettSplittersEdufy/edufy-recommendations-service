package se.frisk.edufyrecommendationsservice.contollers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import se.frisk.edufyrecommendationsservice.services.RecommendationService;

import java.util.List;

@RestController
@RequestMapping("/api/recommendations")
public class RecommendationController {

    private final RecommendationService recommendationService;

    public RecommendationController(RecommendationService recommendationService) {
        this.recommendationService = recommendationService;
    }


    @GetMapping("/test")
    public String test() {
        return "Recommendations service upp and running!";
    }

    @GetMapping("/next")
    public String getNextRecommendation(
            @RequestParam String userId,
            @RequestParam String mediaId
    ){
        return recommendationService.pickNext(userId, mediaId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.noContent().build());
    }

    @GetMapping("/top")
    public ResponseEntity<List<String>> topTenRecommendations(@RequestParam String userId,
                                                              @RequestParam(defaultValue = "10") int limit){
        var list = recommendationService.pickTop(userId, Math.max(1,Math.min(limit, 50)));
        if(list.isEmpty()) return ResponseEntity.noContent().build();
        return ResponseEntity.ok(list);
    }
}
