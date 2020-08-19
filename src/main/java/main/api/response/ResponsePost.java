package main.api.response;

import java.io.Serializable;
import java.util.List;
import main.model.api.PostResponse;

public class ResponsePost implements Serializable {

    int count;
    List<PostResponse> posts;

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public List getPosts() {
        return posts;
    }

    public void setPosts(List posts) {
        this.posts = posts;
    }
}
