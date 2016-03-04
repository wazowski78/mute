package com.getmuteapp.mute.login;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.getmuteapp.mute.home.HomeScreenActivity;

import org.json.JSONException;
import org.json.JSONObject;

public class SessionKeyActionReceiver extends BroadcastReceiver {

    private static final String LOG_TAG = SessionKeyActionReceiver.class.getSimpleName();

    public static final String SESSION_KEY_RESPONSE = "SESSION_KEY_RESPONSE";

    @Override
    public void onReceive(Context context, Intent intent) {

        String response = intent.getStringExtra(SESSION_KEY_RESPONSE);
        String action = "";

        try {
            JSONObject responseJSON = new JSONObject(response);
            action = responseJSON.getString("response");
        } catch(JSONException e) {
            e.printStackTrace();
        }

        if(action != null) {
            switch (action) {
                case LoginActionReceiver.SUCCESS:
                    Intent i = new Intent(context, HomeScreenActivity.class);
                    i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(i);
                    break;
                case LoginActionReceiver.BAD_REQUEST:
                    break;
                case LoginActionReceiver.UNAUTHORIZED:
                    break;
                case LoginActionReceiver.INTERNAL_SERVER:
                    break;
                default:
                    Log.d(LOG_TAG,"Demek ki Ulaş bana haber vermeden bişey döndü!");
                    break;
            }
        } else {
            Log.d(LOG_TAG,"action null gelmiş");
        }

    }
}
