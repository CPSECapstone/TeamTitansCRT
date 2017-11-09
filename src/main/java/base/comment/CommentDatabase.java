package base.comment;

import org.springframework.data.repository.CrudRepository;


public interface CommentDatabase extends CrudRepository<Comment, Long> {

}