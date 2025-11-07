package se.frisk.edufyrecommendationsservice.clients;

import se.frisk.edufyrecommendationsservice.dto.MusicItem;

import java.util.List;

public interface MusicClient {

    List<MusicItem> getAvailableMusic();
}