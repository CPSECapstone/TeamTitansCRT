package webHandler;

import org.junit.Test;
import org.springframework.http.ResponseEntity;

import static org.junit.Assert.*;

public class CaptureControllerTest {
    @Test
    public void captureCommand() throws Exception {
        CaptureController captureController = new CaptureController();
        String actual = captureController.CaptureCommand("capture","command").getBody();
        assertEquals("capture - command",actual);
    }

    @Test
    public void captureStatus() throws Exception {
        CaptureController captureController = new CaptureController();
        Status status = captureController.CaptureStatus("capture").getBody();
        assertEquals("Capturing",status.getStatus());
    }
}