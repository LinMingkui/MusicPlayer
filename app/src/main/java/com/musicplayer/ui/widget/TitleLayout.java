package com.musicplayer.ui.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;

import com.musicplayer.R;

public class TitleLayout extends LinearLayout {

    private LinearLayout layoutPlayBar;
    private Context context;
    public TitleLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        LayoutInflater.from(context).inflate(R.layout.layout_title, this);
    }

}
