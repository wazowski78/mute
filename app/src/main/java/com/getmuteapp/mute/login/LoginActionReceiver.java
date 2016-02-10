package com.getmuteapp.mute.login;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.getmuteapp.mute.HomeActivity;
import com.getmuteapp.mute.home.HomeScreenActivity;

import org.json.JSONException;
import org.json.JSONObject;

public class LoginActionReceiver extends BroadcastReceiver {

    private static final String LOG_TAG = LoginActionReceiver.class.getSimpleName();
    private static final String SUCCESS = "success";
    private static final String INTERNAL_SERVER = "InternalServer";
    private static final String BAD_REQUEST = "BadRequest";

    public static final String LOGIN_RESPONSE = "LOGIN_RESPONSE";

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
                    Intent i = new Intent(context, HomeScreenActivity.class);
                    i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(i);
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
