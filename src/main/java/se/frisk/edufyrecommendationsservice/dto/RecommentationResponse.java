package se.frisk.edufyrecommendationsservice.dto;

import java.util.List;

public record RecommentationResponse (String userId, List<RecommendationItem> items) {

}
