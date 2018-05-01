package app.cli;

import java.io.IOException;

public class ResourceCLI extends CLI {

    public static String rds() throws IOException, RuntimeException {
        String rdsURLString = urlString + "/resource/rds";
        return completeGET(rdsURLString);
    }

    public static String s3() throws IOException, RuntimeException {
        String s3URLString = urlString + "/resource/s3";
        return completeGET(s3URLString);
    }

    public static String captures() throws IOException, RuntimeException {
        String captureURLString = urlString + "resource/captures";
        return completeGET(captureURLString);
    }

    public static String captures(String status) throws IOException, RuntimeException {
        String captureURLString = urlString + "resource/captures/" + status;
        return completeGET(captureURLString);
    }

    public static String replays() throws IOException, RuntimeException {
        String replayURLString = urlString + "resource/replays";
        return completeGET(replayURLString);
    }

    public static String replays(String status) throws IOException, RuntimeException {
        String replayURLString = urlString + "resource/replays/" + status;
        return completeGET(replayURLString);
    }

    /*
    public static void main(String[] args) {
        try {
            System.out.println("Get rds: " + rds());
            System.out.println("Get S3: " + s3());
            System.out.println("Get Captures: " + captures());
            System.out.println("Get Captures with Status: " + captures("Finished"));
            System.out.println("Get Replays: " + replays());
            System.out.println("Get Replays with Status: " + replays("Finished"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    } */
}
