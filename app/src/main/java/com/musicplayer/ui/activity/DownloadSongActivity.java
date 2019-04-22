package com.musicplayer.ui.activity;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.musicplayer.R;
import com.musicplayer.database.DataBase;
import com.musicplayer.utils.BaseActivity;

public class DownloadSongActivity extends BaseActivity implements View.OnClickListener {

    private Context mContext;
    private DataBase dataBase;

    private ImageView imgTitleBack;
    private TextView tvTitleName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download_song);
        init();
        setOnClickListener();
    }

    private void setOnClickListener(){
        imgTitleBack.setOnClickListener(this);
    }
    private void init(){
        mContext = DownloadSongActivity.this;
        imgTitleBack = findViewById(R.id.img_title_back);
        tvTitleName = findViewById(R.id.tv_title_name);
        tvTitleName.setText("下载管理");
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.img_title_back:
                DownloadSongActivity.this.finish();
                break;
        }
    }
}
