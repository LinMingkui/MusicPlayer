package com.musicplayer.ui.activity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.PopupMenu;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.musicplayer.R;
import com.musicplayer.adapter.SongListAdapter;
import com.musicplayer.database.DataBase;
import com.musicplayer.utils.BaseActivity;
import com.musicplayer.utils.Song;
import com.musicplayer.utils.StaticVariate;

import java.util.ArrayList;
import java.util.Locale;

import static com.musicplayer.utils.AudioUtils.getAllSongs;
import static com.musicplayer.utils.AudioUtils.startPlay;
import static com.musicplayer.utils.MethodUtils.addFavorite;
import static com.musicplayer.utils.MethodUtils.addSongMenu;
import static com.musicplayer.utils.MethodUtils.deleteSong;
import static com.musicplayer.utils.MethodUtils.getSqlBaseOrder;
import static com.musicplayer.utils.MethodUtils.insertSong;
import static com.musicplayer.utils.MethodUtils.showOrderPopupMenu;


public class LocalSongActivity extends BaseActivity implements View.OnClickListener,
        SongListAdapter.OnSongListItemMenuClickListener {

    private Context mContext;
    private String TAG = "*LocalSongActivity";
    private DataBase dataBase;
    private SQLiteDatabase db;

    private SharedPreferences preferencesPlayList;
    private SharedPreferences.Editor editorPlayList;
    private SharedPreferences preferencesSet;
    private SharedPreferences.Editor editorSet;

    private ListView listLocalMusic;
    private SongListAdapter songListAdapter;
    private ImageView imgTitleBack, imgTitleSearch, imgTitleMenu;
    private TextView tvTitleName;
    private LinearLayout linearLayoutTitle;

    private SetListPlayIconThread setListPlayIconThread;
    private ArrayList<Song> song;
    private Cursor cursorSong;
    private static ProgressDialog progressDialog;
    private boolean isOver = false;
    private boolean run = true;
    private int songListItemPosition;
    private int result = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_local_song);

        init();
        setClickListener();
        String sql = getSqlBaseOrder(StaticVariate.localSongListTable, preferencesSet);
        Log.e(TAG, sql);
        cursorSong = db.rawQuery(sql, null);

        //初始化本地音乐列表
        if (cursorSong.getCount() == 0) {
            song = getAllSongs(this);
            songListAdapter = new SongListAdapter(mContext,
                    song, StaticVariate.localSongListTable);
            listLocalMusic.setAdapter(songListAdapter);
            SaveSongThread saveSongThread = new SaveSongThread(song);
            saveSongThread.start();
        } else {
            isOver = true;
            cursorSong.moveToFirst();
            songListAdapter = new SongListAdapter(LocalSongActivity.this,
                    cursorSong, StaticVariate.localSongListTable);
            listLocalMusic.setAdapter(songListAdapter);
        }

        //音乐列表点击事件
        listLocalMusic.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (isOver) {
                    startPlay(mContext, editorPlayList,
                            StaticVariate.localSongListTable, position);
                    songListAdapter.changeData();
                    songListAdapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(mContext, "等待初始化完成", Toast.LENGTH_SHORT).show();
                }
            }
        });
        //音乐列表菜单点击事件监听器
        songListAdapter.setOnItemMenuClickListener(this);

    }

    //音乐列表菜单点击事件
    public void onSongListItemMenuClick(int position) {
        if (isOver) {
            songListItemPosition = position;
            cursorSong.moveToPosition(songListItemPosition);
            AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
            builder.setItems(new String[]{"添加或移除收藏", "添加到歌单", "从列表删除"}, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Log.e(TAG, "which" + which);
                    songListItemMenuItemClick(which);
                }
            }).show();
        } else {
            Toast.makeText(mContext, "等待初始化完成", Toast.LENGTH_SHORT).show();
        }
    }

    //音乐列表菜单弹出dialog点击事件
    private void songListItemMenuItemClick(int dialogItemPosition) {
        switch (dialogItemPosition) {
            //添加或移除收藏
            case 0:
                addFavorite(mContext, db, cursorSong);
                break;
            //添加到歌单
            case 1:
                View viewAddSongMenuDialog = getLayoutInflater().inflate(R.layout.dialog_add_song_menu, null);
                addSongMenu(mContext, db, cursorSong, viewAddSongMenuDialog);
                break;
            //删除
            case 2:
                deleteSong(mContext, db, StaticVariate.localSongListTable,
                        cursorSong, preferencesPlayList, songListItemPosition, songListAdapter);
                cursorSong = db.rawQuery("select * from " + StaticVariate.localSongListTable, null);
                break;
        }
    }

    //把音乐列表存入数据库
    public class SaveSongThread extends Thread {
        ArrayList<Song> song;

        SaveSongThread(ArrayList<Song> song) {
            this.song = song;
        }

        public void run() {
            for (int i = 0; i < song.size(); i++) {
                Log.e(TAG, song.get(i).getTitle());
                insertSong(db, StaticVariate.localSongListTable, song.get(i));
            }
            Message message = new Message();
            message.what = 3;
            changeUIHandler.sendMessage(message);
            isOver = true;
            cursorSong = db.rawQuery("select * from " + StaticVariate.localSongListTable, null);
            Log.e(TAG, "初始化完成，共发现" + cursorSong.getCount() + "首音乐");
        }
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.img_title_back:
                LocalSongActivity.this.finish();
                break;
            case R.id.img_title_search:
                Intent intent = new Intent(mContext, LocalSearchActivity.class);
                intent.putExtra("tableName", StaticVariate.localSongListTable);
                startActivityForResult(intent, 1);
                break;
            case R.id.img_title_menu:
                titleMenu();
                break;
        }
    }


    private void titleMenu() {
        final PopupMenu pm = new PopupMenu(mContext, imgTitleMenu);
        pm.getMenuInflater().inflate(R.menu.menu_popupmenu_local_song, pm.getMenu());
        pm.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.item_scan_local_song:
                        Intent intent = new Intent(mContext, ScanLocalSongActivity.class);
                        startActivityForResult(intent, 2);
                        pm.dismiss();
                        break;
                    case R.id.item_select_order_way:
                        Log.e(TAG, "排序");
//                        pm.dismiss();
                        progressDialog = new ProgressDialog(mContext);
                        progressDialog.setMessage("正在存入数据库");
                        progressDialog.setCancelable(false);
                        showOrderPopupMenu(mContext, imgTitleMenu, db,
                                StaticVariate.localSongListTable, preferencesSet,
                                editorSet, changeUIHandler, progressDialog,
                                StaticVariate.keyLocalOrder);

                        break;

                }
                return false;
            }
        });
        pm.show();

    }

    private void setClickListener() {
        imgTitleBack.setOnClickListener(this);
        imgTitleSearch.setOnClickListener(this);
        imgTitleMenu.setOnClickListener(this);
    }

    private void init() {
        mContext = LocalSongActivity.this;
        preferencesPlayList = getSharedPreferences(StaticVariate.playList, MODE_PRIVATE);
        editorPlayList = preferencesPlayList.edit();
        preferencesSet = getSharedPreferences(StaticVariate.keySet, MODE_PRIVATE);
        editorSet = preferencesSet.edit();
        dataBase = new DataBase(this, StaticVariate.dataBaseName, null, 1);
        db = dataBase.getWritableDatabase();
        db.setLocale(Locale.SIMPLIFIED_CHINESE);

        linearLayoutTitle = findViewById(R.id.linear_layout_title);
        listLocalMusic = findViewById(R.id.list_local_song);
        tvTitleName = findViewById(R.id.tv_title_name);
        tvTitleName.setText("本地音乐");
        imgTitleBack = findViewById(R.id.img_title_back);
        imgTitleSearch = findViewById(R.id.img_title_search);
        imgTitleMenu = findViewById(R.id.img_title_menu);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
//        switch (requestCode) {
//            case 1:
//                if (resultCode == 1) {
        songListAdapter.changeData();
        songListAdapter.notifyDataSetChanged();
//                }
//                break;
//        }
    }

    //设置列表播放图标线程
    class SetListPlayIconThread extends Thread {
        public void run() {
            Log.e(TAG, "SetListPlayIconThread start");
            while (run) {
                if (StaticVariate.isSetListPlayIcon) {
                    Message message = new Message();
                    message.what = 1;
                    changeUIHandler.sendMessage(message);
                }
                try {
                    sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    break;
                }
            }
            Log.e(TAG, "SetListPlayIconThread close");
        }
    }

    private Handler changeUIHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    songListAdapter.changeData();
                    songListAdapter.notifyDataSetChanged();
                    StaticVariate.isSetListPlayIcon = false;
                    break;
                case StaticVariate.ORDER_COMPLETE:
                    cursorSong = db.rawQuery("select * from " +
                            StaticVariate.localSongListTable, null);
                    songListAdapter.changeData();
                    songListAdapter.notifyDataSetChanged();
                    progressDialog.dismiss();
                    break;
                case 3:
                    Toast.makeText(mContext,
                            "初始化完成", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };

    @Override
    protected void onStart() {
        super.onStart();
        setListPlayIconThread = new SetListPlayIconThread();
        setListPlayIconThread.start();
        run = true;
    }

    @Override
    protected void onStop() {
        super.onStop();
        run = false;
    }

    protected void onDestroy() {
        super.onDestroy();
        setResult(result);
        dataBase.close();
        db.close();
        cursorSong.close();
        Log.e(TAG, "关闭Activity");
    }
}
