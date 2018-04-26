package app.cli;
import org.json.simple.JSONObject;

import java.util.Date;
import java.util.List;

public class CaptureCLI extends CLI {

    public static String sendStart(String id, String rdsRegion, String rds, String s3Region, String s3, Date startTime,
                                 Date endTime, long transactionLimit, long fileSizeLimit, List<String> filterStatements,
                                 List<String> filterUsers) throws Exception {
        String startString = urlString + "capture/start";

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
        return completePOST(startString, object.toString());
    }

    public static void resourceTestFunc() throws Exception {
        String urlString2 = urlString + "resource/rds";
        System.out.println(completeGET(urlString2));
    }

    // this main method is only for testing
    /* If you'd like to run this main method, either run it directly or
    add the following lines to build.gradle

    apply plugin: "application"
    mainClassName="app/cli/CaptureCLI"

    run the main with

    gradle run
     */
    public static void main(String[] args) {
        try {
            resourceTestFunc();
        } catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
