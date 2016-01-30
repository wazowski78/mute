package com.getmuteapp.mute.videocapture;

import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.getmuteapp.mute.R;

import java.io.IOException;

//TODO: Resolution ve autofocus işlerine bakmak gerek.

public class CaptureVideoActivity extends AppCompatActivity implements MediaRecorder.OnInfoListener{

    private static final String LOG_TAG = CaptureVideoActivity.class.getSimpleName();

    private Camera camera;
    private CameraPreview cameraPreview;
    private MediaRecorder mediaRecorder;
    private Context context;
    private LinearLayout cameraPreviewLayout;
    private boolean cameraFront = false;
    boolean recording = false;
    private Button capture, switchCamera;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_capture_video);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        context = this;
        initialize();
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

    public void initialize() {
        cameraPreviewLayout = (LinearLayout) findViewById(R.id.camera_preview);

        cameraPreview = new CameraPreview(context, camera);
        cameraPreviewLayout.addView(cameraPreview);

        capture = (Button) findViewById(R.id.button_capture);
        capture.setOnClickListener(captureListener);

        switchCamera = (Button) findViewById(R.id.button_change_camera);
        switchCamera.setOnClickListener(switchCameraListener);
    }

    View.OnClickListener switchCameraListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            // get the number of cameras
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
    };

    View.OnClickListener captureListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(recording) {
                mediaRecorder.stop();
                releaseMediaRecorder();
                Toast.makeText(CaptureVideoActivity.this, "Video captured!", Toast.LENGTH_LONG).show();
                recording = false;
            } else {
                if(!prepareMediaRecorder()) {
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
        }
    };

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

        CamcorderProfile profile = CamcorderProfile.get(CamcorderProfile.QUALITY_480P);
        mediaRecorder.setOutputFormat(profile.fileFormat);
        mediaRecorder.setVideoFrameRate(profile.videoFrameRate);
        mediaRecorder.setVideoSize(profile.videoFrameWidth, profile.videoFrameHeight);
        mediaRecorder.setVideoEncodingBitRate(profile.videoBitRate);
        mediaRecorder.setVideoEncoder(profile.videoCodec);

        mediaRecorder.setOutputFile("/storage/emulated/0/Download/myvideo.mp4");
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


    @Override
    public void onInfo(MediaRecorder mr, int what, int extra) {
        if (what == MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED) {
            mr.stop();
        }
    }
}


