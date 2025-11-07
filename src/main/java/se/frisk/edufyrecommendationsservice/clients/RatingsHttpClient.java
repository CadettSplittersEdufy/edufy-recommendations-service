package se.frisk.edufyrecommendationsservice.clients;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import se.frisk.edufyrecommendationsservice.dto.RatingItem;

import java.util.Arrays;
import java.util.List;

@Component
public class RatingsHttpClient implements RatingsClient {

    private final RestClient restClient;

    public RatingsHttpClient(RestClient.Builder builder,
                             @Value("${services.ratings.base-url}") String baseUrl) {
        this.restClient = builder.baseUrl(baseUrl).build();
    }

    @Override
    public List<String> getLikedIds(String userId){
        RatingItem[] response = restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/ratings/user/{userId}")
                        .build(userId))
                .retrieve().body(RatingItem[].class);

        if(response == null){
            return List.of();
        }
        return Arrays.stream(response)
                .filter(RatingItem::isLiked)
                .map(RatingItem::getMediaId)
                .toList();
    }

    @Override
    public List<String> getDislikedIds(String userId){
        RatingItem[] response = restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/ratings/user/{userId}")
                        .build(userId))
                .retrieve()
                .body(RatingItem[].class);

        if(response == null){
            return List.of();
        }

        return Arrays.stream(response)
                .filter(r -> !r.isLiked())
                .map(RatingItem::getMediaId)
                .toList();
    }
}