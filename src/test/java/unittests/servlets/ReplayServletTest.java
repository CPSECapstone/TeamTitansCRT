package unittests.servlets;

import app.models.Capture;
import app.models.DatabaseInfo;
import app.models.Replay;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import app.servlets.ReplayServlet;
import app.util.DBUtil;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class ReplayServletTest {

    private ReplayServlet replayServlet;

    @Before
    public void before() throws Exception {
        replayServlet = new ReplayServlet();
    }

//    // This will replay an already captured workload in the S3 bucket onto the database
//    @Test
//    public void testReplayStart() throws Exception {
//        Capture capture = new Capture("MyCaptureTestKyle33", "testRds", "US_WEST_1","teamtitans-test-mycrt", "US_WEST_1");
//        capture.setStatus("Finished");
//        DBUtil.getInstance().saveCapture(capture);
//        Replay replay = new Replay("ReplayServletTest_TestReplayStart", "testRDS", "US_WEST_1", "teamtitans-test-mycrt", "US_WEST_1",
//                "Fast Mode", "MyCaptureTestKyle33");
//        replay.setDatabaseInfo(new DatabaseInfo("testdb.cgtpml3lsh3i.us-west-1.rds.amazonaws.com:3306",
//                "testdb", "US_WEST_1", "admin", "TeamTitans!"));
//        HttpStatus actual = replayServlet.startReplay(replay).getStatusCode();
//        assertEquals(HttpStatus.OK,actual);
//    }

//    @Test
//    public void testReplayStop() throws Exception {
//        Replay replay = new Replay("id", "testRDS", "US_WEST_1", "testS3", "US_WEST_1", "Fast Mode", "");
//        replay.setDatabaseInfo(new DatabaseInfo("", "", "", "", ""));
//        replayServlet.startReplay(replay);
//        HttpStatus status = replayServlet.stopReplay(replay).getStatusCode();
//        assertEquals(HttpStatus.OK,status);
//    }

    @After
    public void after() throws Exception {
        replayServlet = null;
        assertNull(replayServlet);
    }
}