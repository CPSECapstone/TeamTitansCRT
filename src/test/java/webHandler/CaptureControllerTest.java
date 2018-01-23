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
        HttpStatus actual = captureController.CaptureStart(new Capture("id", "testRDS", "testS3")).getStatusCode();
        assertEquals(HttpStatus.OK,actual);
    }

    @Test
    public void captureStatus() throws Exception {
        HttpStatus status = captureController.CaptureStatus().getStatusCode();
        assertEquals(HttpStatus.OK,status);
    }

    @After
    public void after() throws Exception {
        captureController = null;
        assertNull(captureController);
    }
}