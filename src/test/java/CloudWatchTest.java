import com.amazonaws.SdkClientException;
import com.amazonaws.services.cloudwatch.model.Datapoint;
import com.amazonaws.services.cloudwatch.model.GetMetricStatisticsResult;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.http.ResponseEntity;
import webHandler.CloudWatchManager;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import static org.junit.Assert.*;

public class CloudWatchTest {

    private static GetMetricStatisticsResult result;
    private static String label = "Test";
    private static Date timestamp = new Date();
    private static Double average = 1.1;
    private static String unit = "Percent";

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        Collection<Datapoint> dataPoints = new ArrayList<>();
        Datapoint point = new Datapoint();
        point.setTimestamp(timestamp);
        point.setAverage(average);
        point.setUnit(unit);
        dataPoints.add(point);

        result = new GetMetricStatisticsResult();
        result.setLabel("Test");
        result.setDatapoints(dataPoints);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testConvertMetricStatisticsToJson() throws SdkClientException {
        JSONObject json = CloudWatchManager.convertMetricStatisticsToJson(result);
        JSONObject testJson = new JSONObject();

        JSONArray dataPoints = new JSONArray();
        JSONObject jsonPoint = new JSONObject();
        testJson.put("Metric", label);
        jsonPoint.put("Timestamp", timestamp.getTime());
        jsonPoint.put("Average", average);
        jsonPoint.put("Unit", unit);
        dataPoints.add(jsonPoint);
        testJson.put("DataPoints", dataPoints);

        assertEquals(testJson.toJSONString(), json.toJSONString());

        ArrayList<GetMetricStatisticsResult> resultArr = new ArrayList<>();
        resultArr.add(result);

        JSONArray arr = CloudWatchManager.convertMetricStatisticsToJson(resultArr);
        JSONArray testArr = new JSONArray();
        testArr.add(testJson);

        assertEquals(testArr.toJSONString(), arr.toJSONString());
    }
}