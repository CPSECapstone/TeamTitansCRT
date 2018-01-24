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

@RestController
public class AnalysisServlet {
    @RequestMapping(value = "/analysis", method = RequestMethod.POST)
    public void getMetrics(HttpServletResponse response, @RequestBody Capture capture) throws IOException {

        InputStream stream;

        // Get metric stream
        if (capture.getEndTime() != null && capture.getStartTime() != null && capture.getRds() != null && new Date().compareTo(capture.getEndTime()) < 0) {
            CloudWatchManager cloudManager = new CloudWatchManager();
            GetMetricStatisticsResult stats = cloudManager.getMetricStatistics(capture.getRds(), capture.getStartTime(), capture.getEndTime(), "CPUUtilization");
            stream = new ByteArrayInputStream(stats.toString().getBytes(StandardCharsets.UTF_8));
        } else {
            S3Manager s3Manager = new S3Manager();
            stream = s3Manager.getFile(capture.getS3(), capture.getId() + "-Performance.log");
        }

        setResponseOutputStream(response, stream, capture.getId());
    }

    /**
     *
     * @param response HttpServletResponse to copy stream to
     * @param stream Stream to copy to response
     * @param id Stream file id
     * @throws IOException Thrown if error is unable to be written to response
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
