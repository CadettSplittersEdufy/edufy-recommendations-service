package se.frisk.edufyrecommendationsservice.clients;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import se.frisk.edufyrecommendationsservice.dto.UserDTO;

@Component
public class UserHttpClient implements UserClient {

    private final RestClient restClient;
    private final String baseUrl;

    public UserHttpClient(RestClient.Builder restClient,
                          @Value("${services.user.base-url}") String baseUrl) {
        this.baseUrl = baseUrl;
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
    public UserDTO getUserById(Long userId) {
        try {
            return restClient.get()
                    .uri(baseUrl + "/get/{id}", userId)
                    .retrieve()
                    .body(UserDTO.class);
        } catch (RestClientException e) {
            return null;
        }
    }

    @Override
    public boolean userIsActive(Long userId) {
        UserDTO user = getUserById(userId);
        return user != null && user.isActive();
    }
}
