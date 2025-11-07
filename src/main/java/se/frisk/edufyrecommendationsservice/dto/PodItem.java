package se.frisk.edufyrecommendationsservice.dto;

import java.util.List;

public class PodItem {

    private Long id;
    private String category;
    private List<String> genres;

    public PodItem() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public List<String> getGenres() {
        return genres;
    }

    public void setGenres(List<String> genres) {
        this.genres = genres;
    }
}
