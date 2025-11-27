package se.frisk.edufyrecommendationsservice.clients;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import se.frisk.edufyrecommendationsservice.dto.PodItem;

import java.util.Arrays;
import java.util.List;

@Component
public class PodHttpClient implements PodClient{

    private final RestClient restClient;

    public PodHttpClient(RestClient.Builder restClient,
                         @Value("${services.pod.base-url}") String baseUrl) {
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
    public List<PodItem> getAvailablePods() {
        PodItem[] response = restClient.get()
                .uri("/pod/available")
                .retrieve()
                .body(PodItem[].class);

        return response == null ? List.of() : Arrays.asList(response);
    }
}
