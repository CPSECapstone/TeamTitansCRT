package webHandler;

import com.amazonaws.SdkClientException;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.cloudwatch.AmazonCloudWatch;
import com.amazonaws.services.cloudwatch.AmazonCloudWatchClientBuilder;


public class CloudWatchManager {

    AmazonCloudWatch cw;

    public CloudWatchManager() {
        this.cw = getConnection();
    }

    public AmazonCloudWatch getConnection() throws SdkClientException{
        try {
            BasicAWSCredentials awsCredentials = new BasicAWSCredentials("access_key", "secret_key");
            AmazonCloudWatch cloudWatch = AmazonCloudWatchClientBuilder.standard()
                    .withCredentials(new AWSStaticCredentialsProvider(awsCredentials))
                    .withRegion(Regions.US_WEST_2)
                    .build();

            return cloudWatch;
        } catch (SdkClientException e) {
            e.printStackTrace();
        }

        return null;
    }
}