package webHandler;

import org.junit.Before;
import org.junit.After;
import org.junit.Test;

import org.springframework.http.HttpStatus;

import static org.junit.Assert.*;

public class CaptureControllerTest {

    private CaptureController captureController;

    @Before
    public void before() throws Exception {
        captureController = new CaptureController();
    }

    @Test
    public void captureCommand() throws Exception {
        HttpStatus actual = captureController.captureStart(new Capture("id", "testRDS", "testS3")).getStatusCode();
        assertEquals(HttpStatus.OK,actual);
    }

    @Test
    public void captureStatus() throws Exception {
        HttpStatus status = captureController.captureStatus().getStatusCode();
        assertEquals(HttpStatus.OK,status);
    }

    @After
    public void after() throws Exception {
        captureController = null;
        assertNull(captureController);
    }
}