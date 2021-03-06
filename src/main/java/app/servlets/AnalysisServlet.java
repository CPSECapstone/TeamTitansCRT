package app.servlets;

import app.models.Session;
import com.amazonaws.services.cloudwatch.model.Datapoint;
import com.amazonaws.services.cloudwatch.model.GetMetricStatisticsResult;
import com.amazonaws.util.IOUtils;
import app.controllers.LogController;
import app.managers.CloudWatchManager;
import app.models.MetricRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import app.util.DBUtil;

import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
/**
 * Servlet to handle all analysis calls.
 */
@RestController
public class AnalysisServlet {

    /**
     * Method to handle post requests to /analysis.
     * @param response HttpServletResponse to stream metric data to.
     * @param idType Capture containing the id.
     */
    @RequestMapping(value = "/analysis", method = RequestMethod.POST)
    public void getMetrics(HttpServletResponse response, @RequestBody HashMap<String, String> idType) {

        InputStream stream;
        Session session = null;
        if (idType.get("type").equals("Capture")) {
            session = DBUtil.getInstance().loadCapture(idType.get("id"));
        } else if (idType.get("type").equals("Replay")) {
            session = DBUtil.getInstance().loadReplay(idType.get("id"));
        }

        if (session == null) {
            writeError(response, "Error: No capture found with given id:" + session.getId());
            return;
        }

        // Get metric stream
        if (session.getStatus().equals("Running")) { // Obtain from CloudWatch if capture is currently running
            String metrics = LogController.getMetricsFromCloudWatch(session.getRds(), session.getRdsRegion(), session.getStartTime(), new Date());
            stream = new ByteArrayInputStream(metrics.getBytes(StandardCharsets.UTF_8));
        } else if (session.getStatus().equals("Finished")) { // Obtain from S3 if capture has already finished
            String metrics = LogController.getMetricsFromS3(session.getS3(), session.getRdsRegion(), session.getId() + "-Performance.log");
            stream = new ByteArrayInputStream(metrics.getBytes(StandardCharsets.UTF_8));
        } else {
            writeError(response, "Error: Capture is not 'Running' or 'Finished'");
            return;
        }

        setResponseOutputStream(response, stream, session.getId());
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
        CloudWatchManager cloudWatchManager = new CloudWatchManager(request.getRdsRegion());
        List<Double> averages = new ArrayList<Double>();

        //set end time to now if it isn't given
        if(request.getEndTime() == null) {
            request.setEndTime(new Date(System.currentTimeMillis()));
        }

        ArrayList<GetMetricStatisticsResult> resultList =
                cloudWatchManager.getMetricStatistics(request.getRDS(),
                    request.getStartTime(),
                    request.getEndTime(),
                    request.getMetrics());

        for(GetMetricStatisticsResult result: resultList) {
            Double average = 0.0;
            Double sum = 0.0;
            int size = result.getDatapoints().size();
            if(size != 0) {
                for (Datapoint datapoint : result.getDatapoints()) {
                    sum += datapoint.getAverage();
                }
                average = sum/size;
            }
            averages.add(average);
        }
        return new ResponseEntity<List<Double>>(averages, HttpStatus.OK);
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
