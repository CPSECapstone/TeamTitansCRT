package app.cli;
import app.models.Capture;
import org.json.simple.JSONObject;

import java.io.IOException;
import java.util.Date;
import java.util.List;

public class CaptureCLI extends CLI {

    public static void start(String id, String rdsRegion, String rds, String s3Region, String s3, Date startTime,
                                 Date endTime, long transactionLimit, long fileSizeLimit, List<String> filterStatements,
                                 List<String> filterUsers) {
        try {
            String captureStartURL = urlString + "capture/start";

            // build json post data
            JSONObject object = new JSONObject();
            object.put("id", id);
            object.put("s3Region", s3Region);
            object.put("s3", s3);
            object.put("rdsRegion", rdsRegion);
            object.put("rds", rds);
            if (startTime != null) {
                object.put("startTime", startTime.getTime());
            }
            if (endTime != null) {
                object.put("endTime", endTime.getTime());
            }
            if (transactionLimit > 0) {
                object.put("transactionLimit", transactionLimit);
            }
            if (fileSizeLimit > 0) {
                object.put("fileSizeLimit", fileSizeLimit);
            }
            if (filterStatements != null) {
                object.put("filterStatements", filterStatements);
            }
            if (filterUsers != null) {
                object.put("filterUsers", filterUsers);
            }
            System.out.println("Sending request to start capture: " + id);
            completePOST(captureStartURL, object.toString());
            System.out.println("Successfully started capture: " + id);
        } catch (Exception e) {
            System.out.println("Failed to start capture: " + id);
        }

    }

    public static void stop(String id) {
        try {
            String captureStopURL = urlString + "capture/stop";
            JSONObject object = new JSONObject();
            object.put("id", id);
            System.out.println("Sending request to stop capture: " + id);
            completePOST(captureStopURL, object.toString());
            System.out.println("Successfully stopped capture: " + id);
        } catch (Exception e) {
            System.out.println("Failed to stop capture: " + id);
        }
    }

    public static List<Capture> status() throws IOException, RuntimeException {
        String captureStatusURL = urlString + "capture/status";
        String captureListString = completeGET(captureStatusURL);
        return convertToListCaptures(captureListString);
    }

    // TODO: Update servlet to reflect POST, take in ID, and grab capture from capture list
    public static void delete(String id) {
        try {
            String captureDeleteURL = urlString + "capture/stop";
            JSONObject object = new JSONObject();
            object.put("id", id);
            System.out.println("Sending request to delete capture: " + id);
            completePOST(captureDeleteURL, object.toString());
            System.out.println("Successfully deleted capture: " + id);
        } catch (Exception e) {
            System.out.println("Failed to delete capture: " + id);
        }
    }

     // TODO: Capture update CLI function
}
