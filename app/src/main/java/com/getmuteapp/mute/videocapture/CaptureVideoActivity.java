package com.getmuteapp.mute.videocapture;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.getmuteapp.mute.R;
import com.getmuteapp.mute.videoupload.UploadVideoActivity;

import java.io.IOException;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

//TODO: Resolution ve autofocus işlerine bakmak gerek.

public class CaptureVideoActivity extends AppCompatActivity implements MediaRecorder.OnInfoListener{

    public static final String FILE_PATH = "FILE_PATH";

    private static final String LOG_TAG = CaptureVideoActivity.class.getSimpleName();
    private static final int PERMISSION_REQUEST_STORAGE = 1;

    @Bind(R.id.camera_preview) LinearLayout cameraPreviewLayout;
    @Bind(R.id.button_capture) Button capture;
    @Bind(R.id.button_change_camera) Button switchCamera;

    private Camera camera;
    private CameraPreview cameraPreview;
    private MediaRecorder mediaRecorder;
    private Context context;

    private boolean cameraFront = false;
    boolean recording = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_capture_video);
        ButterKnife.bind(this);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        context = this;

        cameraPreview = new CameraPreview(context, camera);
        cameraPreviewLayout.addView(cameraPreview);
    }

    @Override
    public void onResume() {
        super.onResume();
        if(!hasCamera(context)) {
            Toast.makeText(context, "Your device has no camera!", Toast.LENGTH_LONG).show();
            finish();
        }

        if(camera == null) {
            chooseCamera();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        releaseCamera();
    }

    @OnClick(R.id.button_change_camera)
    void onChangeClicked() {
        if (!recording) {
            int camerasNumber = Camera.getNumberOfCameras();
            if (camerasNumber > 1) {
                // release the old camera instance
                // switch camera, from the front and the back and vice versa

                releaseCamera();
                cameraFront = !cameraFront;
                chooseCamera();
            } else {
                Toast toast = Toast.makeText(context, "Sorry, your phone has only one camera!", Toast.LENGTH_LONG);
                toast.show();
            }
        }
    }

    @OnClick(R.id.button_capture)
    void onCaptureClicked() {
        if(recording) {
            stopMediaRecorder();
        } else {
            if(ContextCompat.checkSelfPermission(context,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                performCapturingAction();
            } else {
                ActivityCompat.requestPermissions(CaptureVideoActivity.this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        PERMISSION_REQUEST_STORAGE);
            }
        }
    }

    public void chooseCamera() {
        if(context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            if (!cameraFront) {
                int cameraId = findBackFacingCamera();
                if (cameraId >= 0) {
                    camera = Camera.open(cameraId);
                    cameraPreview.refreshCamera(camera);
                }
            } else {
                int cameraId = findFrontFacingCamera();
                if (cameraId >= 0) {
                    camera = Camera.open(cameraId);
                    cameraPreview.refreshCamera(camera);
                }
            }
        }
        else {
            Toast.makeText(context, "Please remove the app gracefully", Toast.LENGTH_LONG).show();
        }
    }

    private void releaseMediaRecorder() {
        if(mediaRecorder != null) {
            mediaRecorder.reset();
            mediaRecorder.release();
            mediaRecorder = null;
            camera.lock();
        }
    }

    private boolean prepareMediaRecorder() {
        mediaRecorder = new MediaRecorder();
        camera.unlock();
        mediaRecorder.setCamera(camera);

        mediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);

        CamcorderProfile profile = CamcorderProfile.get(CamcorderProfile.QUALITY_LOW);
        mediaRecorder.setOutputFormat(profile.fileFormat);
        mediaRecorder.setVideoFrameRate(profile.videoFrameRate);
        mediaRecorder.setVideoSize(profile.videoFrameWidth, profile.videoFrameHeight);
        mediaRecorder.setVideoEncodingBitRate(profile.videoBitRate);
        mediaRecorder.setVideoEncoder(profile.videoCodec);

        mediaRecorder.setOutputFile(context.getExternalFilesDir(null).getAbsolutePath()+"/myvideo.mp4");
        mediaRecorder.setMaxDuration(5000);
        mediaRecorder.setOnInfoListener(this);
        try {
            mediaRecorder.prepare();
        } catch (IllegalStateException e) {
            e.printStackTrace();
            releaseMediaRecorder();
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            releaseMediaRecorder();
            return false;
        }

        return true;
    }

    private boolean hasCamera(Context context) {
        return context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA);
    }

    private void releaseCamera() {
        if(camera != null) {
            camera.release();
            camera = null;
        }
    }

    private int findFrontFacingCamera() {
        int cameraId = -1;
        int numberOfCameras = Camera.getNumberOfCameras();

        Log.d(LOG_TAG, "Number of cameras in findFrontFacingCamera(): " + numberOfCameras);
        for(int i = 0; i < numberOfCameras; i++) {
            Camera.CameraInfo info = new Camera.CameraInfo();
            Camera.getCameraInfo(i,info);
            if(info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                cameraId = i;
                cameraFront = true;
                break;
            }
        }
        return cameraId;
    }

    private int findBackFacingCamera() {
        int cameraId = -1;
        int numberOfCameras = Camera.getNumberOfCameras();

        Log.d(LOG_TAG, "Number of cameras in findBackFacingCamera(): " + numberOfCameras);
        for(int i = 0; i < numberOfCameras; i++) {
            Camera.CameraInfo info = new Camera.CameraInfo();
            Camera.getCameraInfo(i,info);
            if(info.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                cameraId = i;
                cameraFront = false;
                break;
            }
        }
        return cameraId;
    }

    private void performCapturingAction() {
        if (!prepareMediaRecorder()) {
            Toast.makeText(CaptureVideoActivity.this, "Fail in prepareMediaRecorder()!\n - Ended -", Toast.LENGTH_LONG).show();
            finish();
        }

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    mediaRecorder.start();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        recording = true;
    }

    private void stopMediaRecorder() {
        mediaRecorder.stop();
        releaseMediaRecorder();
        Toast.makeText(CaptureVideoActivity.this, "Video captured!", Toast.LENGTH_LONG).show();
        recording = false;
        Intent i = new Intent(CaptureVideoActivity.this, UploadVideoActivity.class);
        i.putExtra(FILE_PATH,context.getExternalFilesDir(null).getAbsolutePath()+"/myvideo.mp4");
        startActivity(i);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_STORAGE: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    performCapturingAction();
                } else {
                    finish();
                }
                break;
            }
        }
    }



    @Override
    public void onInfo(MediaRecorder mr, int what, int extra) {
        if (what == MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED) {
            stopMediaRecorder();
        }
    }
}


