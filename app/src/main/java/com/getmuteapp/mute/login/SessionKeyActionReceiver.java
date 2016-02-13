package com.getmuteapp.mute.login;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.webkit.CookieManager;

import com.getmuteapp.mute.services.CommunicationService;

import java.net.CookieHandler;
import java.net.CookieStore;
import java.net.HttpCookie;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

public class SessionKeyActionReceiver extends BroadcastReceiver {

    private static final String LOG_TAG = SessionKeyActionReceiver.class.getSimpleName();

    public static final String SESSION_KEY_RESPONSE = "SESSION_KEY_RESPONSE";

    @Override
    public void onReceive(Context context, Intent intent) {

        CookieManager cookieManager = android.webkit.CookieManager.getInstance();
        cookieManager.setAcceptCookie(true);
        CookieStore cookieStore = ((java.net.CookieManager) CookieHandler.getDefault()).getCookieStore();
        URI baseUri = null;
        try {
            baseUri = new URI(CommunicationService.HOST);
        } catch(URISyntaxException e) {
            e.printStackTrace();
        }

        List<HttpCookie> cookies = cookieStore.get(baseUri);
        String url = baseUri.toString();

        for (HttpCookie cookie : cookies) {
            String setCookie = new StringBuilder(cookie.toString())
                    .append("; domain=").append(cookie.getDomain())
                    .append("; path=").append(cookie.getPath())
                    .toString();
            cookieManager.setCookie(url, setCookie);
        }

    }
}
