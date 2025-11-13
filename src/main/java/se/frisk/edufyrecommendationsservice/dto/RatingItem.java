package se.frisk.edufyrecommendationsservice.dto;

public class RatingItem {

    private String mediaId;
    private boolean liked;

    public RatingItem() {
    }

    public String getMediaId() {
        return mediaId;
    }

    public void setMediaId(String mediaId) {
        this.mediaId = mediaId;
    }

    public boolean isLiked() {
        return liked;
    }

    public void setLiked(boolean liked) {
        this.liked = liked;
    }
}
