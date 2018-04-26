package app.cli;

import org.json.simple.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.ListIterator;

public class CLI {

    protected static final String USER_AGENT = "Mozilla/5.0";
    //protected static final String urlString = "http://localhost:8080/";
    protected static final String urlString = "http://54.176.147.59:8080/";

    protected static void presentListOptions(List<String> options) {
        ListIterator<String> it = options.listIterator();
        while (it.hasNext()) {
            System.out.println(it.nextIndex() + ": " + it.next());
        }
    }

    private static HttpURLConnection createConnection(String url) throws Exception {
        URL urlObj = new URL(url);
        return (HttpURLConnection) urlObj.openConnection();
    }

    private static HttpURLConnection createConnectionGET(String url) throws Exception {
        HttpURLConnection con = createConnection(url);
        con.setRequestMethod("GET"); //GET or POST
        con.setRequestProperty("User-Agent", USER_AGENT);
        return con;
    }

    private static HttpURLConnection createConnectionPOST(String url) throws Exception {
        HttpURLConnection con = createConnection(url);
        con.setRequestMethod("POST"); //GET or POST
        con.setRequestProperty("User-Agent", USER_AGENT);
        con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
        con.setRequestProperty("Content-Type","application/json");
        System.out.println("Creating 'POST' request to URL : " + url);
        return con;
    }

    private static void sendConnectionPOST(HttpURLConnection con, String data) throws Exception {
        System.out.println("Sending post data : " + data);
        con.setDoOutput(true);
        DataOutputStream wr = new DataOutputStream(con.getOutputStream());
        wr.writeBytes(data);
        wr.flush();
        wr.close();
    }

    private static StringBuffer readResponse(HttpURLConnection con) throws Exception {
        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
        String output;
        StringBuffer response = new StringBuffer();

        // reading in data
        while ((output = in.readLine()) != null) {
            response.append(output);
        }
        in.close();

        return response;
    }

    protected static String completeGET(String url) throws Exception {
        HttpURLConnection con = createConnectionGET(url);

        int responseCode = con.getResponseCode(); // HTTP Response Code

        // printing request properties
        System.out.println("Response Code : " + responseCode);
        System.out.println(con.getResponseMessage());

        StringBuffer response = readResponse(con);

        return response.toString();
    }

    protected static String completePOST(String url, String data) throws Exception {
        HttpURLConnection con = createConnectionPOST(url);

        sendConnectionPOST(con, data);

        int responseCode = con.getResponseCode(); // HTTP Response Code

        // printing request properties
        System.out.println("Response Code : " + responseCode);
        System.out.println(con.getResponseMessage());

        StringBuffer response = readResponse(con);

        return response.toString();
    }
}
