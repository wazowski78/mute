package com.getmuteapp.mute.services;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.getmuteapp.mute.login.LoginActionReceiver;
import com.getmuteapp.mute.login.LoginActivity;
import com.getmuteapp.mute.login.SessionKeyActionReceiver;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.HttpCookie;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.List;
import java.util.Map;


public class CommunicationService extends IntentService {

    private static final String LOG_TAG = CommunicationService.class.getSimpleName();

    private static final String HOST_TAG = "HOST";
    private static final String DATA = "DATA";

    public static final String HOST = "http://192.168.0.15:9000";
    public static final String ACTION_LOGIN_COMPLETED = "ACTION_LOGIN_COMPLETED";
    public static final String ACTION_TAKE_SESSION_KEY_COMPLETED = "ACTION_TAKE_SESSION_KEY_COMPLETED";

    public CommunicationService(){ super("CommunicationService");}

    public static void startActionPostServer(Context context, String data, String action) {
        Intent i = new Intent(context,CommunicationService.class);
        i.putExtra(DATA,data);
        i.putExtra(HOST_TAG,HOST);
        i.setAction(action);
        context.startService(i);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if(intent != null) {
            String action = intent.getAction();
            String data = intent.getStringExtra(DATA);
            String host = intent.getStringExtra(HOST_TAG);

            switch (action) {
                case LoginActivity.ACTION_LOGIN:
                    Intent loginBroadcastIntent = new Intent(ACTION_LOGIN_COMPLETED);
                    loginBroadcastIntent.putExtra(LoginActionReceiver.LOGIN_RESPONSE,
                            postData(data,host+"/newUser"));
                    sendBroadcast(loginBroadcastIntent);
                    break;
                case LoginActionReceiver.ACTION_TAKE_SESSION_KEY:
                    Intent takeSessionKeyBroadcastIntent = new Intent(ACTION_TAKE_SESSION_KEY_COMPLETED);
                    takeSessionKeyBroadcastIntent.putExtra(SessionKeyActionReceiver.SESSION_KEY_RESPONSE,
                            postData(data, host + "/login"));
                    sendBroadcast(takeSessionKeyBroadcastIntent);
                    break;
            }
        }
    }

    public String postData(String parameters,String local) {
        final String urlparameters = parameters;
        String returnString = "";
        try {
            URL url = new URL(local);
            final String COOKIES_HEADER = "Set-Cookie";
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            java.net.CookieManager cookieManager = SessionManager.getCookieManager();

            con.setRequestMethod("POST");
            con.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            if(cookieManager.getCookieStore().getCookies().size() > 0) {
                //While joining the Cookies, use ',' or ';' as needed. Most of the server are using ';'
                Log.d(LOG_TAG,"IF");
                con.setRequestProperty("Cookie",
                        "PLAY_SESSION="+cookieManager.getCookieStore().getCookies().get(0).getValue());

                //TextUtils.join(";", cookieManager.getCookieStore().getCookies())
            }
            con.setDoOutput(true);
            DataOutputStream wr = new DataOutputStream(con.getOutputStream());
            wr.writeBytes(urlparameters);
            wr.flush();
            wr.close();

            Map<String, List<String>> headerFields = con.getHeaderFields();
            List<String> cookiesHeader = headerFields.get(COOKIES_HEADER);

            if(cookiesHeader != null) {
                for (String cookie : cookiesHeader) {
                    cookieManager.getCookieStore().add(null, HttpCookie.parse(cookie).get(0));
                    Log.d(LOG_TAG,cookie);
                    Log.d(LOG_TAG,cookieManager.getCookieStore().getCookies().get(0).getValue());
                }
            }
            /*
            */

            int responseCode = con.getResponseCode();
            Log.d("RESPONSECODE", responseCode + "");

            switch (responseCode) {
                case 400:
                    return "{\"response\":\"BadRequest\"}";
                case 401:
                    return "{\"response\":\"Unauthorized\"}";
                case 500:
                    return "{\"response\":\"InternalServer\"}";
                default:
                    Log.d(LOG_TAG,"Undefined response code");
                    break;

            }

            BufferedReader in = new BufferedReader(
                    new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
            System.out.println("RESPONSEEEEE: " + response.toString());

            returnString = response.toString();

            System.out.println("RESPONSEEEEE: "+returnString);
            con.disconnect();

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (ProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return returnString;
    }


}
