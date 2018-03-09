package webHandler;

import org.junit.Before;
import org.junit.Test;

import org.springframework.http.HttpStatus;

import static org.junit.Assert.*;

public class CaptureServletTest {
    private CaptureServlet servlet;

    @Before
    public void before()
    {
        this.servlet = new CaptureServlet();
    }

    @Test
    public void captureCommand() throws Exception {
        HttpStatus actual = servlet.captureStart(new Capture("id", "testRDS", "testS3")).getStatusCode();
        assertEquals(HttpStatus.OK,actual);
    }

    @Test
    public void captureStatus() throws Exception {
        HttpStatus status = servlet.captureStatus().getStatusCode();
        assertEquals(HttpStatus.OK,status);
    }

}