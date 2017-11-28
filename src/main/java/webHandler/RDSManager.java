package webHandler;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.AmazonWebServiceResult;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.InstanceProfileCredentialsProvider;
import com.amazonaws.services.rds.AmazonRDSClientBuilder;
import com.amazonaws.services.rds.AmazonRDS;
import com.amazonaws.services.rds.model.DownloadDBLogFilePortionRequest;
import com.amazonaws.services.rds.model.DownloadDBLogFilePortionResult;
import com.amazonaws.services.rds.model.DBLogFileNotFoundException;
import com.amazonaws.services.s3.model.ObjectMetadata;

import java.io.*;
import java.nio.charset.StandardCharsets;


public class RDSManager extends AmazonWebServiceResult{

    private static AmazonRDS rdsClient;

    public RDSManager() {
        this.rdsClient = AmazonRDSClientBuilder.standard().withRegion("us-west-1").withCredentials(InstanceProfileCredentialsProvider.getInstance()).build();

        /* Testing outside of EC2 Instance:
        BasicAWSCredentials awsCredentials = new BasicAWSCredentials("accesskey", "secretkey");
        this.rdsClient = AmazonRDSClientBuilder.standard().withRegion("us-west-1").withCredentials(new AWSStaticCredentialsProvider(awsCredentials)).build();*/
    }

    public static InputStream downloadLog(String DBInstance, String logFile){
        try {
            DownloadDBLogFilePortionRequest request = new DownloadDBLogFilePortionRequest().withDBInstanceIdentifier(DBInstance).withLogFileName(
                     logFile);
            DownloadDBLogFilePortionResult response = rdsClient.downloadDBLogFilePortion(request);
            InputStream inResponse = new ByteArrayInputStream(response.getLogFileData().getBytes(StandardCharsets.UTF_8.name()));

            return inResponse;

        } catch (DBLogFileNotFoundException e) {
            e.printStackTrace();
        } catch (AmazonServiceException ase) {
            System.out.println("Caught an AmazonServiceException");
            System.out.println("Error Message: " + ase.getMessage());
            System.out.println("Error Code: " + ase.getErrorCode());
        } catch (AmazonClientException ace) {
            System.out.println("Caught an AmazonClientException");
            System.out.println("Error Message: " + ace.getMessage());
        } catch (UnsupportedEncodingException enc) {
            enc.printStackTrace();
        }
        return null;
    }
}