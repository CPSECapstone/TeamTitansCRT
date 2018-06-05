package app.cli;

import app.models.Capture;
import app.servlets.AnalysisServlet;
import app.models.MetricRequest;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.util.Date;
import java.util.List;

public class AnalysisCLI extends CLI{
    public static String average(Capture capture, String... metrics)throws RuntimeException {
        String rds = capture.getRds();
        String region = capture.getRdsRegion();
        Date startTime = capture.getStartTime();
        Date now = new Date(System.currentTimeMillis());
        MetricRequest request = new MetricRequest(rds, region, startTime, now, metrics);

        AnalysisServlet analysisServlet = new AnalysisServlet();
        ResponseEntity<List<Double>> dataList = analysisServlet.calculateAverages(request);

        //format output
        String output = "";
        for(int i = 0; i < metrics.length; i++) {
            String line = String.format("%-20s= %-10f \n", metrics[i], dataList.getBody().get(i));
            output = output + line;
        }
        return output;
    }

    public static void main(String[] args) {
        try {
            String[] metrics = {"CPUUtilization", "WriteThroughput"};
            Capture capture = new Capture();
            capture.setRds("testdb");
            capture.setRdsRegion("US_WEST_1");
            Date startTime = new Date(System.currentTimeMillis());
            startTime.setTime(startTime.getTime()-3600*200);
            capture.setStartTime(startTime);
            System.out.println(average(capture, metrics));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
