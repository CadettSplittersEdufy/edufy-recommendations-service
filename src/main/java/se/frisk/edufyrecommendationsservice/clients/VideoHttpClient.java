package se.frisk.edufyrecommendationsservice.clients;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.beans.factory.annotation.Value;
import se.frisk.edufyrecommendationsservice.dto.VideoItem;

import java.util.Arrays;
import java.util.List;

@Component
public class VideoHttpClient implements VideoClient {

    private final RestClient restClient;

    public VideoHttpClient(RestClient.Builder restClient,
                           @Value("${services.video.base-url}") String baseUrl) {
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
    public List<VideoItem> getAvailableVideos() {
        VideoItem[] response = restClient.get()
                .uri("/video/get/available")
                .retrieve()
                .body(VideoItem[].class);

        return response == null ? List.of() : Arrays.asList(response);
    }
}
