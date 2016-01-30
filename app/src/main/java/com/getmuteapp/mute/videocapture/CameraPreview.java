package com.getmuteapp.mute.videocapture;


import android.content.Context;
import android.content.res.Configuration;
import android.hardware.Camera;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import java.io.IOException;

public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {

    private static final String LOG_TAG = CameraPreview.class.getSimpleName();

    private static final double ASPECT_RATIO = 3.0 / 4.0;

    private SurfaceHolder holder;
    private Camera camera;

    public CameraPreview(Context context, Camera camera) {
        super(context);
        this.camera = camera;
        holder = getHolder();
        holder.addCallback(this);
        //deprecated
        //holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {

        if(camera == null) {
            startPreview();
        }

    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        refreshCamera(camera);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        camera.release();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int height = MeasureSpec.getSize(heightMeasureSpec);
        int width = MeasureSpec.getSize(widthMeasureSpec);

        final boolean isPortrait =
                getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;

        if (isPortrait) {
            if (width > height * ASPECT_RATIO) {
                width = (int) (height * ASPECT_RATIO + 0.5);
            } else {
                height = (int) (width / ASPECT_RATIO + 0.5);
            }
        } else {
            if (height > width * ASPECT_RATIO) {
                height = (int) (width * ASPECT_RATIO + 0.5);
            } else {
                width = (int) (height / ASPECT_RATIO + 0.5);
            }
        }

        setMeasuredDimension(width, width);
    }


    public void refreshCamera(Camera camera) {
        if(holder.getSurface() == null) {
            return;
        }

        try {
            camera.stopPreview();
        } catch (Exception e) {

        }

        setCamera(camera);
        camera.setDisplayOrientation(90);
        startPreview();
    }

    private void startPreview() {
        try {
            camera.setPreviewDisplay(holder);
            camera.startPreview();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setCamera(Camera camera) {
        this.camera = camera;
    }

}
