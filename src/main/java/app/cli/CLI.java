package app.cli;

import org.json.simple.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
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

    private static HttpURLConnection createConnection(String url) throws IOException {
        URL urlObj = new URL(url);
        return (HttpURLConnection) urlObj.openConnection();
    }

    private static HttpURLConnection createConnectionGET(String url) throws IOException {
        HttpURLConnection con = createConnection(url);
        con.setRequestMethod("GET"); //GET or POST
        con.setRequestProperty("User-Agent", USER_AGENT);
        return con;
    }

    private static HttpURLConnection createConnectionPOST(String url) throws IOException {
        HttpURLConnection con = createConnection(url);
        con.setRequestMethod("POST"); //GET or POST
        con.setRequestProperty("User-Agent", USER_AGENT);
        con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
        con.setRequestProperty("Content-Type","application/json");
        System.out.println("Creating 'POST' request to URL : " + url);
        return con;
    }

    private static void sendConnectionPOST(HttpURLConnection con, String data) throws IOException {
        System.out.println("Sending post data : " + data);
        con.setDoOutput(true);
        DataOutputStream wr = new DataOutputStream(con.getOutputStream());
        wr.writeBytes(data);
        wr.flush();
        wr.close();
    }

    private static String readResponse(HttpURLConnection con) throws IOException {
        BufferedReader in = null;
        StringBuffer response;
        try {
            in = new BufferedReader(
                    new InputStreamReader(con.getInputStream()));
            String output;
            response = new StringBuffer();

            // reading in data
            while ((output = in.readLine()) != null) {
                response.append(output);
            }
        } finally {
            if (in != null) {
                in.close();
            }
        }

        return response != null ? response.toString() : "";
    }

    protected static String completeGET(String url) throws IOException, RuntimeException {
        HttpURLConnection con = createConnectionGET(url);

        int responseCode = con.getResponseCode(); // HTTP Response Code

        // printing request properties
        System.out.println("Response Code : " + responseCode);
        System.out.println(con.getResponseMessage());

        if (responseCode >= 400) {
            throw new RuntimeException();
        }

        return readResponse(con);
    }

    protected static String completePOST(String url, String data) throws IOException, RuntimeException {
        HttpURLConnection con = createConnectionPOST(url);

        sendConnectionPOST(con, data);

        int responseCode = con.getResponseCode(); // HTTP Response Code

        if (responseCode >= 400) {
            throw new RuntimeException();
        }

        // printing request properties
        System.out.println("Response Code : " + responseCode);
        System.out.println(con.getResponseMessage());

        return readResponse(con);
    }
}
