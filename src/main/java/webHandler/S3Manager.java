package webHandler;

import java.io.*;
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
import com.amazonaws.util.IOUtils;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class S3Manager {
    private AmazonS3 s3Client;

    public S3Manager() {
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

    public void uploadFile(String bucketName, String fileName, InputStream file, ObjectMetadata metadata) {
       try {
           System.out.println("Starting the upload of a file to S3.");
           s3Client.putObject(new PutObjectRequest(bucketName, fileName, file, metadata));
           System.out.println("Uploaded file to S3");
       } catch (AmazonServiceException ase) {
           System.out.println("Caught an AmazonServiceException");
           System.out.println("Error Message: " + ase.getMessage());
           System.out.println("Error Code: " + ase.getErrorCode());
       } catch (AmazonClientException ace) {
           System.out.println("Caught an AmazonClientException");
           System.out.println("Error Message: " + ace.getMessage());
       }
    }

    public void uploadFile(String bucketName, String fileName, File file)
    {
        try {
            System.out.println("Starting the upload of a file to S3.");
            s3Client.putObject(new PutObjectRequest(bucketName, fileName, file));
            System.out.println("Uploaded file to S3");
        } catch (AmazonServiceException ase) {
            System.out.println("Caught an AmazonServiceException");
            System.out.println("Error Message: " + ase.getMessage());
            System.out.println("Error Code: " + ase.getErrorCode());
        } catch (AmazonClientException ace) {
            System.out.println("Caught an AmazonClientException");
            System.out.println("Error Message: " + ace.getMessage());
        }
    }

    public InputStream getFile(String bucketName, String fileName) {
        InputStream dataStream = null;
        try {
            System.out.println("Downloading an object");
            S3Object s3Object = s3Client.getObject(new GetObjectRequest(
                    bucketName, fileName));
            dataStream = s3Object.getObjectContent();
        } catch (AmazonServiceException ase) {
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

    public String getFileString(String bucketName, String fileName)
    {
        InputStream dataStream = null;
        BufferedReader buffer = null;
        try {
            System.out.println("Downloading an object");
            S3Object s3Object = s3Client.getObject(new GetObjectRequest(
                    bucketName, fileName));
            dataStream = s3Object.getObjectContent();
            buffer = new BufferedReader(new InputStreamReader(dataStream));

        } catch (AmazonServiceException ase) {
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
        return buffer.lines().collect(Collectors.joining("\n"));
    }

    public List<String> getS3Buckets() {
        return s3Client.listBuckets().stream()
                .map(x->x.getName())
                .collect(Collectors.toList());
    }
  
    /**
     * @param bucketName S3 bucket name. ex: teamtitans-test-mycrt
     * @param fileName file to search for
     * @param filePath where the file will be saved. ex: src/main/resources/filename.tmp
     * @return void it will create a new file and write the inputstream's content to it
     */
    public void downloadFileLocally (String bucketName, String fileName, String filePath) throws IOException {
        //this is the datastream of the file being downloaded
        InputStream inStream = getFile(bucketName, fileName);

        //creates the new local file and creates an outputstream for it
        File newFile = new File(filePath);
        OutputStream outStream = new FileOutputStream(newFile);

        //this will copy the content of the inputstream to the new fiel
        IOUtils.copy(inStream,outStream);

        //close after use
        inStream.close();
        outStream.close();
    }
}
