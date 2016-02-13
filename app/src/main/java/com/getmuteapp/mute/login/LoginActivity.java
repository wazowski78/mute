package com.getmuteapp.mute.login;

import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.VideoView;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphRequestAsyncTask;
import com.facebook.GraphResponse;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.getmuteapp.mute.R;
import com.getmuteapp.mute.services.CommunicationService;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.List;

public class LoginActivity extends AppCompatActivity {

    private static final String LOG_TAG = LoginActivity.class.getSimpleName();

    private LoginButton facebookLoginButton;
    private CallbackManager callbackManager;
    private Context context;
    VideoView videoView = null;
    ImageView imageView;

    public static final String ACTION_LOGIN = "ACTION_LOGIN";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //This must be called before the super.onCreate()
        makeFullScreen();
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(this);
        context = this;
        setContentView(R.layout.activity_login);
        setViewConfigurations();
        setClickOptions();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if(AccessToken.getCurrentAccessToken() !=  null) {
            facebookLoginButton.setVisibility(View.GONE);
        }

    }

    private void makeFullScreen() {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    private void setViewConfigurations() {
        videoView = (VideoView) findViewById(R.id.background_video_view);
        imageView = (ImageView) findViewById(R.id.logo);
        previewVideo();
        facebookLoginButton = (LoginButton) findViewById(R.id.facebook_login_button);
        facebookLoginButton.setReadPermissions(Arrays.asList("public_profile, email, user_birthday, user_friends"));

    }

    private void previewVideo() {
        videoView.setVisibility(View.VISIBLE);
        Uri video = Uri.parse("android.resource://" + getPackageName() + "/raw/background_video_login");
        videoView.setVideoURI(video);
        DisplayMetrics displaymetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        int height = displaymetrics.heightPixels;
        int width = displaymetrics.widthPixels;
        ViewGroup.LayoutParams layoutParams = videoView.getLayoutParams();
        layoutParams.width = width;
        layoutParams.height = height;
        setImageSize(width);
        videoView.setLayoutParams(layoutParams);
        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mp.setLooping(true);
                mp.setVolume(0,0);
            }
        });
        videoView.start();
    }

    private void setImageSize(int width) {

        int imageWidth = width/2;
        int imageHeight = width/2;
        ViewGroup.LayoutParams imageLayoutParams = imageView.getLayoutParams();
        imageLayoutParams.width = imageWidth;
        imageLayoutParams.height = imageHeight;
        imageView.setLayoutParams(imageLayoutParams);
    }

    private void setClickOptions() {
        callbackManager = CallbackManager.Factory.create();
        facebookLoginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(final LoginResult loginResult) {
                Log.d(LOG_TAG, "Access Token: " + loginResult.getAccessToken().getToken());

                GraphRequest request = GraphRequest.newMeRequest(
                        loginResult.getAccessToken(),
                        newMeRequestJsonCallback(loginResult)
                        );

                Bundle parameters = new Bundle();
                parameters.putString("fields", "id,name,email,gender,birthday");
                request.setParameters(parameters);
                Log.d(LOG_TAG, parameters.toString());
                request.executeAsync();

            }

            @Override
            public void onCancel() {
                Log.d(LOG_TAG, "Cancel");
            }

            @Override
            public void onError(FacebookException error) {
                Log.d(LOG_TAG, error.toString());
            }
        });
    }

    private GraphRequest.GraphJSONObjectCallback newMeRequestJsonCallback(final LoginResult loginResult) {
        return new GraphRequest.GraphJSONObjectCallback() {
            @Override
            public void onCompleted(JSONObject object, GraphResponse response) {
                try {
                    //When newMeRequest completed getFacebookFriends
                    //get() blocks the current thread and waits for result
                    List<GraphResponse> friendsResponseList = getFacebookFriends(loginResult).get();

                    //friendsResponseList is a one-element List (I guess)
                    GraphResponse friendsResponse = friendsResponseList.get(0);
                    JSONObject friendsResponseJSONObject = friendsResponse.getJSONObject();

                    JSONArray friendsArray = friendsResponseJSONObject.getJSONArray("data");
                    StringBuilder sb = new StringBuilder();
                    sb.append("{");
                    sb.append("\"user\":{\"userId\":\"");
                    sb.append(object.getString("id"));
                    sb.append("\",\"facebookId\":\"");
                    sb.append(object.getString("id"));
                    sb.append("\",\"name\":\"");
                    sb.append(object.getString("name"));
                    sb.append("\",\"email\":\"");
                    sb.append(object.getString("email"));
                    sb.append("\",\"dateOfBirth\":\"");
                    sb.append(object.getString("birthday"));
                    sb.append("\",\"score\":0,\"gender\":\"");
                    sb.append(object.getString("gender"));
                    sb.append("\"},\"friends\":[");
                    for (int i = 0; i < friendsArray.length(); i++) {
                        JSONObject jsonObject = friendsArray.getJSONObject(i);
                        sb.append("\"");
                        sb.append(jsonObject.getString("id"));
                        sb.append("\"");
                        if (i != friendsArray.length() - 1) {
                            sb.append(",");
                        }
                    }
                    sb.append("]}");
                    Log.d(LOG_TAG, "SERVER: " + sb.toString());
                    //Service triggered.
                    CommunicationService.startActionPostServer(context, sb.toString(), ACTION_LOGIN);

                } catch (Exception ex) {
                    Log.e(LOG_TAG, ex.getMessage());
                }
            }
        };
    }

    private GraphRequestAsyncTask getFacebookFriends(LoginResult loginResult) {
        return GraphRequest.newMyFriendsRequest(
                loginResult.getAccessToken(),
                new GraphRequest.GraphJSONArrayCallback() {
                    @Override
                    public void onCompleted(JSONArray objects, GraphResponse response) {
                        Log.d(LOG_TAG, response.toString());
                    }
                }).executeAsync();
    }
}
