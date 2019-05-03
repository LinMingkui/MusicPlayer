package com.musicplayer.ui.activity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.view.menu.MenuPopupHelper;
import android.support.v7.widget.PopupMenu;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.musicplayer.R;
import com.musicplayer.adapter.SongListAdapter;
import com.musicplayer.database.DataBase;
import com.musicplayer.ui.widget.PlayBarLayout;
import com.musicplayer.utils.BaseActivity;
import com.musicplayer.utils.Variate;

import java.lang.reflect.Field;

import static com.musicplayer.utils.AudioUtils.startPlay;
import static com.musicplayer.utils.MethodUtils.addFavorite;
import static com.musicplayer.utils.MethodUtils.addSongMenu;
import static com.musicplayer.utils.MethodUtils.deleteSong;
import static com.musicplayer.utils.MethodUtils.getSqlBaseOrder;
import static com.musicplayer.utils.MethodUtils.setPlayMessage;

public class RecentlyPlayActivity extends BaseActivity implements View.OnClickListener, SongListAdapter.OnSongListItemMenuClickListener, PlayBarLayout.OnPlaySongChangeListener {

    private Context mContext;
    private final String TAG = "*RecentlyPlayActivity";
    private DataBase dataBase;
    private SQLiteDatabase db;
    private Cursor cursorSong;
    private SongListAdapter songListAdapter;
    private SharedPreferences preferencesPlayList;
    private SharedPreferences.Editor editorPlayList;
    private SharedPreferences preferencesSet;
    private SharedPreferences.Editor editorSet;

    private PlayBarLayout playBarLayout;
    private ImageView imgTitleBack, imgTitleSearch, imgTitleMenu;
    private TextView tvTitleName;
    private ListView listRecentlyPlay;
    private String sql;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recently_play);
        init();
        setOnClickListener();
        sql = getSqlBaseOrder(Variate.recentlySongListTable, preferencesSet);
        cursorSong = db.rawQuery(sql, null);
        songListAdapter = new SongListAdapter(mContext, cursorSong, Variate.recentlySongListTable);
        songListAdapter.setOnItemMenuClickListener(this);
        listRecentlyPlay.setAdapter(songListAdapter);

        listRecentlyPlay.setOnItemClickListener((parent, view, position, id) -> {
            cursorSong.moveToPosition(position);
            startPlay(mContext, editorPlayList, Variate.recentlySongListTable, position);
            setPlayMessage(editorPlayList, cursorSong, Variate.recentlySongListTable, position);
            songListAdapter.changeData();
            songListAdapter.notifyDataSetChanged();
        });
    }


    private void setOnClickListener() {
        imgTitleBack.setOnClickListener(this);
        imgTitleSearch.setOnClickListener(this);
        imgTitleMenu.setOnClickListener(this);
        playBarLayout.setOnPlaySongChangeListener(this);
    }

    private void init() {
        mContext = RecentlyPlayActivity.this;
        dataBase = new DataBase(this, Variate.dataBaseName,
                null, 1);
        db = dataBase.getWritableDatabase();
        cursorSong = db.query(Variate.recentlySongListTable, null, null,
                null, null, null, null);

        preferencesPlayList = getSharedPreferences(Variate.playList, MODE_PRIVATE);
        editorPlayList = preferencesPlayList.edit();
        preferencesSet = getSharedPreferences(Variate.set, MODE_PRIVATE);
        editorSet = preferencesSet.edit();

        imgTitleBack = findViewById(R.id.img_title_back);
        imgTitleSearch = findViewById(R.id.img_title_search);
        imgTitleMenu = findViewById(R.id.img_title_menu);
        tvTitleName = findViewById(R.id.tv_title_name);
        tvTitleName.setText("最近播放");
        listRecentlyPlay = findViewById(R.id.list_recently_play);
        playBarLayout = findViewById(R.id.play_bar_layout);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.img_title_back:
                RecentlyPlayActivity.this.finish();
                break;
            case R.id.img_title_search:
                Intent intent = new Intent(mContext, LocalSearchActivity.class);
                intent.putExtra("tableName", Variate.recentlySongListTable);
                startActivityForResult(intent, 1);
                break;
            case R.id.img_title_menu:
                preferencesSet = getSharedPreferences(Variate.set, MODE_PRIVATE);
                editorSet = preferencesSet.edit();
                titleMenu();
                break;
        }
    }

    @SuppressLint("RestrictedApi")
    private void titleMenu() {
        final PopupMenu pm = new PopupMenu(mContext, imgTitleMenu);
        pm.getMenuInflater().inflate(R.menu.menu_pm_song_list_title, pm.getMenu());
        pm.getMenu().getItem(0).setVisible(false);
        pm.setOnMenuItemClickListener(menuItem -> {
            switch (menuItem.getItemId()) {
                case R.id.item_delete_all:
                    AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                    builder.setTitle("提示").setMessage("确定要清空列表？")
                            .setNegativeButton("取消", null)
                            .setPositiveButton("确定", (dialog, which) -> {
                                db.execSQL("delete from " + Variate.recentlySongListTable);
                                songListAdapter.changeData();
                                songListAdapter.notifyDataSetChanged();
                            }).show();
                    break;
            }
            return false;
        });
        //使用反射，强制显示菜单图标
        try {
            Field field = pm.getClass().getDeclaredField("mPopup");
            field.setAccessible(true);
            MenuPopupHelper mHelper = (MenuPopupHelper) field.get(pm);
            mHelper.setForceShowIcon(true);
            mHelper.show();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }

    }

    //音乐列表菜单点击事件
    @SuppressLint("RestrictedApi")
    public void onSongListItemMenuClick(View view, int position) {
        cursorSong.moveToPosition(position);
        PopupMenu pm = new PopupMenu(mContext, view.findViewById(R.id.img_song_list_menu));
        pm.getMenuInflater().inflate(R.menu.memu_pm_local_song_list, pm.getMenu());
        pm.setOnMenuItemClickListener(menuItem -> {
            songListItemMenuItemClick(menuItem.getItemId());
            return false;
        });
        //使用反射，强制显示菜单图标
        try {
            Field field = pm.getClass().getDeclaredField("mPopup");
            field.setAccessible(true);
            MenuPopupHelper mHelper = (MenuPopupHelper) field.get(pm);
            mHelper.setForceShowIcon(true);
            mHelper.show();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
    }

    //音乐列表菜单弹出dialog点击事件
    private void songListItemMenuItemClick(int dialogItemId) {
        switch (dialogItemId) {
            //添加或移除收藏
            case R.id.item_add_favorite:
                addFavorite(mContext, db, cursorSong);
                break;
            //添加到歌单
            case R.id.item_add_song_menu:
                View viewAddSongMenuDialog = getLayoutInflater().inflate(R.layout.dialog_add_song_menu, null);
                addSongMenu(mContext, db, cursorSong, viewAddSongMenuDialog);
                break;
            //删除
            case R.id.item_delete:
                deleteSong(mContext, db, Variate.recentlySongListTable, cursorSong, songListAdapter);
                cursorSong = db.rawQuery(sql, null);
                break;
        }
    }

    @Override
    public void OnPlaySongChange() {
        songListAdapter.changeData();
        songListAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onStart() {
        playBarLayout.mBindService(mContext);
        super.onStart();
    }

    @Override
    protected void onPause() {
        playBarLayout.mUnBindService(mContext);
        super.onPause();
    }

    protected void onDestroy() {
        super.onDestroy();
        dataBase.close();
        db.close();
        cursorSong.close();
        Log.e(TAG, "关闭Activity");
    }

}
