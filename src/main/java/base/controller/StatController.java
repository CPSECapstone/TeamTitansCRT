package base.controller;

import base.post.PostDatabase;
import base.tag.Tag;
import base.tag.TagDatabase;
import base.user.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by melyou on 5/11/17.
 */
@RestController
@RequestMapping("/stats")
public class StatController {

    @Autowired
    UserRepository userRepository;

    @Autowired
    PostDatabase postRepository;

    @Autowired
    TagDatabase tagRepository;

    @RequestMapping("/totalUsers")
    public int totalUsers() {
        return userRepository.countUsers();
    }

    @RequestMapping("/totalPolyUsers")
    public int totalPolyUsers() {
        return userRepository.countPolyUsers();
    }

    @RequestMapping("/totalStudents")
    public int totalStudentUsers() {
        return userRepository.countStudents();
    }

    @RequestMapping("/totalPosts")
    public int totalPosts() {
        return postRepository.countPosts();
    }

    @RequestMapping("/tagNames")
    public String[] tagNames() {
        return tagRepository.tagNames();
    }

    @RequestMapping("/{tagName}")
    public int postsPerTag(@PathVariable(value = "tagName") String name) {
        Tag tag = tagRepository.findByName(name);
        return tag.getAllPosts().size();
    }
}
