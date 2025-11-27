package se.frisk.edufyrecommendationsservice.clients;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import se.frisk.edufyrecommendationsservice.dto.RatingItem;
import java.util.Arrays;
import java.util.List;

@Component
public class RatingsHttpClient implements RatingsClient {

    private final RestClient restClient;

    public RatingsHttpClient(RestClient.Builder restClient,
                             @Value("${services.ratings.base-url}") String baseUrl) {
        this.restClient = restClient
                    .baseUrl(baseUrl)
                    .requestInterceptor((request, body, execution) -> {

                        var auth = SecurityContextHolder.getContext().getAuthentication();

                        if (auth instanceof JwtAuthenticationToken jwtAuth) {
                            String token = jwtAuth.getToken().getTokenValue();
                            request.getHeaders().setBearerAuth(token);
                        }

                        return execution.execute(request, body);
                    })
                    .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                    .build();
        }

    @Override
    public List<String> getLikedIds(String userId) {
        RatingItem[] response = restClient.get()
                .uri("/ratings/getlikesbyuser/{userId}", userId)
                .retrieve()
                .body(RatingItem[].class);

        if (response == null) return List.of();

        return Arrays.stream(response)
                .map(RatingItem::getMediaId)
                .filter(id -> id != null && !id.isBlank())
                .toList();
    }

    @Override
    public List<String> getDislikedIds(String userId) {
        RatingItem[] response = restClient.get()
                .uri("/ratings/getdislikesbyuser/{userId}", userId)
                .retrieve()
                .body(RatingItem[].class);

        if (response == null) return List.of();

        return Arrays.stream(response)
                .map(RatingItem::getMediaId)
                .filter(id -> id != null && !id.isBlank())
                .toList();
    }
}