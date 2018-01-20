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
import org.json.simple.parser.JSONParser;
import org.json.simple.JSONObject;

import java.util.Date;


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
}