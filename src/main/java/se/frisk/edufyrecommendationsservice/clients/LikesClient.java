package se.frisk.edufyrecommendationsservice.clients;

import java.util.List;

public interface LikesClient {
    List<String> getLikedIds(String userId);
    List<String> getDislikedIds(String userId);
}