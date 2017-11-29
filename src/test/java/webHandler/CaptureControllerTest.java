package webHandler;

import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.Assert.*;

public class CaptureControllerTest {
    @Test
    public void captureCommand() throws Exception {
        CaptureController captureController = new CaptureController();
        HttpStatus actual = captureController.CaptureStart(new Capture("id", "testRDS", "testS3")).getStatusCode();
        assertEquals(HttpStatus.OK,actual);
    }

    @Test
    public void captureStatus() throws Exception {
        CaptureController captureController = new CaptureController();
        Status status = captureController.CaptureStatus("capture").getBody();
        assertEquals("Capturing",status.getStatus());
    }
}