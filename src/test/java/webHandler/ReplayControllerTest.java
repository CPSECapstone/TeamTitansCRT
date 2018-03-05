package webHandler;

import org.junit.Before;
import org.junit.After;
import org.junit.Test;

import org.springframework.http.HttpStatus;

import static org.junit.Assert.*;

public class ReplayControllerTest {

    private ReplayController replayController;

    @Before
    public void before() throws Exception {
        replayController = new ReplayController();
    }

    @Test
    public void testReplayStart() throws Exception {
        Replay replay = new Replay("id", "testRDS", "testS3");
        replay.setDatabaseInfo(new DatabaseInfo("", "", "", ""));

        HttpStatus actual = replayController.startReplay(replay, "Fast Mode").getStatusCode();
        assertEquals(HttpStatus.OK,actual);
    }

    @Test
    public void testReplayStop() throws Exception {
        Replay replay = new Replay("id", "testRDS", "testS3");
        replay.setDatabaseInfo(new DatabaseInfo("", "", "", ""));

        HttpStatus status = replayController.stopReplay(replay).getStatusCode();
        assertEquals(HttpStatus.OK,status);
    }

    @After
    public void after() throws Exception {
        replayController = null;
        assertNull(replayController);
    }
}