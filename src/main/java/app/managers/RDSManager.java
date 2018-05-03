package app.managers;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.AmazonWebServiceResult;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.InstanceProfileCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.rds.AmazonRDS;
import com.amazonaws.services.rds.AmazonRDSClientBuilder;
import com.amazonaws.services.rds.model.*;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.File;
import java.io.FileReader;
import java.util.List;
import java.util.stream.Collectors;


public class RDSManager extends AmazonWebServiceResult{

    private AmazonRDS rdsClient;

    public RDSManager(String region) {

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
                    .withRegion(Regions.valueOf(region))
                    .build();
        } catch (Exception e) {
            // Running inside EC2 Instance:
            this.rdsClient = AmazonRDSClientBuilder.standard()
                    .withCredentials(InstanceProfileCredentialsProvider.getInstance())
                    .withRegion(Regions.valueOf(region))
                    .build();
        }
    }

    public String downloadLog(String DBInstance, String logFile){
        try {
            DownloadDBLogFilePortionRequest request = new DownloadDBLogFilePortionRequest().withDBInstanceIdentifier(DBInstance).withLogFileName(
                     logFile);
            DownloadDBLogFilePortionResult response = rdsClient.downloadDBLogFilePortion(request);

            return response.getLogFileData();

        } catch (DBLogFileNotFoundException e) {
            e.printStackTrace();
        } catch (AmazonServiceException ase) {
            System.out.println("Caught an AmazonServiceException");
            System.out.println("Error Message: " + ase.getMessage());
            System.out.println("Error Code: " + ase.getErrorCode());
        } catch (AmazonClientException ace) {
            System.out.println("Caught an AmazonClientException");
            System.out.println("Error Message: " + ace.getMessage());
        }
        return null;
    }

    public List<String> getRDSInstances() {
        return rdsClient.describeDBInstances().getDBInstances().stream()
                .map(x->x.getDBInstanceIdentifier())
                .collect(Collectors.toList());
    }

    public String getRDSInstanceUrl(String id) {
        DescribeDBInstancesRequest request = new DescribeDBInstancesRequest().withDBInstanceIdentifier(id);

        List<DBInstance> instanceList = rdsClient.describeDBInstances(request).getDBInstances();

        if (instanceList.isEmpty()) {
            return null;
        }

        DBInstance instance = instanceList.get(0);
        return instance.getEndpoint().getAddress() + ":" + instance.getEndpoint().getPort();
    }
}
