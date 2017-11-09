package base.tag;

import javax.persistence.*;
import java.util.ArrayList;

@Entity
@Table(name = "TagTable")

public class Tag {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    private String name;

    private ArrayList<Long> posts;

    public Tag() {

    }

    public Tag(String name) {
        this.name = name;
        posts = new ArrayList<Long>();
    }

    public String getName() {
        return name;
    }

    public ArrayList<Long> getAllPosts() {
        return posts;
    }

    public void addPost(Long postId) {
        posts.add(postId);
    }

    public void removePost(Long postId) {
        posts.remove(postId);
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }
}