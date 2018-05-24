package app.cli;

import org.json.simple.JSONObject;

import java.io.IOException;
import java.util.Date;
import java.util.List;

public class ReplayCLI extends CLI {

    public static String start(String id, String rdsRegion, String rds, String s3Region, String s3,
                                   String replayType, String captureId, Date startTime, Date endTime,
                                   long transactionLimit, List<String> filterStatements,
                                   List<String> filterUsers, String username, String password) throws IOException, RuntimeException {
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
        return completePOST(replayStartURL, object.toString());
    }

    public static String stop(String id) throws IOException, RuntimeException {
        String captureStopURL = urlString + "replay/stop";
        JSONObject object = new JSONObject();
        object.put("id", id);

        return completePOST(captureStopURL, object.toString());
    }

    public static String delete(String id) throws IOException, RuntimeException {
        String captureStopURL = urlString + "replay/delete";
        JSONObject object = new JSONObject();
        object.put("id", id);

        return completePOST(captureStopURL, object.toString());
    }
}
