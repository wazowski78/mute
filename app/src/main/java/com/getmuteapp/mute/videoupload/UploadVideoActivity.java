package com.getmuteapp.mute.videoupload;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.getmuteapp.mute.R;
import com.getmuteapp.mute.home.MuteVideoView;
import com.getmuteapp.mute.services.CommunicationService;
import com.getmuteapp.mute.services.SessionManager;
import com.getmuteapp.mute.videocapture.CaptureVideoActivity;

import net.gotev.uploadservice.MultipartUploadRequest;
import net.gotev.uploadservice.UploadNotificationConfig;
import net.gotev.uploadservice.UploadService;
import net.gotev.uploadservice.UploadServiceBroadcastReceiver;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;


public class UploadVideoActivity extends AppCompatActivity {

    private static final String LOG_TAG = UploadVideoActivity.class.getSimpleName();

    public static final String SHARED_PREFERENCES = "MUTE_SHARED_PREFERENCES";
    public static final String SCREEN_WIDTH = "SCREEN_WIDTH";

    @Bind(R.id.container) ViewGroup container;
    @Bind(R.id.upload_button) Button uploadButton;
    @Bind(R.id.txtPercentage) TextView percentage;
    @Bind(R.id.progressBar)   ProgressBar progressBar;
    @Bind(R.id.videoPreview) MuteVideoView videoView;

    private Context context;
    private String filePath = null;

    private Map<String, UploadProgressViewHolder> uploadProgressHolders = new HashMap<>();

