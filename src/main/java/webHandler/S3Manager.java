package webHandler;

import java.io.File;
import java.io.InputStream;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.InstanceProfileCredentialsProvider;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.S3Object;


public class S3Manager {

    private AmazonS3 s3Client;

    public S3Manager()
    {
        this.s3Client = (AmazonS3ClientBuilder.standard())
                .withRegion("us-west-2")
                .withCredentials(InstanceProfileCredentialsProvider.getInstance()).build();
    }

    public void uploadFile(String bucketName, String fileName, File file)
    {
       try
       {
           System.out.println("Starting the upload of a file to S3.");
           s3Client.putObject(new PutObjectRequest(bucketName, fileName, file));
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
}
