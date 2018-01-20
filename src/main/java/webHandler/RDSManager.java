package webHandler;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.AmazonWebServiceResult;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.InstanceProfileCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.rds.AmazonRDSClientBuilder;
import com.amazonaws.services.rds.AmazonRDS;
import com.amazonaws.services.rds.model.DownloadDBLogFilePortionRequest;
import com.amazonaws.services.rds.model.DownloadDBLogFilePortionResult;
import com.amazonaws.services.rds.model.DBLogFileNotFoundException;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.*;
import java.nio.charset.StandardCharsets;


public class RDSManager extends AmazonWebServiceResult{

    private AmazonRDS rdsClient;

    public RDSManager() {

        JSONParser parser = new JSONParser();

        try {
            // Running outside EC2 Instance:
            FileReader reader = new FileReader(new File(".privateKeys"));
            Object obj = parser.parse(reader);
            JSONObject jsonObject = (JSONObject) obj;

            String accessKey = (String) jsonObject.get("accessKey");
            String secretKey = (String) jsonObject.get("secretKey");

            BasicAWSCredentials awsCredentials = new BasicAWSCredentials(accessKey, secretKey);

            this.rdsClient = AmazonRDSClientBuilder.standard()
                    .withCredentials(new AWSStaticCredentialsProvider(awsCredentials))
                    .withRegion(Regions.US_WEST_1)
                    .build();
        } catch (Exception e) {
            // Running inside EC2 Instance:
            this.rdsClient = AmazonRDSClientBuilder.standard()
                    .withCredentials(InstanceProfileCredentialsProvider.getInstance())
                    .withRegion(Regions.US_WEST_1)
                    .build();
        }
    }

    public InputStream downloadLog(String DBInstance, String logFile){
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