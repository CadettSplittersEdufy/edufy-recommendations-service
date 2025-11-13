package se.frisk.edufyrecommendationsservice.clients;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import se.frisk.edufyrecommendationsservice.dto.HistoryItem;
import se.frisk.edufyrecommendationsservice.dto.MediaType;

import java.util.Arrays;
import java.util.List;

@Component
public class HistoryHttpClient implements HistoryClient {

    private final RestClient restClient;

    public HistoryHttpClient(RestClient.Builder builder,
                             @Value("${services.history.base-url}") String baseUrl) {
        this.restClient = builder.baseUrl(baseUrl).build();
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