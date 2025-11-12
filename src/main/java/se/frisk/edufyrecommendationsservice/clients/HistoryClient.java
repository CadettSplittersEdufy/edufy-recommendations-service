package se.frisk.edufyrecommendationsservice.clients;

import se.frisk.edufyrecommendationsservice.dto.HistoryItem;
import se.frisk.edufyrecommendationsservice.dto.MediaType;

import java.util.List;

public interface HistoryClient {

    List<HistoryItem> getHistory(String userId, MediaType mediaType, int limit);

    default List<HistoryItem> getHistory(String userId, MediaType mediaType) {
        return getHistory(userId, mediaType, 100);
    }
}
