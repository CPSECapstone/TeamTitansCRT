package app.cli;

import app.models.Capture;
import app.models.Replay;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ResourceCLI extends CLI {

    public static List<String> rds(String region) throws IOException, RuntimeException {
        String rdsURLString = urlString + "/resource/rds/" + region;
        String rdsResponse = completeGET(rdsURLString);

        return convertToListString(rdsResponse);
    }

    public static List<String> s3(String region) throws IOException, RuntimeException {
        String s3URLString = urlString + "/resource/s3/" + region;
        String s3Response = completeGET(s3URLString);

        return convertToListString(s3Response);
    }

    public static List<String> regions() throws IOException, RuntimeException {
        String regionUrl = urlString + "/resource/regions";
        String regionResponse = completeGET(regionUrl);

        return convertToListString(regionResponse);
    }

    public static List<Capture> captures() throws IOException, RuntimeException {
        String captureURLString = urlString + "resource/captures";
        String captureListString = completeGET(captureURLString);
        return convertToListCaptures(captureListString);
    }

    public static List<Capture> captures(String status) throws IOException, RuntimeException {
        String captureURLString = urlString + "resource/captures/" + status;
        String captureListString = completeGET(captureURLString);
        return convertToListCaptures(captureListString);
    }

    public static List<Replay> replays() throws IOException, RuntimeException {
        String replayURLString = urlString + "resource/replays";
        String replayListString = completeGET(replayURLString);
        return convertToListReplays(replayListString);
    }

    public static List<Replay> replays(String status) throws IOException, RuntimeException {
        String replayURLString = urlString + "resource/replays/" + status;
        String replayListString = completeGET(replayURLString);
        return convertToListReplays(replayListString);
    }


    public static void main(String[] args) {
        try {
            System.out.println("Get rds: " + rds("us-west-1"));
            System.out.println("Get S3: " + s3("us-west-1"));
            System.out.println("Get Captures: " + captures());
            System.out.println("Get Captures with Status: " + captures("Finished"));
            System.out.println("Get Replays: " + replays());
            System.out.println("Get Replays with Status: " + replays("Finished"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
