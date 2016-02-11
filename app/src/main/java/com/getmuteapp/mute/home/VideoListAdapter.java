package com.getmuteapp.mute.home;

import android.content.Context;
import android.media.MediaPlayer;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.VideoView;

import com.getmuteapp.mute.R;
import com.getmuteapp.mute.model.Post;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.List;

/**
 * Created by anil on 10/02/16.
 */
public class VideoListAdapter extends ArrayAdapter<Post> {
    private static final String LOG_TAG = VideoListAdapter.class.getSimpleName();

    private Context context;

    public VideoListAdapter(Context context, int resource, List<Post> objects) {
        super(context, resource, objects);
        this.context = context;
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View view = convertView;

        if (view == null) {
            LayoutInflater layoutInflater;
            layoutInflater = LayoutInflater.from(context);
            view = layoutInflater.inflate(R.layout.post_list_item, parent, false);
        }

        Post post = getItem(position);

        if (post != null) {
            //TODO: Burada gerekli viewlar assign edilecek.
            TextView userName = (TextView) view.findViewById(R.id.user_name);
            ImageView userProfilePic = (ImageView) view.findViewById(R.id.user_profile_pic);
            MuteVideoView postContent = (MuteVideoView) view.findViewById(R.id.post_content);
            TextView title = (TextView) view.findViewById(R.id.post_title);
            TextView numberOfAnswers = (TextView) view.findViewById(R.id.post_number_of_answers);
            TextView date = (TextView) view.findViewById(R.id.post_date);

            if(userName != null) {
                userName.setText(post.getUserName());
            }

            if(userProfilePic != null) {
                userProfilePic.setImageResource(post.getIcon());
            }

            if(postContent != null) {
                Log.d(LOG_TAG, "Postcontent null değildir.");
                DisplayMetrics displaymetrics = new DisplayMetrics();
                ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getMetrics(displaymetrics);
                int height = displaymetrics.widthPixels;
                int width = displaymetrics.widthPixels;
                Log.d(LOG_TAG,"HEIGHT: "+height+" WIDTH: "+width);
                ViewGroup.LayoutParams layoutParams = postContent.getLayoutParams();
                layoutParams.width = width;
                layoutParams.height = height;
                postContent.setLayoutParams(layoutParams);
                postContent.setVisibility(View.VISIBLE);
                MediaController mediaController = new MediaController(context);
                mediaController.setAnchorView(postContent);
                mediaController.setMediaPlayer(postContent);
                postContent.setVideoURI(post.getUri());
                postContent.start();

            }

            if(title != null) {
                title.setText(post.getTitle());
                //title.setHeight(postContent.getWidth()/7);
            }

            if(numberOfAnswers != null) {
                numberOfAnswers.setText("9 answers");
            }

            if(date != null) {
                date.setText("9h");
            }

        }

        return view;
    }

}
