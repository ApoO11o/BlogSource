package main.model.api;

public class CommentResponse {

    int id;
    long timestamp;
    String text;
    UserResponseForComments user;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public UserResponseForComments getUser() {
        return user;
    }

    public void setUser(UserResponseForComments user) {
        this.user = user;
    }
}
