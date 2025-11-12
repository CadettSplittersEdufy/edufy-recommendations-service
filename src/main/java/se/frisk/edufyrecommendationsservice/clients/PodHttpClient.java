package se.frisk.edufyrecommendationsservice.clients;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import se.frisk.edufyrecommendationsservice.dto.PodItem;

import java.util.Arrays;
import java.util.List;

@Component
public class PodHttpClient implements PodClient{

    private final RestClient restClient;

    public PodHttpClient(RestClient.Builder builder,
                         @Value("${services.pod.base-url}") String baseUrl) {
        this.restClient = builder.baseUrl(baseUrl).build();
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
