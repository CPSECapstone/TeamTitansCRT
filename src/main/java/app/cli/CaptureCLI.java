package app.cli;
import app.models.Capture;
import org.json.simple.JSONObject;

import java.io.IOException;
import java.util.Date;
import java.util.List;

public class CaptureCLI extends CLI {

    public static String start(String id, String rdsRegion, String rds, String s3Region, String s3, Date startTime,
                                 Date endTime, long transactionLimit, long fileSizeLimit, List<String> filterStatements,
                                 List<String> filterUsers) throws IOException, RuntimeException {
        String captureStartURL = urlString + "capture/start";

        // build json post data
        JSONObject object = new JSONObject();
        object.put("id", id);
        object.put("s3Region", s3Region);
        object.put("s3", s3);
        object.put("rdsRegion", rdsRegion);
        object.put("rds", rds);
        if (startTime != null) {
            object.put("startTime", startTime);
        }
        if (endTime != null) {
            object.put("endTime", endTime);
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
        return completePOST(captureStartURL, object.toString());

    }

    public static String stop(String id) throws IOException, RuntimeException {
        String captureStopURL = urlString + "capture/stop";
        JSONObject object = new JSONObject();
        object.put("id", id);

        return completePOST(captureStopURL, object.toString());
    }

    public static List<Capture> status() throws IOException, RuntimeException {
        // TODO: Add JSON Parsing and return Capture Object
        String captureStatusURL = urlString + "capture/status";
        String captureListString = completeGET(captureStatusURL);
        return convertToListCaptures(captureListString);
    }

    // TODO: Update servlet to reflect POST, take in ID, and grab capture from capture list
    public static String delete(String id) throws IOException, RuntimeException {
        String captureDeleteURL = urlString + "capture/stop";
        JSONObject object = new JSONObject();
        object.put("id", id);

        return completePOST(captureDeleteURL, object.toString());
    }

     // TODO: Capture update CLI function
     /*
    public static String sendUpdate(String id) throws IOException, RuntimeException {

    }*/

    // this main method is only for testing
    /* If you'd like to run this main method, either run it directly or
    add the following lines to build.gradle

    apply plugin: "application"
    mainClassName="app/cli/CaptureCLI"

    run the main with

    gradle run
     */
    /*
    public static void main(String[] args) {
        try {
            System.out.println(status());
        } catch (Exception e)
        {
            e.printStackTrace();
        }
    }*/
}
