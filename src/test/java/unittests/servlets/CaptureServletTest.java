package unittests.servlets;

import app.models.Capture;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import app.servlets.CaptureServlet;

import static org.junit.Assert.assertEquals;

public class CaptureServletTest {
    private CaptureServlet servlet;

    @Before
    public void before()
    {
        this.servlet = new CaptureServlet();
    }

    @Test
    public void captureCommand() throws Exception {
        Capture capture = new Capture("id", "testRDS", "US_WEST_1", "teamtitans-test-mycrt", "US_WEST_1");
        servlet.captureDelete(capture);
        HttpStatus actual = servlet.captureStart(capture).getStatusCode();
        assertEquals(HttpStatus.OK,actual);
    }

    @Test
    public void captureStatus() throws Exception {
        HttpStatus status = servlet.captureStatus().getStatusCode();
        assertEquals(HttpStatus.OK,status);
    }

}