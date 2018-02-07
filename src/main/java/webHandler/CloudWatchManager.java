package webHandler;

import java.io.File;
import java.io.FileReader;
import java.util.*;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.InstanceProfileCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.cloudwatch.AmazonCloudWatch;
import com.amazonaws.services.cloudwatch.AmazonCloudWatchClientBuilder;
import com.amazonaws.services.cloudwatch.model.*;

import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import org.json.simple.JSONObject;
import java.util.Date;
import java.util.stream.Collectors;

public class CloudWatchManager {

    private AmazonCloudWatch cwClient;

    public CloudWatchManager() {

        JSONParser parser = new JSONParser();

        try {
            // Running outside EC2 Instance:
            FileReader reader = new FileReader(new File(".privateKeys"));
            Object obj = parser.parse(reader);
            JSONObject jsonObject = (JSONObject) obj;

            String accessKey = (String) jsonObject.get("accessKey");
            String secretKey = (String) jsonObject.get("secretKey");

            BasicAWSCredentials awsCredentials = new BasicAWSCredentials(accessKey, secretKey);

            this.cwClient = AmazonCloudWatchClientBuilder.standard()
                    .withCredentials(new AWSStaticCredentialsProvider(awsCredentials))
                    .withRegion(Regions.US_WEST_1)
                    .build();
        } catch (Exception e) {
            // Running inside EC2 Instance:
            this.cwClient = AmazonCloudWatchClientBuilder.standard()
                    .withCredentials(InstanceProfileCredentialsProvider.getInstance())
                    .withRegion(Regions.US_WEST_1)
                    .build();
        }
    }

    /**
     *
     * @return An array of all metrics available on CloudWatch
     */
    public String[] getMetricNames() {
        return cwClient.listMetrics().getMetrics().stream()
                .map(x -> x.getMetricName())
                .distinct()
                .sorted()
                .toArray(String[]::new);
    }

    /**
     *
     * @param dbID The database to get data from
     * @param start The start time
     * @param end The end time
     * @return A string containing json for all metric results
     */
    public String getAllMetricStatisticsAsJson(String dbID, Date start, Date end) {
        return getMetricStatisticsAsJson(dbID, start, end, getMetricNames());
    }

    /**
     *
     * @param dbID      The database to get data from
     * @param start     The start time
     * @param end       The end time
     * @param metrics   One or more metric names to request
     * @return          A string containing json for the metric results
     */
    public String getMetricStatisticsAsJson(String dbID, Date start, Date end, String... metrics) {
        ArrayList<GetMetricStatisticsResult> results = getMetricStatistics(dbID, start, end, metrics);
        return convertMetricStatisticsToJson(results).toJSONString();
    }

    /**
     *
     * @param dbID      The database to get data from
     * @param start     The start time
     * @param end       The end time
     * @param metrics   One or more metric names to request
     * @return          A list of metric results listed in the same order as received
     */
    public ArrayList<GetMetricStatisticsResult> getMetricStatistics(String dbID, Date start, Date end, String... metrics) {
        ArrayList<GetMetricStatisticsResult> results = new ArrayList<>();
        for (String metric : metrics) {
            results.add(getMetricStatistics(dbID, start, end, metric));
        }

        return results;
    }

    /**
     *
     * @param dbID      The database to get data from
     * @param start  The start time
     * @param end    The end time
     * @param metric The name of the metric of interest
     * @return       Returns GetMetricStatisticsResult - for more information see http://docs.aws.amazon.com/AWSJavaSDK/latest/javadoc/com/amazonaws/services/cloudwatch/model/GetMetricStatisticsResult.html
     */
    public GetMetricStatisticsResult getMetricStatistics(String dbID, Date start, Date end, String metric) {
        GetMetricStatisticsRequest request = new GetMetricStatisticsRequest()
                .withNamespace("AWS/RDS")
                .withDimensions(new Dimension().withName("DBInstanceIdentifier").withValue(dbID))
                .withMetricName(metric)
                .withStartTime(start)
                .withEndTime(end)
                .withStatistics(Statistic.Sum, Statistic.Average)
                .withPeriod(60);


        return cwClient.getMetricStatistics(request);
    }

    /**
     *
     * @param results Arraylist of GetMetricStaticsResults to obtain json for
     * @return JSONArray of the provided results
     */
    @SuppressWarnings("unchecked")
    public static JSONArray convertMetricStatisticsToJson(ArrayList<GetMetricStatisticsResult> results) {
        JSONArray arr = new JSONArray();
        for (GetMetricStatisticsResult result : results) {
            arr.add(convertMetricStatisticsToJson(result));
        }
        return arr;
    }

    /**
     *
     * @param result GetMetricStaticsResult to obtain json for
     * @return JSONObject of the provided result
     */
    @SuppressWarnings("unchecked")
    public static JSONObject convertMetricStatisticsToJson(GetMetricStatisticsResult result) {
        JSONObject obj = new JSONObject();
        obj.put("Metric", result.getLabel());

        JSONArray dataPoints = new JSONArray();

        for (Datapoint point : result.getDatapoints()) {
            JSONObject jsonPoint = new JSONObject();

            jsonPoint.put("Timestamp", point.getTimestamp().getTime());

            if (point.getAverage() != null) {
                jsonPoint.put("Average", point.getAverage());
            }

            if (point.getSum() != null) {
                jsonPoint.put("Sum", point.getSum());
            }

            if (point.getMaximum() != null) {
                jsonPoint.put("Max", point.getMaximum());
            }

            if (point.getMinimum() != null) {
                jsonPoint.put("Min", point.getMinimum());
            }

            if (point.getSampleCount() != null) {
                jsonPoint.put("SampleCount", point.getSampleCount());
            }

            if (point.getUnit() != null) {
                jsonPoint.put("Unit", point.getUnit());
            }
            dataPoints.add(jsonPoint);
        }

        obj.put("DataPoints", dataPoints);
        return obj;
    }

    /* @param dbID      The database to get data from
     * @param start     The (capture's) start time
     * @param end       The end time. For current time use (new Date(System.currentTimeMillis()))
     * @param metrics   One or more metric names to request ex. "CPUUtilization"
     * @return          The average through the timespan as a double
     */
    public double calculateAverage(String dbID, Date start, Date end, String metric){
        GetMetricStatisticsResult result = getMetricStatistics(dbID, start, end, metric);
        List<Datapoint> dataPoints = result.getDatapoints();
        double averageSum = 0;

        //It's usually empty when the start and end times are too close together
        if(dataPoints.isEmpty()){
            return 0;
        }

        for(Datapoint point: dataPoints) {
            averageSum += point.getAverage();
        }

        return averageSum / dataPoints.size();
    }
}