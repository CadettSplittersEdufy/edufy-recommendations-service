package se.frisk.edufyrecommendationsservice.clients;

import java.util.List;

public interface HistoryClient {
    List<String> getTopGenres(String userId);
    List<String> getPlayedIds(String userId);
}
