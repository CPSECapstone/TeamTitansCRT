import org.junit.Test;
import webHandler.DatabaseManager;
import webHandler.MySQLManager;

import static org.junit.Assert.assertTrue;

public class MySQLManagerTest {
    @Test
    public void query() throws Exception
    {
        DatabaseManager db = new MySQLManager("testdb.cgtpml3lsh3i.us-west-1.rds.amazonaws.com:3306",
                "testdb", "admin", "TeamTitans!");
        assertTrue(db.query("SELECT 1"));
    }
}
