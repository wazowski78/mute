package com.getmuteapp.mute.login;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import org.json.JSONException;
import org.json.JSONObject;

public class LoginActionReceiver extends BroadcastReceiver {

    private static final String LOG_TAG = LoginActionReceiver.class.getSimpleName();

    public static final String LOGIN_RESPONSE = "LOGIN_RESPONSE";

    public LoginActionReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        String response = intent.getStringExtra(LOGIN_RESPONSE);
        String responseKey = null;

        try {
            JSONObject responseJSON = new JSONObject(response);
            responseKey = responseJSON.getString("key"); //TODO: Buradaki key değişecek.

        } catch(JSONException e) {
            e.printStackTrace();
        }

        if(responseKey != null) {
            switch (responseKey) {
                //TODO: Duruma göre buralar bakılacak.
            }
        } else {
            //TODO: Response key null
        }
    }
}
