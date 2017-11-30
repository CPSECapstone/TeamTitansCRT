package webHandler;

import java.util.*;
import com.amazonaws.SdkClientException;
import com.amazonaws.auth.InstanceProfileCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.cloudwatch.AmazonCloudWatch;
import com.amazonaws.services.cloudwatch.AmazonCloudWatchClientBuilder;
import com.amazonaws.services.cloudwatch.model.*;

import java.util.Date;


public class CloudWatchManager {

    AmazonCloudWatch cw;

    public CloudWatchManager() {
        this.cw = getConnection();
    }

    public AmazonCloudWatch getConnection() throws SdkClientException {
        try {
            AmazonCloudWatch cloudWatch = AmazonCloudWatchClientBuilder.standard()
                    .withCredentials(InstanceProfileCredentialsProvider.getInstance())
                    .withRegion(Regions.US_WEST_2)
                    .build();

            return cloudWatch;
        } catch (SdkClientException e) {
            e.printStackTrace();
        }

        return null;
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
                .withUnit(StandardUnit.Seconds);


        return cw.getMetricStatistics(request);
    }
}