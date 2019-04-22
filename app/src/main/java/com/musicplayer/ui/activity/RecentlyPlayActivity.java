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
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.musicplayer.R;
import com.musicplayer.adapter.SongListAdapter;
import com.musicplayer.database.DataBase;
import com.musicplayer.utils.BaseActivity;
import com.musicplayer.utils.StaticVariate;

import static com.musicplayer.utils.AudioUtils.startPlay;
import static com.musicplayer.utils.MethodUtils.addFavorite;
import static com.musicplayer.utils.MethodUtils.addSongMenu;
import static com.musicplayer.utils.MethodUtils.deleteSong;
import static com.musicplayer.utils.MethodUtils.showOrderPopupMenu;

public class RecentlyPlayActivity extends BaseActivity implements View.OnClickListener, SongListAdapter.OnSongListItemMenuClickListener {

    private Context mContext;
    private final String TAG = "*RecentlyPlayActivity";
    private DataBase dataBase;
    private SQLiteDatabase db;
    private Cursor cursorSong;
    private SongListAdapter songListAdapter;
    private SetListPlayIconThread setListPlayIconThread;
    private int songListItemPosition;
    private boolean run = true;
    private SharedPreferences preferencesPlayList;
    private SharedPreferences.Editor editorPlayList;
    private SharedPreferences preferencesSet;
    private SharedPreferences.Editor editorSet;

    private static ProgressDialog progressDialog;
    private ImageView imgTitleBack, imgTitleSearch, imgTitleMenu;
    private TextView tvTitleName;
    private ListView listRecentlyPlay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recently_play);
        init();
        setOnClickListener();
        cursorSong = db.rawQuery("select * from "
                + StaticVariate.recentlySongListTable
                + " order by " + StaticVariate.addTime
                + " desc ", null);
        songListAdapter = new SongListAdapter(mContext,
                cursorSong, StaticVariate.recentlySongListTable);
        songListAdapter.setOnItemMenuClickListener(this);
        listRecentlyPlay.setAdapter(songListAdapter);

        listRecentlyPlay.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                startPlay(mContext, editorPlayList,
                        StaticVariate.recentlySongListTable, position);
            }
        });
        setListPlayIconThread = new SetListPlayIconThread();
        setListPlayIconThread.start();
    }


    private void setOnClickListener() {
        imgTitleBack.setOnClickListener(this);
        imgTitleSearch.setOnClickListener(this);
        imgTitleMenu.setOnClickListener(this);
    }

    private void init() {
        mContext = RecentlyPlayActivity.this;
        dataBase = new DataBase(this, StaticVariate.dataBaseName,
                null, 1);
        db = dataBase.getWritableDatabase();
        cursorSong = db.query(StaticVariate.recentlySongListTable, null, null,
                null, null, null, null);

        preferencesPlayList = getSharedPreferences(StaticVariate.playList, MODE_PRIVATE);
        editorPlayList = preferencesPlayList.edit();

        imgTitleBack = findViewById(R.id.img_title_back);
        imgTitleSearch = findViewById(R.id.img_title_search);
        imgTitleMenu = findViewById(R.id.img_title_menu);
        tvTitleName = findViewById(R.id.tv_title_name);
        tvTitleName.setText("最近播放");
        listRecentlyPlay = findViewById(R.id.list_recently_play);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.img_title_back:
                RecentlyPlayActivity.this.finish();
                break;
            case R.id.img_title_search:
                Intent intent = new Intent(mContext, LocalSearchActivity.class);
                intent.putExtra("tableName", StaticVariate.recentlySongListTable);
                startActivityForResult(intent, 1);
                break;
            case R.id.img_title_menu:
                preferencesSet = getSharedPreferences(StaticVariate.keySet, MODE_PRIVATE);
                editorSet = preferencesSet.edit();
                titleMenu();
                break;
        }
    }

    private void titleMenu() {
        final PopupMenu pm = new PopupMenu(mContext, imgTitleMenu);
        pm.getMenuInflater().inflate(R.menu.menu_popupmenu_song_list_title, pm.getMenu());
        pm.getMenu().getItem(0).setVisible(false);
        pm.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.item_delete_all:
                        pm.dismiss();
                        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                        builder.setTitle("提示").setMessage("确定要清空列表？")
                                .setNegativeButton("取消", null)
                                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        db.execSQL("delete from "+ StaticVariate.recentlySongListTable);
                                        songListAdapter.changeData();
                                        songListAdapter.notifyDataSetChanged();
                                    }
                                }).show();
                        break;

                }
                return false;
            }
        });
        pm.show();

    }

    //音乐列表菜单点击事件
    public void onSongListItemMenuClick(int position) {
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
                deleteSong(mContext, db, StaticVariate.recentlySongListTable,
                        cursorSong, preferencesPlayList, songListItemPosition, songListAdapter);
                cursorSong = db.rawQuery("select * from " + StaticVariate.localSongListTable, null);
                break;
        }
    }

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
            StaticVariate.isSetListPlayIcon = false;
            songListAdapter.changeData();
            songListAdapter.notifyDataSetChanged();
            cursorSong = db.rawQuery("select * from "
                    + StaticVariate.recentlySongListTable
                    + " order by " + StaticVariate.addTime
                    + " desc ", null);
        }
    };

    protected void onDestroy() {
        super.onDestroy();
        run = false;
//        setResult(result);
        dataBase.close();
        db.close();
        cursorSong.close();
        Log.e(TAG, "关闭Activity");
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case 1:
                if (resultCode == 1) {
                    songListAdapter.changeData();
                    songListAdapter.notifyDataSetChanged();
                }
                break;
        }
    }
}
