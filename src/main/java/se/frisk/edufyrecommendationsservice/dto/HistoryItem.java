package se.frisk.edufyrecommendationsservice.dto;

import java.time.OffsetDateTime;

public class HistoryItem {

    private Long id;
    private String userId;
    private String itemType;
    private String itemId;
    private OffsetDateTime playedAt;

    public HistoryItem() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getItemType() {
        return itemType;
    }

    public void setItemType(String itemType) {
        this.itemType = itemType;
    }

    public String getItemId() {
        return itemId;
    }

    public void setItemId(String itemId) {
        this.itemId = itemId;
    }

    public OffsetDateTime getPlayedAt() {
        return playedAt;
    }

    public void setPlayedAt(OffsetDateTime playedAt) {
        this.playedAt = playedAt;
    }
}
