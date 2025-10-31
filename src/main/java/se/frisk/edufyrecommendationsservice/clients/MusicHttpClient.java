package se.frisk.edufyrecommendationsservice.clients;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.RequestEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.Collections;
import java.util.List;

@Component
public class MusicHttpClient implements MusicClient {

    private final RestTemplate http;
    private final String baseUrl;

    public MusicHttpClient(RestTemplate http,
                           @Value("${app.services.musicBaseUrl:}") String baseUrl) {
        this.http = http;
        this.baseUrl = trimSlash(baseUrl);
    }

    @Override
    public List<String> getByGenres(List<String> genres, int limit) {
        if (baseUrl.isBlank()) return Collections.emptyList();
        try {
            String csv = String.join(",", genres);
            URI uri = URI.create(baseUrl + "/by-genres?genres=" + url(csv) + "&limit=" + limit);
            var res = http.exchange(RequestEntity.get(uri).build(),
                    new ParameterizedTypeReference<List<String>>() {});
            return res.getBody() != null ? res.getBody() : Collections.emptyList();
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    @Override
    public List<String> getByGenresExcept(List<String> genres, int limit) {
        if (baseUrl.isBlank()) return Collections.emptyList();
        try {
            String csv = String.join(",", genres);
            URI uri = URI.create(baseUrl + "/by-genres-except?genres=" + url(csv) + "&limit=" + limit);
            var res = http.exchange(RequestEntity.get(uri).build(),
                    new ParameterizedTypeReference<List<String>>() {});
            return res.getBody() != null ? res.getBody() : Collections.emptyList();
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    private static String url(String s) { return s.replace(" ", "%20"); }
    private static String trimSlash(String s) {
        if (s == null || s.isBlank()) return "";
        return s.endsWith("/") ? s.substring(0, s.length() - 1) : s;
    }
}