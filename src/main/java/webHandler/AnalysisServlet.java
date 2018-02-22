package webHandler;

import com.amazonaws.services.cloudwatch.model.GetMetricStatisticsResult;
import com.amazonaws.util.IOUtils;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * Servlet to handle all analysis calls.
 */
@RestController
public class AnalysisServlet {

    //TODO: Update to only take capture ID when persistent capture data is added.
    /**
     * Method to handle post requests to /analysis.
     * @param response HttpServletResponse to stream metric data to.
     * @param capture Capture containing the id and s3 where metric data is stored.
     * @throws IOException Throws an IOException if unable to copy stream to response.
     */
    @RequestMapping(value = "/analysis", method = RequestMethod.POST)
    public void getMetrics(HttpServletResponse response, @RequestBody Capture capture) throws IOException {

        InputStream stream;
        // TODO: Update parameter name from capture to captureId, add Capture DAO, sudo-code below
        // Capture capture = DatabaseManager.getCaptureWithID(captureId.getId());

        // Get metric stream
        if (capture.getStatus().equals("Running")) { // Obtain from CloudWatch if capture is currently running
            CloudWatchManager cloudManager = new CloudWatchManager();
            String metrics = cloudManager.getAllMetricStatisticsAsJson(capture.getRds(), capture.getStartTime(), new Date());
            stream = new ByteArrayInputStream(metrics.getBytes(StandardCharsets.UTF_8));
        } else { // Obtain from S3 if capture has already finished
            S3Manager s3Manager = new S3Manager();
            stream = s3Manager.getFile(capture.getS3(), capture.getId() + "-Performance.log");
        }

        //TODO: Replace else statement with commented block when Capture DAO added (Test everything else before replacing).
        /*else if (capture.getStatus().equals("Finished")) { // Obtain from S3 if capture has already finished
            S3Manager s3Manager = new S3Manager();
            stream = s3Manager.getFile(capture.getS3(), capture.getId() + "-Performance.log");
        } else {
            return;
        }*/

        setResponseOutputStream(response, stream, capture.getId());
    }

    /**
     * Method to copy InputStream to HttpServletResponse.
     * @param response HttpServletResponse to copy stream to.
     * @param stream Stream to copy to response.
     * @param id Stream file id.
     * @throws IOException Thrown if error is unable to be written to response.
     */
    public void setResponseOutputStream(HttpServletResponse response, InputStream stream, String id) throws IOException {
        // Return error if performance log not found
        if (stream == null) {
            response.sendError(HttpStatus.BAD_REQUEST.value(), "Error: No capture performance log found in specified s3 bucket");
            return;
        }

        // Copy the stream to the response's output stream.
        try {
            IOUtils.copy(stream, response.getOutputStream());
            response.flushBuffer();
        } catch (Exception e) {
            response.sendError(HttpStatus.BAD_REQUEST.value(), "Error: Unable to copy metric stream to response.");
            return;
        }

        // Set the content type and attachment header.
        response.addHeader("Content-disposition", "attachment;filename=" + id + "-Performance.log");
        response.setContentType("txt/plain");
    }
}
