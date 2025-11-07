package se.frisk.edufyrecommendationsservice.clients;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import se.frisk.edufyrecommendationsservice.dto.MusicItem;

import java.util.Arrays;
import java.util.List;

@Component
public class MusicHttpClient implements MusicClient {

    private final RestClient restClient;

    public MusicHttpClient(RestClient.Builder builder,
                           @Value("${services.music.base-url}") String baseUrl) {
        this.restClient = builder.baseUrl(baseUrl).build();
    }

    @Override
    public List<MusicItem> getAvailableMusic() {
        MusicItem[] response = restClient.get()
                .uri("/music/available")
                .retrieve()
                .body(MusicItem[].class);

        return response == null ? List.of() : Arrays.asList(response);
    }
}