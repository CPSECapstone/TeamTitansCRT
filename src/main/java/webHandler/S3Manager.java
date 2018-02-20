package webHandler;

import java.io.File;
import java.io.FileReader;
import java.io.InputStream;
import java.util.List;
import java.util.stream.Collectors;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.InstanceProfileCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;


public class S3Manager {

    private AmazonS3 s3Client;

    public S3Manager()
    {

        JSONParser parser = new JSONParser();

        try {
            // Running outside EC2 Instance:
            FileReader reader = new FileReader(new File(".privateKeys"));
            Object obj = parser.parse(reader);
            JSONObject jsonObject = (JSONObject) obj;

            String accessKey = (String) jsonObject.get("accessKey");
            String secretKey = (String) jsonObject.get("secretKey");

            BasicAWSCredentials awsCredentials = new BasicAWSCredentials(accessKey, secretKey);

            this.s3Client = AmazonS3ClientBuilder.standard()
                    .withCredentials(new AWSStaticCredentialsProvider(awsCredentials))
                    .withRegion(Regions.US_WEST_1)
                    .build();
        } catch (Exception e) {
            // Running inside EC2 Instance:
            this.s3Client = AmazonS3ClientBuilder.standard()
                    .withCredentials(InstanceProfileCredentialsProvider.getInstance())
                    .withRegion(Regions.US_WEST_1)
                    .build();
        }

    }

    public void uploadFile(String bucketName, String fileName, InputStream file, ObjectMetadata metadata)
    {
       try
       {
           System.out.println("Starting the upload of a file to S3.");
           s3Client.putObject(new PutObjectRequest(bucketName, fileName, file, metadata));
           System.out.println("Uploaded file to S3");
       } catch (AmazonServiceException ase)
       {
           System.out.println("Caught an AmazonServiceException");
           System.out.println("Error Message: " + ase.getMessage());
           System.out.println("Error Code: " + ase.getErrorCode());
       } catch (AmazonClientException ace)
       {
           System.out.println("Caught an AmazonClientException");
           System.out.println("Error Message: " + ace.getMessage());
       }
    }

    public InputStream getFile(String bucketName, String fileName) {
        InputStream dataStream = null;
        try
        {
            System.out.println("Downloading an object");
            S3Object s3Object = s3Client.getObject(new GetObjectRequest(
                    bucketName, fileName));
            dataStream = s3Object.getObjectContent();
        } catch (AmazonServiceException ase)
        {
            System.out.println("Caught an AmazonServiceException");
            System.out.println("Error Message:    " + ase.getMessage());
            System.out.println("HTTP Status Code: " + ase.getStatusCode());
            System.out.println("AWS Error Code:   " + ase.getErrorCode());
            System.out.println("Error Type:       " + ase.getErrorType());
            System.out.println("Request ID:       " + ase.getRequestId());
        } catch (AmazonClientException ace) {
            System.out.println("Caught an AmazonClientException");
            System.out.println("Error Message: " + ace.getMessage());
        }
        return dataStream;
    }

    public List<String> getS3Buckets() {
        return s3Client.listBuckets().stream()
                .map(x->x.getName())
                .collect(Collectors.toList());
    }
}
