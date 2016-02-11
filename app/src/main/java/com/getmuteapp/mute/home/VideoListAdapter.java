package com.getmuteapp.mute.home;

import android.content.Context;
import android.media.MediaPlayer;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.getmuteapp.mute.R;
import com.getmuteapp.mute.model.Post;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.List;

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
            final MuteVideoView postContent = (MuteVideoView) view.findViewById(R.id.post_content);
            TextView title = (TextView) view.findViewById(R.id.post_title);
            TextView numberOfAnswers = (TextView) view.findViewById(R.id.post_number_of_answers);
            TextView date = (TextView) view.findViewById(R.id.post_date);

            if(userName != null) {
                userName.setText(post.getUserName());
            }

            if(userProfilePic != null) {
                userProfilePic.setImageResource(post.getIcon());
                userProfilePic.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Toast.makeText(context,"Hello world!",Toast.LENGTH_SHORT).show();
                    }
                });
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
                postContent.seekTo(1);

                postContent.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        VideoView mView = (VideoView) v;
                        if(mView.isPlaying()) {
                            mView.pause();
                        } else {
                            mView.start();
                        }
                        return false;
                    }
                });


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
