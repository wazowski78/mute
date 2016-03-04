package com.getmuteapp.mute;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;

import com.getmuteapp.mute.home.HomeScreenActivity;
import com.getmuteapp.mute.login.LoginActivity;
import com.getmuteapp.mute.videocapture.CaptureVideoActivity;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class HomeActivity extends AppCompatActivity {
    private static final String LOG_TAG = HomeActivity.class.getSimpleName();
    private static final int PERMISSION_REQUEST_CAMERA_AND_AUDIO = 0;

    @Bind(R.id.go_video) Button goVideoButton;
    @Bind(R.id.go_login) Button goLoginButton;
    @Bind(R.id.go_home_screen) Button goHomeScreenButton;

    //TODO: Contractor gibi bir class oluşturup final valueları ordan çağırmaya ne dersin.

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        ButterKnife.bind(this);
    }

    @OnClick(R.id.go_video)
    void onGoVideoClicked() {
        if (ContextCompat.checkSelfPermission(HomeActivity.this, Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(HomeActivity.this,Manifest.permission.RECORD_AUDIO) ==
                PackageManager.PERMISSION_GRANTED) {
            goCaptureActivity();
        } else {
            ActivityCompat.requestPermissions(HomeActivity.this,
                    new String[]{
                            Manifest.permission.CAMERA,
                            Manifest.permission.RECORD_AUDIO}, PERMISSION_REQUEST_CAMERA_AND_AUDIO);
        }
    }

    @OnClick(R.id.go_login)
    void onGoLoginClicked() {
        Intent intent = new Intent(HomeActivity.this, LoginActivity.class);
        startActivity(intent);
    }

    @OnClick(R.id.go_home_screen)
    void onGoHomeScreenClicked() {
        Intent homeIntent = new Intent(HomeActivity.this, HomeScreenActivity.class);
        startActivity(homeIntent);
    }

    private void goCaptureActivity() {
        Intent i = new Intent(HomeActivity.this, CaptureVideoActivity.class);
        startActivity(i);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_CAMERA_AND_AUDIO: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    goCaptureActivity();
                } else {
                    finish();
                }
                break;
            }
        }
    }
}
