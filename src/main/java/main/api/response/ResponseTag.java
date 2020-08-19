package main.api.response;

import java.util.List;
import main.model.api.TagResponse;

public class ResponseTag {
    List<TagResponse> tags;

    public List<TagResponse> getTags() {
        return tags;
    }

    public void setTags(List<TagResponse> tags) {
        this.tags = tags;
    }
}
