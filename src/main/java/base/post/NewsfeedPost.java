package base.post;

import base.user.User;
import com.fasterxml.jackson.annotation.JsonFormat;

import javax.persistence.*;
import java.text.SimpleDateFormat;
import java.util.*;

@Entity
@Table(name = "PostTable")
public class NewsfeedPost implements Comparator<NewsfeedPost>{

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;
    private String tags;
    private byte[] image;

    private ArrayList<User> likes;
    //store comment ids instead?
    private ArrayList<Long> comments;

    //info common for all posts and comments
    private String timeStamp;
    private User author;
    private String message;

    public NewsfeedPost() {

    }

    public NewsfeedPost(User author, String message, String tags, byte[] image) {
        this.author = author;
        this.message = message;
        this.timeStamp = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").format(new Date());
        this.tags = tags;
        this.image = image;
        this.comments = new ArrayList<>();
        this.likes = new ArrayList<User>();

    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public User getAuthor() {
        return author;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    @JsonFormat(with = JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
    public ArrayList<User> getLikes() {
        return likes;
    }

    public void setLikes(ArrayList<User> likes) {
        this.likes = likes;
    }

    public byte[] getImage() {
        return image;
    }

    public void setImage(byte[] image) {
        this.image = image;
    }

    public int hasLiked(User user) {
        for(int i = 0; i < likes.size(); i++) {
            if(likes.get(i).getId().equals(user.getId())) {
                return i;
            }
        }
        return -1;
    }

    public void addLike(User user) {
        likes.add(user);
    }

    public void removeLike(User user) {
        int idx = hasLiked(user);
        if(idx != -1) {
            likes.remove(idx);
        }
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public ArrayList<Long> getComments() {
        return comments;
    }

    public void addComment(Long id) {
        comments.add(id);
    }

    @Override
    public int compare(NewsfeedPost o1, NewsfeedPost o2) {
        return o2.getTimeStamp().compareTo(o1.getTimeStamp());
    }
}