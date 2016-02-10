package com.getmuteapp.mute.model;


import android.net.Uri;

public class Post {
    private String userName;
    private String filePath;
    private int icon;
    private Uri uri;

    public Post(String userName, String filePath) {
        this.userName = userName;
        this.filePath = filePath;
        this.uri = Uri.parse(filePath);
    }

    public String getUserName() {
        return userName;
    }

    public String getFilePath() {
        return filePath;
    }

    public int getIcon() {
        return icon;
    }

    public void setIcon(int icon) {
        this.icon = icon;
    }

    public Uri getUri() {
        return uri;
    }

}
