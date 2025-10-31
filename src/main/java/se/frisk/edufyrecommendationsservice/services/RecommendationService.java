package se.frisk.edufyrecommendationsservice.services;

import org.springframework.stereotype.Service;
import se.frisk.edufyrecommendationsservice.clients.HistoryClient;
import se.frisk.edufyrecommendationsservice.clients.LikesClient;
import se.frisk.edufyrecommendationsservice.clients.MusicClient;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class RecommendationService {

    private final HistoryClient history;
    private final LikesClient likes;
    private final MusicClient music;

    public RecommendationService(HistoryClient history, LikesClient likes, MusicClient music) {
        this.history = history;
        this.likes = likes;
        this.music = music;
    }

    public String pickNext(String userId, String currentMediaId) {
        var genres   = Optional.ofNullable(history.getTopGenres(userId)).orElse(List.of());
        var disliked = Optional.ofNullable(likes.getDislikedIds(userId)).orElse(List.of());
        var played   = Optional.ofNullable(history.getPlayedIds(userId)).orElse(List.of());
        var candidates = genres.isEmpty() ? List.<String>of() : music.getByGenres(genres, 20);

        for (var id : candidates) {
            if (!Objects.equals(id, currentMediaId) && !played.contains(id) && !disliked.contains(id)) {
                return id;
            }
        }
        return ""; // Controller returnerar 204 om tom sträng
    }

    public List<String> pickTop(String userId, int limit) {
        var genres = Optional.ofNullable(history.getTopGenres(userId)).orElse(List.of());
        var list = genres.isEmpty() ? List.<String>of() : music.getByGenres(genres, limit);
        return list == null ? List.of() : list;
    }
}
