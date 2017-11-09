package base.user;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

public interface UserRepository extends CrudRepository<User, Long> {

    User findByEmail(String email);

    User findByVerificationToken(String token);

    @Query("SELECT COUNT(*) FROM User u")
    int countUsers();

    @Query("SELECT COUNT(*) FROM User u where u.email LIKE '%@calpoly.edu%' ")
    int countPolyUsers();

    @Query("SELECT COUNT(*) FROM User u where u.role LIKE 'Student' ")
    int countStudents();
}
