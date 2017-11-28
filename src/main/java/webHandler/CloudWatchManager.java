package webHandler;

import com.amazonaws.SdkClientException;
import com.amazonaws.auth.InstanceProfileCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.cloudwatch.AmazonCloudWatch;
import com.amazonaws.services.cloudwatch.AmazonCloudWatchClientBuilder;
import com.amazonaws.services.cloudwatch.model.GetMetricStatisticsRequest;
import com.amazonaws.services.cloudwatch.model.GetMetricStatisticsResult;
import com.amazonaws.services.cloudwatch.model.StandardUnit;
import com.amazonaws.services.cloudwatch.model.Statistic;

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
     * @param metric The name of the metric of interest
     * @param start  The start time
     * @param end    The end time
     * @return       Returns GetMetricStatisticsResult - for more information see http://docs.aws.amazon.com/AWSJavaSDK/latest/javadoc/com/amazonaws/services/cloudwatch/model/GetMetricStatisticsResult.html#withUnit-java.lang.String-
     */
    public GetMetricStatisticsResult getMetricStatistics(String metric, Date start, Date end) {
        GetMetricStatisticsRequest request = new GetMetricStatisticsRequest()
                .withNamespace("AWS/RDS")
                .withMetricName(metric)
                .withStartTime(start)
                .withEndTime(end)
                .withStatistics(Statistic.Sum, Statistic.Average)
                .withUnit(StandardUnit.Seconds);


        return cw.getMetricStatistics(request);
    }
}