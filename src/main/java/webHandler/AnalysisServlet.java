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
import com.amazonaws.services.cloudwatch.model.*;

import java.util.*;
/**
 * Servlet to handle all analysis calls.
 */
@RestController
public class AnalysisServlet {

    /**
     * Method to handle post requests to /analysis.
     * @param response HttpServletResponse to stream metric data to.
     * @param captureId Capture containing the id.
     */
    @RequestMapping(value = "/analysis", method = RequestMethod.POST)
    public void getMetrics(HttpServletResponse response, @RequestBody Capture captureId) {

        InputStream stream;
        Capture capture = DBUtil.getInstance().loadCapture(captureId.getId());

        if (capture == null) {
            writeError(response, "Error: No capture found with given id:" + captureId.getId());
            return;
        }

        // Get metric stream
        if (capture.getStatus().equals("Running")) { // Obtain from CloudWatch if capture is currently running
            String metrics = LogController.getMetricsFromCloudWatch(capture.getRds(), capture.getStartTime(), new Date());
            stream = new ByteArrayInputStream(metrics.getBytes(StandardCharsets.UTF_8));
        } else if (capture.getStatus().equals("Finished")) { // Obtain from S3 if capture has already finished
            String metrics = LogController.getMetricsFromS3(capture.getS3(), capture.getId() + "-Performance.log");
            stream = new ByteArrayInputStream(metrics.getBytes(StandardCharsets.UTF_8));
        } else {
            writeError(response, "Error: Capture is not 'Running' or 'Finished'");
            return;
        }

        setResponseOutputStream(response, stream, capture.getId());
    }

    /**
     * Method to copy InputStream to HttpServletResponse.
     * @param response HttpServletResponse to copy stream to.
     * @param stream Stream to copy to response.
     * @param id Stream file id.
     */
    public void setResponseOutputStream(HttpServletResponse response, InputStream stream, String id) {
        // Return error if performance log not found
        if (stream == null) {
            writeError(response, "Error: No capture performance log found in specified s3 bucket");
            return;
        }

        // Copy the stream to the response's output stream.
        try {
            IOUtils.copy(stream, response.getOutputStream());
            response.flushBuffer();
        } catch (Exception e) {
            writeError(response, "Error: Unable to copy metric stream to response.");
            return;
        }

        // Set the content type and attachment header.
        response.addHeader("Content-disposition", "attachment;filename=" + id + "-Performance.log");
        response.setContentType("txt/plain");
    }
    
    /**
     * Calculates the average of metrics for a time span.
     * @param  request MetricRequest contains String rds, Date start, Date end, String... metrics
     * @return         Averages of metrics.
     */
    @RequestMapping(value = "/cloudwatch/average", method = RequestMethod.POST)
    public ResponseEntity<List<Double>> calculateAverages(@RequestBody MetricRequest request){
        List<Double> averages = new ArrayList<Double>();

        for(String metric : request.getMetrics()) {
            averages.add(calculateAverage(request.getRDS(), request.getStartTime(), request.getEndTime(), metric));
        }

        return new ResponseEntity<List<Double>>(averages, HttpStatus.OK);
    }

    /**
     * Calculate the average of a metric for a time span.
     * @param  rds    Database to get data from.
     * @param  start  Capture's Start time.
     * @param  end    Capture's end time. For current time use (new Date(System.currentTimeMillis())).
     * @param  metric Metric name to request ex. "CPUUtilization".
     * @return        Average of a single metric.
     */
    public Double calculateAverage(String rds, Date start, Date end, String metric) {
        CloudWatchManager cloudManager = new CloudWatchManager();
        GetMetricStatisticsResult result = cloudManager.getMetricStatistics(rds, start, end, metric);
        List<Datapoint> dataPoints = result.getDatapoints();
        Double averageSum = 0.0;

        if(dataPoints.isEmpty()){
            return averageSum;
        }

        for(Datapoint point: dataPoints) {
            averageSum += point.getAverage();
        }
        return averageSum/dataPoints.size();
    }

    /**
     * Method which attempts to write error to response.
     * @param response Response to write to
     * @param message Error message
     */
    private void writeError(HttpServletResponse response, String message) {
        try {
            response.sendError(HttpStatus.BAD_REQUEST.value(), message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
