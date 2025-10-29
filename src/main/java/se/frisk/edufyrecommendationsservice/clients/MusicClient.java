package se.frisk.edufyrecommendationsservice.clients;

import java.util.List;


public interface MusicClient {
    List<String> getByGenres(java.util.List<String> genres, int limit);
    List<String> getByGenresExcept(java.util.List<String> genres, int limit);
}
