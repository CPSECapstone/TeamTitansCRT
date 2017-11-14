import org.junit.Test;

import static org.junit.Assert.*;

/*public class CrtControllerTest {
    @Test
    public void index() throws Exception {
        CrtController crtCon = new CrtController();
        assertEquals("Welcome to MyCRT!", crtCon.index());
    }

}*/

public class DatabaseTest {
    @Test
    public void getConnection() throws Exception
    {
        Database db = new Database("testdb.cgtpml3lsh3i.us-west-1.rds.amazonaws.com:3306", "testdb", "admin", "TeamTitans!");
        assertNotNull(db.getConnection());
    }
}
