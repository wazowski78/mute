package com.getmuteapp.mute.services;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;


public class CommunicationService extends IntentService {

    private static final String LOG_TAG = CommunicationService.class.getSimpleName();

    public CommunicationService(){ super("CommunicationService");}

    public static void startActionUploadVideo(Context context, String path, String action) {
        //TODO: Implement here
        Intent i = new Intent(context,CommunicationService.class);
        i.setAction(action);
        context.startService(i);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if(intent != null) {
            String action = intent.getAction();

            switch (action) {
            }
        }
    }


}
