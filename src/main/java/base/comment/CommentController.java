package base.comment;

import base.post.NewsfeedPost;
import base.post.PostDatabase;
import base.security.CurrentUser;
import base.user.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by michellegu on 5/23/17.
 */

@RestController
@RequestMapping("/comments")
public class CommentController {
    @Autowired
    private PostDatabase postRepository;
    @Autowired
    private CommentDatabase commentRepository;

    @GetMapping("{id}")
    public List<Comment> handleGetAllComments(@PathVariable Long id) {
        List<Long> commentIds = postRepository.findOne(id).getComments();
        List<Comment> comments = new ArrayList<>();

        for(Long c: commentIds) {
            comments.add(commentRepository.findOne(c));
        }

        return comments;
    }

    @PostMapping("{id}")
    public NewsfeedPost handleAddNewComment(@PathVariable Long id, @CurrentUser User currentUser, @RequestBody Comment input) {
        Comment comment = new Comment(currentUser, input.getMessage(), input.getParentId());
        commentRepository.save(comment);

        NewsfeedPost post = postRepository.findOne(id);
        post.addComment(comment.getId());
        postRepository.save(post);

        return post;
    }
}
