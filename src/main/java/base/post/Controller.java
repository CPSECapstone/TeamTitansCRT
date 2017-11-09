package base.post;

import base.security.CurrentUser;
import base.tag.Tag;
import base.tag.TagDatabase;
import base.user.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Collections;

@RestController
@RequestMapping("/posts")
public class Controller {

    @Autowired
    PostDatabase postRepository;
    @Autowired
    TagDatabase tagRepository;

    @GetMapping
    public List<NewsfeedPost> handleGetAll() {
        ArrayList<NewsfeedPost> posts = new ArrayList<>();
        postRepository.findAll().forEach(post -> posts.add(post));

        Collections.sort(posts, new NewsfeedPost());
        return posts;
    }

    @GetMapping("{tags}")
    public List<NewsfeedPost> handleFilterRequest(@PathVariable String[] tags) {
        List<NewsfeedPost> posts = new ArrayList<>();


        for(String t: tags) {
            //get all the ids of the post with the specified tag
            List<Long> postsId = tagRepository.findByName(t).getAllPosts();

            //get the NewsfeedPost object based on ID and add it to the posts list
            for(Long p: postsId) {
                posts.add(postRepository.findOne(p));
            }
        }
        return posts;
    }

    @PostMapping
    public NewsfeedPost handleNewPostRequest(@CurrentUser User currentUser, @RequestBody NewsfeedPost input) {
        NewsfeedPost np = new NewsfeedPost(currentUser, input.getMessage(), input.getTags(), input.getImage());
        postRepository.save(np);

        //the comma should be changed to # later
        String[] tags = input.getTags().split(",");

        //for every tag inputted, add the new post to the tags post list
        for(String t: tags) {
            Tag foundTag = tagRepository.findByName(t);

            //tag is not found, a new tag is created and store in the db
            //the new post is added to the new tag's post list
            if(foundTag == null) {
                Tag newTag = new Tag(t);
                newTag.addPost(np.getId());
                tagRepository.save(newTag);
            }
            else { // tag is found, add the new post to the tag's post list
                foundTag.addPost(np.getId());
                tagRepository.save(foundTag);
            }
        }

        return np;
    }

    @DeleteMapping("{id}")
    public void delete(@PathVariable Long id) {
        postRepository.delete(id);
    }

    @DeleteMapping
    public void deleteAll() {
        postRepository.deleteAll();
    }


    @PutMapping("/{id}")
    public NewsfeedPost incrementLike(@CurrentUser User currentUser, @PathVariable Long id) {
        NewsfeedPost post = (NewsfeedPost)postRepository.findOne(id);
        if (post == null) {
            return null;
        } else {
            if(post.hasLiked(currentUser) != -1) {
                post.removeLike(currentUser);
            }
            else {
                post.addLike(currentUser);
            }
            return postRepository.save(post);
        }
    }


    @PutMapping
    public NewsfeedPost update(@RequestBody NewsfeedPost input) {
        NewsfeedPost post = (NewsfeedPost)postRepository.findOne(input.getId());
        if (post == null) {
            return null;
        } else {
            post.setMessage(input.getMessage());
            post.setLikes(input.getLikes());
            post.setTags(input.getTags());
            return postRepository.save(post);
        }
    }

}
