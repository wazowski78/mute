package com.getmuteapp.mute.home;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.getmuteapp.mute.R;
import com.getmuteapp.mute.model.Post;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by anil on 10/02/16.
 */
public class HomeScreenFragment extends ListFragment {
    private static final String LOG_TAG = HomeScreenFragment.class.getSimpleName();

    private ArrayList<Post> posts;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // initialize the items list
        posts = new ArrayList<Post>();
        Post post1 = new Post("post1","android.resource://"+getActivity().getPackageName()+"/raw/deneme");
        Post post2 = new Post("post2","android.resource://"+getActivity().getPackageName()+"/raw/deneme");
        post1.setTitle("A beautiful mind");
        post2.setTitle("Some other title");

        post1.setIcon(R.mipmap.ic_launcher);
        post2.setIcon(R.mipmap.ic_launcher);
        posts.add(post1);
        posts.add(post2);

    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        VideoListAdapter adapter = new VideoListAdapter(getActivity(),
                R.layout.post_list_item,
                posts);

        // Set the adapter between the ListView and its backing data.
        setListAdapter(adapter);

    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        // retrieve theListView item
        Post post = posts.get(position);

        // do something
        Toast.makeText(getActivity(),post.getUserName(),Toast.LENGTH_SHORT).show();
    }

}
