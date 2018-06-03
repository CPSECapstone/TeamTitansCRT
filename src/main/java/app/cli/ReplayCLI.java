package app.cli;

import org.json.simple.JSONObject;

import java.io.IOException;
import java.util.Date;
import java.util.List;

public class ReplayCLI extends CLI {

    public static void start(String id, String rdsRegion, String rds, String s3Region, String s3,
                                   String replayType, String captureId, Date startTime, Date endTime,
                                   long transactionLimit, List<String> filterStatements,
                                   List<String> filterUsers, String username, String password) {
        try {
            String replayStartURL = urlString + "replay/start";
            // build json post data
            JSONObject object = new JSONObject();
            object.put("id", id);
            object.put("s3Region", s3Region);
            object.put("s3", s3);
            object.put("rdsRegion", rdsRegion);
            object.put("rds", rds);
            object.put("replayType", replayType);
            object.put("captureId", captureId);
            JSONObject dbInfo = new JSONObject();
            dbInfo.put("database", rds);
            dbInfo.put("region", rdsRegion);
            dbInfo.put("username", username);
            dbInfo.put("password", password);
            object.put("databaseInfo", dbInfo);

            if (startTime != null) {
                object.put("startTime", startTime.getTime());
            }
            if (endTime != null) {
                object.put("endTime", endTime.getTime());
            }
            if (transactionLimit > 0) {
                object.put("transactionLimit", transactionLimit);
            }
            if (filterStatements != null) {
                object.put("filterStatements", filterStatements);
            }
            if (filterUsers != null) {
                object.put("filterUsers", filterUsers);
            }
            System.out.println("Sending request to start replay: " + id);
            completePOST(replayStartURL, object.toString());
            System.out.println("Replay " + id + " successfully started");
        } catch (Exception e) {
            System.out.println("Failed to start replay: " + id);
        }
    }

    public static void stop(String id) {
        try {
            String captureStopURL = urlString + "replay/stop";
            JSONObject object = new JSONObject();
            object.put("id", id);
            System.out.println("Sending request to stop replay: " + id);
            completePOST(captureStopURL, object.toString());
            System.out.println("Successfully stopped replay: " + id);
        } catch (Exception e) {
            System.out.println("Failed to stop replay: " + id);
        }
    }

    // TODO: Update servlet to reflect POST method, take in ID, and grab replay from replay list
    public static void delete(String id) {
        try {
            String captureStopURL = urlString + "replay/delete";
            JSONObject object = new JSONObject();
            object.put("id", id);
            System.out.println("Sending request to delete replay: " + id);
            completePOST(captureStopURL, object.toString());
            System.out.println("Successfully deleted replay: " + id);
        } catch (Exception e) {
            System.out.println("Failed to delete replay: " + id);
        }
    }
}
