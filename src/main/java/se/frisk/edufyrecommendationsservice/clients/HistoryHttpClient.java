package se.frisk.edufyrecommendationsservice.clients;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import se.frisk.edufyrecommendationsservice.dto.HistoryItem;
import se.frisk.edufyrecommendationsservice.dto.MediaType;

import java.util.Arrays;
import java.util.List;

@Component
public class HistoryHttpClient implements HistoryClient {

    private final RestClient restClient;

    public HistoryHttpClient(RestClient.Builder restClient,
                             @Value("${services.history.base-url}") String baseUrl) {
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
                    .defaultHeader(HttpHeaders.CONTENT_TYPE, org.springframework.http.MediaType.APPLICATION_JSON_VALUE)
                    .defaultHeader(HttpHeaders.ACCEPT, org.springframework.http.MediaType.APPLICATION_JSON_VALUE)
                    .build();
        }

    @Override
    public List<HistoryItem> getHistory(String userId, MediaType mediaType, int limit) {
        HistoryItem[] response = restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/history/historyByType/{userId}/{itemType}")
                        .queryParam("limit", limit)
                        .build(userId, mediaType.name()))
                .retrieve()
                .body(HistoryItem[].class);

        return response == null ? List.of() : Arrays.asList(response);
    }
}