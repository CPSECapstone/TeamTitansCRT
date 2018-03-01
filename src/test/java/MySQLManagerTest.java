import org.junit.Test;
import webHandler.DatabaseManager;
import webHandler.MySQLManager;

import static org.junit.Assert.*;

/*public class CrtControllerTest {
    @Test
    public void index() throws Exception {
        CrtController crtCon = new CrtController();
        assertEquals("Welcome to MyCRT!", crtCon.index());
    }

}*/

public class MySQLManagerTest {
    @Test
    public void query() throws Exception
    {
        DatabaseManager db = new MySQLManager("testdb.cgtpml3lsh3i.us-west-1.rds.amazonaws.com:3306",
                "testdb", "admin", "TeamTitans!");
        assertTrue(db.query("SELECT 1"));
    }
}
