package com.musicplayer.ui.widget;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.widget.LinearLayout;

import com.musicplayer.R;

class AddSongMenuDialog extends Dialog {

    private LinearLayout linearLayoutDialog;
    public AddSongMenuDialog(Context context, int themeResId) {
        super(context, themeResId);
    }


    public AddSongMenuDialog(Context context) {
        super(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_add_song_menu);
        linearLayoutDialog = findViewById(R.id.linear_layout_dialog);
//        DisplayMetrics dm = new DisplayMetrics();
//        int width = dm.widthPixels;
//        LinearLayout.LayoutParams params= (LinearLayout.LayoutParams) linearLayoutDialog.getLayoutParams();
//        params.weight = (int)(width*0.6);
//        linearLayoutDialog.setLayoutParams(params);
    }
}
