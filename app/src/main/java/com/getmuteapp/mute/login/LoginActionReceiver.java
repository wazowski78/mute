package com.getmuteapp.mute.login;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.getmuteapp.mute.HomeActivity;
import com.getmuteapp.mute.home.HomeScreenActivity;
import com.getmuteapp.mute.services.CommunicationService;

import org.json.JSONException;
import org.json.JSONObject;

public class LoginActionReceiver extends BroadcastReceiver {

    private static final String LOG_TAG = LoginActionReceiver.class.getSimpleName();

    public static final String SUCCESS = "success";
    public static final String INTERNAL_SERVER = "InternalServer";
    public static final String BAD_REQUEST = "BadRequest";
    public static final String UNAUTHORIZED = "Unautjorized";

    public static final String LOGIN_RESPONSE = "LOGIN_RESPONSE";
    public static final String SESSION_SHARED_PREFERENCES = "SESSION_SHARED_PREFERENCES";
    public static final String SP_USER_ID = "SP_USER_ID";
    public static final String ACTION_TAKE_SESSION_KEY = "ACTION_TAKE_SESSION_KEY";

    public LoginActionReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        String response = intent.getStringExtra(LOGIN_RESPONSE);
        String responseKey = null;

        try {
            JSONObject responseJSON = new JSONObject(response);
            responseKey = responseJSON.getString("response");
        } catch(JSONException e) {
            e.printStackTrace();
        }

        if(responseKey != null) {
            switch (responseKey) {
                case SUCCESS:
                    SharedPreferences sp = context.getSharedPreferences(SESSION_SHARED_PREFERENCES,Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sp.edit();
                    editor.putString(SP_USER_ID, AccessToken.getCurrentAccessToken().getUserId());
                    editor.apply();
                    StringBuilder sb = new StringBuilder();
                    sb.append("{\"userId\":\"");
                    sb.append(sp.getString(SP_USER_ID,null));
                    sb.append("\"}");

                    CommunicationService.startActionPostServer(context,sb.toString() , ACTION_TAKE_SESSION_KEY);
                    break;
                case INTERNAL_SERVER:
                    break;
                case BAD_REQUEST:
                    break;
            }
        } else {
            //TODO: Response key null
        }
    }
}
