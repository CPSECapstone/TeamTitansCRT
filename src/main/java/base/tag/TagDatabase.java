package base.tag;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

/**
 * Created by michellegu on 4/19/17.
 */
public interface TagDatabase extends CrudRepository<Tag, Long> {
    Tag findByName(String name);

    @Query("SELECT name FROM Tag")
    String[] tagNames();
}
