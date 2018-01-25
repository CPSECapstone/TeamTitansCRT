package webHandler;

import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpStatus;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
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
        when(mockResponse.getOutputStream()).thenReturn(mockOutput);
    }

    @Test
    public void testSetResponseOutputStreamSuccess() throws Exception {
        new AnalysisServlet().setResponseOutputStream(mockResponse, stream, "test");

        verify(mockResponse, times(1)).addHeader("Content-disposition", "attachment;filename=test-Performance.log");
        verify(mockResponse, times(1)).setContentType("txt/plain");
        verify(mockResponse, times(1)).flushBuffer();
    }

    @Test
    public void testSetResponseOutputStreamNullStream() throws Exception {
        new AnalysisServlet().setResponseOutputStream(mockResponse, null, "test");

        verify(mockResponse, times(1)).sendError(HttpStatus.BAD_REQUEST.value(), "Error: No capture performance log found in specified s3 bucket");
        verify(mockResponse, never()).addHeader("Content-disposition", "attachment;filename=test-Performance.log");
        verify(mockResponse, never()).setContentType("txt/plain");
        verify(mockResponse, never()).flushBuffer();
    }

    @Test
    public void testSetResponseOutputStreamBadCopy() throws Exception {
        when(mockResponse.getOutputStream()).thenReturn(null);
        new AnalysisServlet().setResponseOutputStream(mockResponse, stream, "test");

        verify(mockResponse, times(1)).sendError(HttpStatus.BAD_REQUEST.value(), "Error: Unable to copy metric stream to response.");
        verify(mockResponse, never()).addHeader("Content-disposition", "attachment;filename=test-Performance.log");
        verify(mockResponse, never()).setContentType("txt/plain");
        verify(mockResponse, never()).flushBuffer();
    }
}