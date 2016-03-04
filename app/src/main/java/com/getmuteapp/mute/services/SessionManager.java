package com.getmuteapp.mute.services;


import java.net.CookieManager;

public class SessionManager {
    private static CookieManager cookieManager;

    public static CookieManager getCookieManager() {
        if(cookieManager == null) {
            cookieManager = new CookieManager();
        }
        return cookieManager;
    }
}
