package com.getmuteapp.mute.login;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.getmuteapp.mute.home.HomeScreenActivity;

public class SessionKeyActionReceiver extends BroadcastReceiver {

    private static final String LOG_TAG = SessionKeyActionReceiver.class.getSimpleName();

    public static final String SESSION_KEY_RESPONSE = "SESSION_KEY_RESPONSE";

    @Override
    public void onReceive(Context context, Intent intent) {

        String response = intent.getStringExtra(SESSION_KEY_RESPONSE);

        if(response != null) {
            Intent i = new Intent(context, HomeScreenActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(i);

        } else {
            Log.d(LOG_TAG,"response null gelmiş");
        }

    }
}
