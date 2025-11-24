package se.frisk.edufyrecommendationsservice.clients;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import se.frisk.edufyrecommendationsservice.dto.UserDTO;

@Component
public class UserHttpClient implements UserClient {

    private final RestClient restClient;
    private final String baseUrl;

    public UserHttpClient(RestClient restClient,
                          @Value("${services.user.base-url}") String baseUrl) {
        this.restClient = restClient;
        this.baseUrl = baseUrl;
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
