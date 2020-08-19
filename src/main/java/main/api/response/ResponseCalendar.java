package main.api.response;

import java.util.HashMap;
import java.util.List;

public class ResponseCalendar {

    List<Integer> years;
    HashMap posts;

    public List<Integer> getYears() {
        return years;
    }

    public void setYears(List<Integer> years) {
        this.years = years;
    }

    public HashMap getPosts() {
        return posts;
    }

    public void setPosts(HashMap posts) {
        this.posts = posts;
    }
}
