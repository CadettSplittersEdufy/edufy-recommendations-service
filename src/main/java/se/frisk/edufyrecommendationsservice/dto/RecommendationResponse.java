package se.frisk.edufyrecommendationsservice.dto;

import java.util.List;

public record RecommendationResponse(String userId, List<RecommendationItem> items) { }
