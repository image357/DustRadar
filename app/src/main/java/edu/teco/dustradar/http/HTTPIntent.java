package edu.teco.dustradar.http;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

public class HTTPIntent extends IntentService {

    private final static String TAG = HTTPIntent.class.getSimpleName();

    // actions
    private static final String ACTION_HTTP_POST_JSON = "edu.teco.dustradar.http.action.post.json";
    private static final String ACTION_HTTP_GET_JSON = "edu.teco.dustradar.http.action.get.json";

    // parameters
    public static final String EXTRA_URL = "edu.teco.dustradar.http.extra.url";
    public static final String EXTRA_JSON = "edu.teco.dustradar.http.extra.json";
    public static final String EXTRA_RESULT = "edu.teco.dustradar.http.extra.result";
    public static final String EXTRA_BROADCAST = "edu.teco.dustradar.http.extra.broadcast";

    // broadcasts
    public final static String BROADCAST_HTTP_POST_SUCCESS = "BROADCAST_HTTP_POST_SUCCESS";
    public final static String BROADCAST_HTTP_POST_FAILURE = "BROADCAST_HTTP_POST_FAILURE";

    private final static List<String> allBroadcasts = Arrays.asList(
            BROADCAST_HTTP_POST_SUCCESS,
            BROADCAST_HTTP_POST_FAILURE
    );


    // private members

    private HttpURLConnection http = null;


    // constructors

    public HTTPIntent() {
        super("HTTPIntent");
        setIntentRedelivery(true);
    }


    // helper methods

    public static void Post(Context context, String returnBroadcast, String url, String json) {
        Intent intent = new Intent(context, HTTPIntent.class);
        intent.setAction(ACTION_HTTP_POST_JSON);
        intent.putExtra(EXTRA_URL, url);
        intent.putExtra(EXTRA_JSON, json);
        intent.putExtra(EXTRA_BROADCAST, returnBroadcast);
        context.startService(intent);
    }


    public static void GetJson(Context context, String returnBroadcast, String url) {
        Intent intent = new Intent(context, HTTPIntent.class);
        intent.setAction(ACTION_HTTP_GET_JSON);
        intent.putExtra(EXTRA_URL, url);
        intent.putExtra(EXTRA_BROADCAST, returnBroadcast);
        context.startService(intent);
    }


    // event handlers

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_HTTP_POST_JSON.equals(action)) {
                final String url = intent.getStringExtra(EXTRA_URL);
                final String json = intent.getStringExtra(EXTRA_JSON);
                final String broadcast = intent.getStringExtra(EXTRA_BROADCAST);
                handlePostJson(broadcast, url, json);
            } else if (ACTION_HTTP_GET_JSON.equals(action)) {
                final String url = intent.getStringExtra(EXTRA_URL);
                final String broadcast = intent.getStringExtra(EXTRA_BROADCAST);
                handleGetJson(broadcast, url);
            }
        }
    }


    // action handlers

    private void handlePostJson(String broadcast, String url, String json) {
        String result;
        Intent intent;
        try {
            initHttp(url);
        }
        catch (Exception e) {
            result = "Exception: initHttp()";
            Log.e(TAG, result);

            intent = new Intent(BROADCAST_HTTP_POST_FAILURE);
            intent.putExtra(EXTRA_BROADCAST, broadcast);
            intent.putExtra(EXTRA_RESULT, result);
            sendBroadcast(intent);
            return;
        }

        try {
            byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
            http.setRequestMethod("POST");
            http.setDoInput(true);
            http.setDoOutput(true);
            http.setConnectTimeout(3000);
            http.setAllowUserInteraction(false);
            http.setFixedLengthStreamingMode(bytes.length);
            http.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            http.setRequestProperty("Content-Encoding", "charset=UTF-8");
            http.setRequestProperty("Accept", "application/json");
            http.connect();

            try (OutputStream os = http.getOutputStream()) {
                os.write(bytes);
                String response;

                switch (http.getResponseCode()) {
                    case 200:
                    case 201:
                        response = getHTTPResult(http.getInputStream());
                        result = "POST url success: " + url + "; Response: " + response;
                        Log.i(TAG, result);

                        intent = new Intent(BROADCAST_HTTP_POST_SUCCESS);
                        intent.putExtra(EXTRA_BROADCAST, broadcast);
                        sendBroadcast(intent);
                        break;

                    default:
                        response = getHTTPResult(http.getErrorStream());
                        result = "POST url failure: " + url + "; Response code != 200: " + response;
                        Log.w(TAG, result);

                        intent = new Intent(BROADCAST_HTTP_POST_FAILURE);
                        intent.putExtra(EXTRA_BROADCAST, broadcast);
                        intent.putExtra(EXTRA_RESULT, result);
                        sendBroadcast(intent);
                }
            }
            catch (IOException e) {
                result = "IOException for url: " + url;
                Log.e(TAG, result);

                intent = new Intent(BROADCAST_HTTP_POST_FAILURE);
                intent.putExtra(EXTRA_BROADCAST, broadcast);
                intent.putExtra(EXTRA_RESULT, result);
                sendBroadcast(intent);
            }
        }
        catch (Exception e) {
            result = "Exception for url: " + url;
            Log.e(TAG, result);

            intent = new Intent(BROADCAST_HTTP_POST_FAILURE);
            intent.putExtra(EXTRA_BROADCAST, broadcast);
            intent.putExtra(EXTRA_RESULT, result);
            sendBroadcast(intent);
        }
        finally {
            http.disconnect();
        }
    }


    private void handleGetJson(String broadcast, String url) {
        // TODO: Handle action
        throw new UnsupportedOperationException("Not yet implemented");
    }


    // static methods

    public static IntentFilter getIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();

        for(String broadcast : allBroadcasts) {
            intentFilter.addAction(broadcast);
        }

        return intentFilter;
    }


    // private methods

    private void broadcastUpdate(final String action) {
        final Intent intent = new Intent(action);
        sendBroadcast(intent);
    }


    private void initHttp(String surl) throws IOException, UnsupportedOperationException {
        http = null;
        if (checkConnectivity()) {
            URL url = new URL(surl);
            http = (HttpURLConnection) url.openConnection();
        }

        if (http == null) {
            throw new UnsupportedOperationException("Cannot connect to url");
        }
    }


    private boolean checkConnectivity() {
        // TODO: improve connectivity check
        ConnectivityManager cn = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo nf = cn.getActiveNetworkInfo();
        if (nf != null && nf.isConnected() == true) {
            return true;
        }
        else {
            final Intent intent = new Intent(HTTPService.BROADCAST_HTTP_TIMEOUT);
            sendBroadcast(intent);
            return false;
        }
    }


    private String getHTTPResult(InputStream is) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(is));
        String inputLine;
        StringBuilder response = new StringBuilder();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();
        return response.toString();
    }

}
