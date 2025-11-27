package se.frisk.edufyrecommendationsservice.clients;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import se.frisk.edufyrecommendationsservice.dto.MusicItem;
import java.util.Arrays;
import java.util.List;

@Component
public class MusicHttpClient implements MusicClient {

    private final RestClient restClient;

    public MusicHttpClient(RestClient.Builder restClient,
                           @Value("${services.music.base-url}") String baseUrl) {

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
    public List<MusicItem> getAvailableMusic() {
        MusicItem[] response = restClient.get()
                .uri("/music/available")
                .retrieve()
                .body(MusicItem[].class);

        return response == null ? List.of() : Arrays.asList(response);
    }
}