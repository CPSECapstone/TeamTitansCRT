package webHandler;

import com.amazonaws.services.cloudwatch.model.GetMetricStatisticsResult;
import com.amazonaws.util.IOUtils;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.ResponseEntity;

import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Date;

import com.amazonaws.services.cloudwatch.AmazonCloudWatch;
import com.amazonaws.services.cloudwatch.AmazonCloudWatchClientBuilder;
import com.amazonaws.services.cloudwatch.model.*;

import java.util.*;
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
    
    /**
     * @param dbID      The database to get data from
     * @param start     The (capture's) start time
     * @param end       The end time. For current time use (new Date(System.currentTimeMillis()))
     * @param metric   One or more metric names to request ex. "CPUUtilization"
     * @return          The average through the timespan as a double
     */
    @RequestMapping(value = "/cloudwatch/average", method = RequestMethod.POST)
    public ResponseEntity<Double> calculateAverage(@RequestBody MetricRequest request){
        CloudWatchManager cloudManager = new CloudWatchManager();
        GetMetricStatisticsResult result = cloudManager.getMetricStatistics(request.getID(), request.getStartTime(), request.getEndTime(), request.getMetric());
        List<Datapoint> dataPoints = result.getDatapoints();
        double averageSum = 0.0;

        //It's usually empty when the start and end times are too close together
        if(dataPoints.isEmpty()){
            return new ResponseEntity<Double>(averageSum, HttpStatus.OK);
        }

        for(Datapoint point: dataPoints) {
            averageSum += point.getAverage();
        }

        return new ResponseEntity<Double>(averageSum / dataPoints.size(), HttpStatus.OK);
    }
}
