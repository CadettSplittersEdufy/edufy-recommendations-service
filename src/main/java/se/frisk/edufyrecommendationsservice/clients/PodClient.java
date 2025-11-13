package se.frisk.edufyrecommendationsservice.clients;

import se.frisk.edufyrecommendationsservice.dto.PodItem;

import java.util.List;

public interface PodClient {
    List<PodItem> getAvailablePods();
}
