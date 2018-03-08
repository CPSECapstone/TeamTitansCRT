package webHandler;

import org.junit.Before;
import org.junit.After;
import org.junit.Test;

import org.springframework.http.HttpStatus;

import static org.junit.Assert.*;

public class CaptureControllerTest {

    @Test
    public void captureCommand() throws Exception {
        HttpStatus actual = CaptureController.getInstance().captureStart(new Capture("id", "testRDS", "testS3")).getStatusCode();
        assertEquals(HttpStatus.OK,actual);
    }

    @Test
    public void captureStatus() throws Exception {
        HttpStatus status = CaptureController.getInstance().captureStatus().getStatusCode();
        assertEquals(HttpStatus.OK,status);
    }

}