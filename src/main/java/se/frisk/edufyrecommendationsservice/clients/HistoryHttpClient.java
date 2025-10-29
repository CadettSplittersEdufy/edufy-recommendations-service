package se.frisk.edufyrecommendationsservice.clients;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.RequestEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import se.frisk.edufyrecommendationsservice.client.HistoryClient;

import java.net.URI;
import java.util.Collections;
import java.util.List;

@Component
public class HistoryHttpClient implements HistoryClient {

    private final RestTemplate http;
    private final String baseUrl;

    public HistoryHttpClient(RestTemplate http,
                             @Value("${app.services.historyBaseUrl}") String baseUrl) {
        this.http = http;
        this.baseUrl = trimSlash(baseUrl);
    }

    @Override
    public List<String> getTopGenres(String userId) {
        try {
            URI uri = URI.create(baseUrl + "/top-genres?userId=" + url(userId) + "&limit=3");
            var res = http.exchange(RequestEntity.get(uri).build(),
                    new ParameterizedTypeReference<List<String>>() {});
            return res.getBody() != null ? res.getBody() : Collections.emptyList();
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    @Override
    public List<String> getPlayedIds(String userId) {
        try {
            URI uri = URI.create(baseUrl + "/played/" + url(userId));
            var res = http.exchange(RequestEntity.get(uri).build(),
                    new ParameterizedTypeReference<List<String>>() {});
            return res.getBody() != null ? res.getBody() : Collections.emptyList();
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    private static String url(String s) { return s.replace(" ", "%20"); }
    private static String trimSlash(String s) { return s.endsWith("/") ? s.substring(0, s.length()-1) : s; }
}
