package com.getmuteapp.mute.home;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;


public class MuteVideoImageView extends ImageView {
    public MuteVideoImageView(Context context) {
        super(context);
    }

    public MuteVideoImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MuteVideoImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int size = 0;
        int width = getMeasuredWidth();
        int height = getMeasuredHeight();

        size = width;

        setMeasuredDimension(size, size);
    }
}
