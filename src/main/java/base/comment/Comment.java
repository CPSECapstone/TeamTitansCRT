package base.comment;
import base.user.User;

import javax.persistence.*;
import java.text.SimpleDateFormat;
import java.util.Date;

@Entity
@Table(name = "CommentTable")

public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;
    private long parentId;

    private String timeStamp;
    private User author;
    private String message;

    public Comment() {
    }

    public Comment(User author, String message, long parentId) {
        this.author = author;
        this.message = message;
        this.timeStamp = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").format(new Date());
        this.parentId = parentId;
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

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getParentId() {
        return parentId;
    }

    public void setParentId(long parentId) {
        this.parentId = parentId;
    }
}
