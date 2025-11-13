package se.frisk.edufyrecommendationsservice.clients;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.beans.factory.annotation.Value;
import se.frisk.edufyrecommendationsservice.dto.VideoItem;

import java.util.Arrays;
import java.util.List;

@Component
public class VideoHttpClient implements VideoClient {

    private final RestClient restClient;

    public VideoHttpClient(RestClient.Builder builder,
                           @Value("${services.video.base-url}") String baseUrl) {
        this.restClient = builder.baseUrl(baseUrl).build();
    }

    @Override
    public List<VideoItem> getAvailableVideos() {
        VideoItem[] response = restClient.get()
                .uri("/video/get/available")
                .retrieve()
                .body(VideoItem[].class);

        return response == null ? List.of() : Arrays.asList(response);
    }
}
