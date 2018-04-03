package unittests.servlets;

import app.models.MetricRequest;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import app.servlets.AnalysisServlet;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.util.Date;
import java.util.List;

import static junit.framework.TestCase.assertTrue;
import static org.mockito.Mockito.*;

public class AnalysisServletTest {

    HttpServletResponse mockResponse;
    InputStream stream;
    ServletOutputStream mockOutput;

    @Before
    public void setUp() throws Exception {
        mockResponse = mock(HttpServletResponse.class);
        stream = new ByteArrayInputStream( "Testing response steam.".getBytes() );

        mockOutput = mock(ServletOutputStream.class);

        // Set the mock object to return mockOutput for the output stream
        when(mockResponse.getOutputStream()).thenReturn(mockOutput);
    }

    @Test
    public void testSetResponseOutputStreamSuccess() throws Exception {
        new AnalysisServlet().setResponseOutputStream(mockResponse, stream, "test");

        // Verify checks if the called function was invoked on the mockResponse the specified number of times
        verify(mockResponse, times(1)).addHeader("Content-disposition", "attachment;filename=test-Performance.log");
        verify(mockResponse, times(1)).setContentType("txt/plain");
        verify(mockResponse, times(1)).flushBuffer();
    }

    @Test
    public void testSetResponseOutputStreamNullStream() throws Exception {
        new AnalysisServlet().setResponseOutputStream(mockResponse, null, "test");

        // Verify checks if the called function was invoked on the mockResponse the specified number of times
        verify(mockResponse, times(1)).sendError(HttpStatus.BAD_REQUEST.value(), "Error: No capture performance log found in specified s3 bucket");
        verify(mockResponse, never()).addHeader("Content-disposition", "attachment;filename=test-Performance.log");
        verify(mockResponse, never()).setContentType("txt/plain");
        verify(mockResponse, never()).flushBuffer();
    }

    @Test
    public void testSetResponseOutputStreamBadCopy() throws Exception {
        // Set the mockResponse to return null for the output stream
        when(mockResponse.getOutputStream()).thenReturn(null);
        new AnalysisServlet().setResponseOutputStream(mockResponse, stream, "test");

        // Verify checks if the called function was invoked on the mockResponse the specified number of times
        verify(mockResponse, times(1)).sendError(HttpStatus.BAD_REQUEST.value(), "Error: Unable to copy metric stream to response.");
        verify(mockResponse, never()).addHeader("Content-disposition", "attachment;filename=test-Performance.log");
        verify(mockResponse, never()).setContentType("txt/plain");
        verify(mockResponse, never()).flushBuffer();
    }


    @Test
    public void calculateAverages() throws Exception {
        File f = new File(".privateKeys");
        org.junit.Assume.assumeTrue(f.exists() && f.isFile());
        //Measures from 2 hours before now.
        Date start = new Date(System.currentTimeMillis()-1000*60*60*2);
        //Ends measurements at 2 hours in the future. This actually just gets adjusted to the current time.
        Date end = new Date(System.currentTimeMillis()+1000*60*60*2);
        AnalysisServlet servlet = new AnalysisServlet();
        MetricRequest request = new MetricRequest("testdb", start, end, "CPUUtilization", "WriteThroughput");
        ResponseEntity<List<Double>> averages = servlet.calculateAverages(request);
        assertTrue(!averages.getBody().isEmpty());
    }


}