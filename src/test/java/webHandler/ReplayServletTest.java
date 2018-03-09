package webHandler;

import org.junit.Before;
import org.junit.After;
import org.junit.Test;

import org.springframework.http.HttpStatus;

import static org.junit.Assert.*;

public class ReplayServletTest {

    private ReplayServlet replayServlet;

    @Before
    public void before() throws Exception {
        replayServlet = new ReplayServlet();
    }

    // This will replay 3 commands in MyCaptureKyleTest26-Workload.log onto the database
    @Test
    public void testReplayStart() throws Exception {
        Replay replay = new Replay("MyCaptureKyleTest26", "testRDS", "teamtitans-test-mycrt");
        replay.setDatabaseInfo(new DatabaseInfo("testdb.cgtpml3lsh3i.us-west-1.rds.amazonaws.com:3306",
                "testdb", "admin", "TeamTitans!"));

        HttpStatus actual = replayServlet.startReplay(replay, "Fast Mode").getStatusCode();
        assertEquals(HttpStatus.OK,actual);
    }

    @Test
    public void testReplayStop() throws Exception {
        Replay replay = new Replay("id", "testRDS", "testS3");
        replay.setDatabaseInfo(new DatabaseInfo("", "", "", ""));

        HttpStatus status = replayServlet.stopReplay(replay).getStatusCode();
        assertEquals(HttpStatus.OK,status);
    }

    @After
    public void after() throws Exception {
        replayServlet = null;
        assertNull(replayServlet);
    }
}