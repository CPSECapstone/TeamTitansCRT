package webHandler;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpStatus;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class ReplayServletTest {

    private ReplayServlet replayServlet;

    @Before
    public void before() throws Exception {
        replayServlet = new ReplayServlet();
    }

    // This will replay an already captured workload in the S3 bucket onto the database
    @Test
    public void testReplayStart() throws Exception {
        Capture capture = new Capture("MyCaptureTestKyle33", "testRds", "teamtitans-test-mycrt");
        capture.setStatus("Finished");
        DBUtil.getInstance().saveCapture(capture);
        Replay replay = new Replay("ReplayServletTest_TestReplayStart", "testRDS", "teamtitans-test-mycrt",
                "Fast Mode", "MyCaptureTestKyle33");
        replay.setDatabaseInfo(new DatabaseInfo("testdb.cgtpml3lsh3i.us-west-1.rds.amazonaws.com:3306",
                "testdb", "admin", "TeamTitans!"));
        HttpStatus actual = replayServlet.startReplay(replay).getStatusCode();
        assertEquals(HttpStatus.OK,actual);
    }

    @Test
    public void testReplayStop() throws Exception {
        Replay replay = new Replay("id", "testRDS", "testS3", "Fast Mode", "");
        replay.setDatabaseInfo(new DatabaseInfo("", "", "", ""));
        ReplayController.addReplay(replay);
        HttpStatus status = replayServlet.stopReplay(replay).getStatusCode();
        assertEquals(HttpStatus.OK,status);
    }

    @After
    public void after() throws Exception {
        replayServlet = null;
        assertNull(replayServlet);
    }
}