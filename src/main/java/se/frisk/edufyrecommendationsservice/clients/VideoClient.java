package se.frisk.edufyrecommendationsservice.clients;

import se.frisk.edufyrecommendationsservice.dto.VideoItem;

import java.util.List;

public interface VideoClient {

    List<VideoItem> getAvailableVideos();
}
