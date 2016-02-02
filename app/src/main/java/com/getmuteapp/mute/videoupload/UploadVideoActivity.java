package com.getmuteapp.mute.videoupload;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.getmuteapp.mute.R;
import com.getmuteapp.mute.utils.Compress;
import com.getmuteapp.mute.videocapture.CaptureVideoActivity;

import net.gotev.uploadservice.MultipartUploadRequest;
import net.gotev.uploadservice.UploadNotificationConfig;
import net.gotev.uploadservice.UploadService;
import net.gotev.uploadservice.UploadServiceBroadcastReceiver;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipFile;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;


public class UploadVideoActivity extends AppCompatActivity {

    private static final String LOG_TAG = UploadVideoActivity.class.getSimpleName();

    @Bind(R.id.container) ViewGroup container;
    @Bind(R.id.upload_button) Button uploadButton;
    @Bind(R.id.txtPercentage) TextView percentage;
    @Bind(R.id.progressBar)   ProgressBar progressBar;
    @Bind(R.id.videoPreview)  VideoView videoView;

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
        final String serverUrlString = "http://192.168.1.6:9000/upload";
        final String paramNameString = "video";

        try {
            final String filename = getFilename(filePath);
            Log.d(LOG_TAG,"FILE PATH: "+filePath);
            String[] fileToCompress = new String[]{filePath};
            Compress compress = new Compress(fileToCompress);
            compress.zip();
            filePath = compress.getZipFile();
            /* To make sure if it is a valid zip file or not
            File file = new File(filePath);
            if(isValid(file)){
                Toast.makeText(this,"VALID",Toast.LENGTH_LONG).show();
            }*/

            String uploadID = new MultipartUploadRequest(context, serverUrlString)
                    .addFileToUpload(filePath, paramNameString)
                    .setNotificationConfig(getNotificationConfig(filename))
                    .setCustomUserAgent("UserAgent")
                    .setAutoDeleteFilesAfterSuccessfulUpload(true)
                    .setUsesFixedLengthStreamingMode(false)
                    .setMaxRetries(2)
                    .startUpload();

            addUploadToList(uploadID,filename);

            // these are the different exceptions that may be thrown
        } catch (FileNotFoundException exc) {
            exc.printStackTrace();
        } catch (IllegalArgumentException exc) {
            exc.printStackTrace();
        } catch (MalformedURLException exc) {
            exc.printStackTrace();
        }
    }

    static boolean isValid(final File file) {
        ZipFile zipfile = null;
        try {
            zipfile = new ZipFile(file);
            return true;
        } catch (IOException e) {
            return false;
        } finally {
            try {
                if (zipfile != null) {
                    zipfile.close();
                    zipfile = null;
                }
            } catch (IOException e) {
            }
        }
    }

    private String getFilename(String filepath) {
        if (filepath == null)
            return null;

        final String[] filepathParts = filepath.split("/");

        return filepathParts[filepathParts.length - 1];
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
