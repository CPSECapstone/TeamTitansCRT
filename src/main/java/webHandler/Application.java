package webHandler;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.sql.SQLException;

@SpringBootApplication
public class Application {

    public static void main(String[] args) {
        try {
            DBUtil db = new DBUtil("captureDatabase.db");
            DBUtil.createNewTable("captureDatabase.db");
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
        SpringApplication.run(Application.class, args);
    }
}
