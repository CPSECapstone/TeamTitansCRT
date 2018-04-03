package unittests.util;

import org.junit.*;
import app.util.DBUtil;

import static org.junit.Assert.*;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;

public class DBUtilTest {
    private DBUtil db = null;

    @Before
    public void setUp() {
        String test = "/Users/devin/test.db";
        //db = new DBUtil(test);
    }

    @After
    public void tearDown() throws SQLException {
        db.closeConnection();
    }

    @Test
    public void canWrite() throws SQLException {
        Connection conn = db.getConnection();

        try {
            assertFalse(conn.isReadOnly());
        }
        finally {
            conn.close();
        }
    }

    @Test
    public void readOnly() throws SQLException {
        Connection conn = db.getConnection();
        Statement stmt = conn.createStatement();

        try {
            assertTrue(conn.isReadOnly());

            stmt.executeUpdate("CREATE TABLE test(name, status)");
            stmt.executeUpdate("INSERT INTO test values ('test', 'running'");

            fail("Read only flag incorrectly set");
        } catch (SQLException e) {
            System.out.println("Read only flag correctly set");
        } finally {
            stmt.close();
            conn.close();
        }

    }

    @Test
    public void valid() throws SQLException {
        Connection conn = db.getConnection();
        assertTrue(conn.isValid(0));
        conn.close();
        assertFalse(conn.isValid(0));
    }

    @Test
    public void executeOnClosed() throws SQLException {
        Connection conn = db.getConnection();
        Statement stmt = conn.createStatement();
        conn.close();

        try {
            stmt.executeUpdate("CREATE TABLE test(name, status");
        }
        catch (SQLException e) {
            return;
        }

        fail("Unsuccessfully detected closed DB");
    }

    @Test
    public void isClosed() throws SQLException {
        Connection conn = db.getConnection();
        assertTrue(conn.isClosed());
    }

    @Test
    public void pragmaTest() throws SQLException {
        Connection conn = db.getConnection();
        Statement stmt = conn.createStatement();

        ResultSet rs = stmt.executeQuery("PRAGMA synchronous");
        assertEquals("NORMAL", rs.getString(1));
        rs.close();

        rs = stmt.executeQuery("PRAGMA journal_mode");
        assertEquals("WAL", rs.getString(1));
        rs.close();

        rs = stmt.executeQuery("PRAGMA cache_size");
        assertEquals(-131072, rs.getInt(1));
        rs.close();

        rs = stmt.executeQuery("PRAGMA threads");
        assertEquals(4, rs.getInt(1));
        rs.close();

        rs = stmt.executeQuery("PRAGMA foreign_keys");
        assertEquals("ON", rs.getString(1));
        rs.close();
    }

    @Test
    public void sync() throws SQLException {
        Connection conn = db.getConnection();
        Statement stmt = conn.createStatement();

        try {
            ResultSet rs = stmt.executeQuery("PRAGMA synchronous");
            if (rs.next()) {
                ResultSetMetaData rd = rs.getMetaData();
                int colCount = rd.getColumnCount();
                int syn = rs.getInt(1);
                assertEquals(0, syn);
            }
        }

        finally {
            stmt.close();
            conn.close();
        }
    }
}
