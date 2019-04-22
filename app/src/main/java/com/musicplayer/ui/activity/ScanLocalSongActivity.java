package com.musicplayer.ui.activity;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.musicplayer.R;
import com.musicplayer.database.DataBase;
import com.musicplayer.utils.BaseActivity;
import com.musicplayer.utils.Song;
import com.musicplayer.utils.StaticVariate;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static com.musicplayer.utils.AudioUtils.getSong;
import static com.musicplayer.utils.MethodUtils.insertSong;

public class ScanLocalSongActivity extends BaseActivity implements View.OnClickListener {

    private Context mContext;
    private final String TAG = "*ScanLocalSongActivity";
    List<File> fileList = new ArrayList<>();
    private int num = 0;
    private boolean run = true;
    private boolean isOver = false;
    private TextView tvTitleName;
    private TextView tvScanning, tvScanFile;
    private ImageView imgTitleBack, imgTitleSearch, imgTitleMenu;
    private Button btnScanAll;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_local_song);

        init();
        setOnClickListener();
    }

    private void init() {
        mContext = ScanLocalSongActivity.this;
        tvTitleName = findViewById(R.id.tv_title_name);
        tvTitleName.setText("扫描本地音乐");
        tvScanning = findViewById(R.id.tv_scanning);
        tvScanning.setVisibility(View.INVISIBLE);
        tvScanFile = findViewById(R.id.tv_scan_file);
        tvScanFile.setVisibility(View.INVISIBLE);

        imgTitleSearch = findViewById(R.id.img_title_search);
        imgTitleSearch.setVisibility(View.GONE);
        imgTitleMenu = findViewById(R.id.img_title_menu);
        imgTitleMenu.setVisibility(View.GONE);
        imgTitleBack = findViewById(R.id.img_title_back);

        btnScanAll = findViewById(R.id.btn_scan_all);

    }

    private void setOnClickListener() {
        imgTitleBack.setOnClickListener(this);
        btnScanAll.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.img_title_back:
                ScanLocalSongActivity.this.finish();
                break;
            case R.id.btn_scan_all:
                tvScanning.setVisibility(View.VISIBLE);
                tvScanFile.setVisibility(View.VISIBLE);
                File file = Environment.getExternalStorageDirectory();
                Log.e(TAG, file.getPath());
                ScanSongThread scanSongThread = new ScanSongThread(file);
                scanSongThread.start();
                SaveSongToDataBaseThread saveSongToDataBaseThread = new SaveSongToDataBaseThread();
                saveSongToDataBaseThread.start();
                btnScanAll.setClickable(false);
                break;
        }
    }


    class ScanSongThread extends Thread {

        private File file;

        public ScanSongThread(File file) {
            this.file = file;
        }

        public void run() {
            Log.e(TAG, "ScanSongThread start");
            scanSong(file);
            isOver = true;
            Log.e(TAG, "ScanSongThread stop");
        }

        private void scanSong(File file) {
            try {
                File[] files = file.listFiles();
                if (files.length > 0) {
                    for (int j = 0; j < files.length; j++) {
                        if (files[j].isFile()) {
                            String fileName = files[j].getName();
                            String suffix = fileName.substring(fileName.lastIndexOf(".") + 1);
                            if ("mp3".equals(suffix) || "wma".equals(suffix)) {
                                synchronized (this) {
                                    fileList.add(files[j]);
                                    num++;
                                }
                            }
                        } else {
                            this.scanSong(files[j]);
                        }
                    }
                }
            } catch (Exception e) {
                e.toString();
            }
        }
    }

    class SaveSongToDataBaseThread extends Thread {
        DataBase dataBase = new DataBase(mContext,
                StaticVariate.dataBaseName, null, 1);
        SQLiteDatabase db = dataBase.getWritableDatabase();
        Cursor cursor;
        String fileUrl;
        int duration;
        Song song;
        Bundle bundle = new Bundle();
        int position = 0;
        int newAddNum = 0;

        public void run() {
            Log.e(TAG, "SaveSongToDataBaseThread start");
            while (run) {
                for (; position < num; position++) {
                    synchronized (this) {
                        song = getSong(fileList.get(position).getPath());
                    }
                    fileUrl = song.getFileUrl();
                    bundle.putString("fileUrl", fileUrl);
                    Message message = new Message();
                    message.what = 1;
                    message.setData(bundle);
                    changeUIHandler.sendMessage(message);
                    duration = song.getDuration() / 1000;
                    if (duration > 30) {
                        cursor = db.rawQuery("select id from " + StaticVariate.localSongListTable
                                        + " where " + StaticVariate.fileUrl + " = ?",
                                new String[]{fileUrl});
                        int count = cursor.getCount();
                        if (count == 0) {
                            Log.e(TAG, fileUrl);
                            insertSong(db, StaticVariate.localSongListTable, song);
                            newAddNum++;
                        }
                    }
                }
                if (isOver && position == num) {
                    run = false;
                    Message message = new Message();
                    message.what = 2;
                    bundle.putInt("newAddNum", newAddNum);
                    message.setData(bundle);
                    changeUIHandler.sendMessage(message);
                }
                try {
                    sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            if (dataBase != null) {
                dataBase.close();
            }
            if (db != null) {
                db.close();
            }
            if (cursor != null) {
                cursor.close();
            }
            Log.e(TAG, "SaveSongToDataBaseThread stop");
        }
    }

    private Handler changeUIHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    String fileUrl = msg.getData().getString("fileUrl");
                    tvScanFile.setText(fileUrl);
                    break;
                case 2:
                    tvScanning.setText("扫描完成");
                    tvScanFile.setText("共扫描到" + num + "首，新增"
                            + msg.getData().getInt("newAddNum") + "首");
                    btnScanAll.setClickable(true);
            }
        }
    };
}