    private final UploadServiceBroadcastReceiver uploadReceiver =
            new UploadServiceBroadcastReceiver() {

                @Override
                public void onProgress(String uploadId, int progress) {
                    Log.i(LOG_TAG, "The progress of the upload with ID " + uploadId + " is: " + progress);

                    if (uploadProgressHolders.get(uploadId) == null)
                        return;

                    uploadProgressHolders.get(uploadId).progressBar.setProgress(progress);
                }

                @Override
                public void onError(String uploadId, Exception exception) {
                    Log.e(LOG_TAG, "Error in upload with ID: " + uploadId + ". "
                            + exception.getLocalizedMessage(), exception);

                    if (uploadProgressHolders.get(uploadId) == null)
                        return;

                    container.removeView(uploadProgressHolders.get(uploadId).itemView);
                    uploadProgressHolders.remove(uploadId);
                }

                @Override
                public void onCompleted(String uploadId, int serverResponseCode, byte[] serverResponseBody) {
                    Log.i(LOG_TAG, "Upload with ID " + uploadId + " is completed: " + serverResponseCode + ", "
                            + new String(serverResponseBody));

                    if (uploadProgressHolders.get(uploadId) == null)
                        return;

                    container.removeView(uploadProgressHolders.get(uploadId).itemView);
                    uploadProgressHolders.remove(uploadId);
                }

                @Override
                public void onCancelled(String uploadId) {
                    Log.i(LOG_TAG, "Upload with ID " + uploadId + " is cancelled");

                    if (uploadProgressHolders.get(uploadId) == null)
                        return;

                    container.removeView(uploadProgressHolders.get(uploadId).itemView);
                    uploadProgressHolders.remove(uploadId);
                }
            };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_video);
        ButterKnife.bind(this);
        context = this;
        Intent i = getIntent();
        filePath = i.getStringExtra(CaptureVideoActivity.FILE_PATH);
        Log.d(LOG_TAG, filePath);

        if(filePath != null) {
            previewVideo();
        } else {
            Toast.makeText(this,
                    R.string.file_path_missing, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        uploadReceiver.register(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        uploadReceiver.unregister(this);
    }

    @OnClick(R.id.upload_button)
    void onUploadClicked() {
        handleUpload();
    }

    private void handleUpload() {
        final String serverUrlString = CommunicationService.HOST+"/upload";
        final String paramNameString = "video";

        try {
            final String fileName = getFilename(filePath);
            Bitmap thumb = ThumbnailUtils.createVideoThumbnail(filePath, MediaStore.Images.Thumbnails.MINI_KIND);
            storeImage(thumb);
            HashMap<String,String> uploadFileMap = new HashMap<>();
            uploadFileMap.put(fileName,filePath);
            uploadFileMap.put(getFilename(getThumbnailPath(filePath)), getThumbnailPath(filePath));

            for(Map.Entry<String,String> entry : uploadFileMap.entrySet()) {
                String uploadID = new MultipartUploadRequest(context, serverUrlString)
                        .addFileToUpload(entry.getValue(), paramNameString)
                        .setNotificationConfig(getNotificationConfig(fileName))
                        .setCustomUserAgent("UserAgent")
                        .setAutoDeleteFilesAfterSuccessfulUpload(true)
                        .setUsesFixedLengthStreamingMode(false)
                        .setMaxRetries(2)
                        .addHeader("Cookie","PLAY_SESSION="+ SessionManager.getCookieManager().getCookieStore().getCookies().get(0).getValue())
                        .startUpload();

                addUploadToList(uploadID, entry.getKey());

            }
            // these are the different exceptions that may be thrown
        } catch (FileNotFoundException exc) {
            exc.printStackTrace();
        } catch (IllegalArgumentException exc) {
            exc.printStackTrace();
        } catch (MalformedURLException exc) {
            exc.printStackTrace();
        }
    }

    private void storeImage(Bitmap image) {

        File pictureFile = new File(getThumbnailPath(filePath));
        if (pictureFile == null) {
            Log.d(LOG_TAG,
                    "Error creating media file, check storage permissions: ");// e.getMessage());
            return;
        }
        try {
            FileOutputStream fos = new FileOutputStream(pictureFile);
            image.compress(Bitmap.CompressFormat.PNG, 90, fos);
            fos.close();
        } catch (FileNotFoundException e) {
            Log.d(LOG_TAG, "File not found: " + e.getMessage());
        } catch (IOException e) {
            Log.d(LOG_TAG, "Error accessing file: " + e.getMessage());
        }
    }

    private String getFilename(String filepath) {
        if (filepath == null)
            return null;

        final String[] filepathParts = filepath.split("/");

        return filepathParts[filepathParts.length - 1];
    }

    private String getThumbnailPath(String filepath) {
        if (filepath == null)
            return null;

        final String[] filepathParts = filepath.split("/");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < filepathParts.length -1;i++) {

            sb.append(filepathParts[i]);
            sb.append("/");
        }
        sb.append(getFilename(filepath).substring(0,getFilename(filepath).lastIndexOf(".")));
        sb.append(".png");
        return sb.toString();
    }

    private void addUploadToList(String uploadID, String filename) {
        View uploadProgressView = getLayoutInflater().inflate(R.layout.view_upload_progress, null);
        UploadProgressViewHolder viewHolder = new UploadProgressViewHolder(uploadProgressView, filename);
        viewHolder.uploadId = uploadID;
        container.addView(viewHolder.itemView, 0);
        uploadProgressHolders.put(uploadID, viewHolder);
    }

    private UploadNotificationConfig getNotificationConfig(String filename) {
        return new UploadNotificationConfig()
                .setIcon(R.drawable.upload)
                .setTitle(filename)
                .setInProgressMessage(getString(R.string.uploading))
                .setCompletedMessage(getString(R.string.upload_success))
                .setErrorMessage(getString(R.string.upload_error))
                .setAutoClearOnSuccess(false)
                .setClickIntent(new Intent(this, CaptureVideoActivity.class))
                .setClearOnAction(true)
                .setRingToneEnabled(true);
    }

    private void previewVideo() {
        videoView.setVisibility(View.VISIBLE);
        videoView.setVideoPath(filePath);
        // start playing
        videoView.start();
    }


    class UploadProgressViewHolder {
        View itemView;

        @Bind(R.id.uploadTitle) TextView uploadTitle;
        @Bind(R.id.uploadProgress) ProgressBar progressBar;

        String uploadId;

        UploadProgressViewHolder(View view, String filename) {
            itemView = view;
            ButterKnife.bind(this, itemView);

            progressBar.setMax(100);
            progressBar.setProgress(0);

            uploadTitle.setText(getString(R.string.upload_progress, filename));
        }

        @OnClick(R.id.cancelUploadButton)
        void onCancelUploadClick() {
            if (uploadId == null)
                return;

            UploadService.stopUpload(uploadId);
        }
    }
}
