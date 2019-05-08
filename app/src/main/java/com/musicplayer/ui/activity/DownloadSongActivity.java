package com.musicplayer.ui.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.view.menu.MenuPopupHelper;
import android.support.v7.widget.PopupMenu;
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
import static com.musicplayer.utils.MethodUtils.addSongMenu;
import static com.musicplayer.utils.MethodUtils.addToFavorite;
import static com.musicplayer.utils.MethodUtils.deleteSong;
import static com.musicplayer.utils.MethodUtils.getSqlBaseOrder;
import static com.musicplayer.utils.MethodUtils.setPlayMessage;

public class DownloadSongActivity extends BaseActivity implements View.OnClickListener, SongListAdapter.OnSongListItemMenuClickListener, PlayBarLayout.OnPlaySongChangeListener {

    private Context mContext;
    private DataBase dataBase;
    private SQLiteDatabase db;
    private SharedPreferences preferencesPlayList;
    private SharedPreferences.Editor editorPlayList;
    private SharedPreferences preferencesSet;
    private SharedPreferences.Editor editorSet;
    private PlayBarLayout playBarLayout;
    private ImageView imgTitleBack, imgTitleSearch, imgTitleMenu;
    private TextView tvTitleName;
    private ListView lvSong;
    private SongListAdapter songListAdapter;
    private Cursor cursorSong;
    private String sql;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download_song);
        init();
        setOnClickListener();
        sql = getSqlBaseOrder(Variate.downloadSongListTable, preferencesSet);
        cursorSong = db.rawQuery(sql, null);
        cursorSong.moveToFirst();
        songListAdapter = new SongListAdapter(mContext, cursorSong, Variate.downloadSongListTable);
        songListAdapter.setOnItemMenuClickListener(this);
        lvSong.setAdapter(songListAdapter);
        lvSong.setOnItemClickListener((parent, view, position, id) -> {
            cursorSong.moveToPosition(position);
            startPlay(mContext, editorPlayList, Variate.downloadSongListTable, position);
            setPlayMessage(editorPlayList, cursorSong, Variate.downloadSongListTable, position);
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
        mContext = DownloadSongActivity.this;
        dataBase = new DataBase(mContext, Variate.dataBaseName, null, 1);
        db = dataBase.getWritableDatabase();
        preferencesPlayList = getSharedPreferences(Variate.playList, MODE_PRIVATE);
        editorPlayList = preferencesPlayList.edit();
        editorPlayList.apply();
        preferencesSet = getSharedPreferences(Variate.set, MODE_PRIVATE);
        editorSet = preferencesSet.edit();
        editorSet.apply();
        playBarLayout = findViewById(R.id.play_bar_layout);
        imgTitleBack = findViewById(R.id.img_title_back);
        imgTitleSearch = findViewById(R.id.img_title_search);
        imgTitleMenu = findViewById(R.id.img_title_menu);
        tvTitleName = findViewById(R.id.tv_title_name);
        tvTitleName.setText("下载管理");
        lvSong = findViewById(R.id.list_download_song);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.img_title_back:
                DownloadSongActivity.this.finish();
                break;
            case R.id.img_title_search:
                Intent intent = new Intent(mContext, LocalSearchActivity.class);
                intent.putExtra("tableName", Variate.downloadSongListTable);
                startActivityForResult(intent, 1);
                break;
            case R.id.img_title_menu:
                titleMenu(v);
                break;
        }
    }

    //音乐列表菜单点击事件
    @SuppressLint("RestrictedApi")
    public void onSongListItemMenuClick(View view, int position) {
        cursorSong.moveToPosition(position);
        PopupMenu pm = new PopupMenu(mContext, view.findViewById(R.id.img_song_list_menu));
        pm.getMenuInflater().inflate(R.menu.memu_pm_local_song_list, pm.getMenu());
        pm.getMenu().getItem(3).setVisible(false);
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
                addToFavorite(mContext, db, cursorSong);
                break;
            //添加到歌单
            case R.id.item_add_song_menu:
                View viewAddSongMenuDialog = getLayoutInflater().inflate(R.layout.dialog_add_song_menu, null);
                addSongMenu(mContext, db, cursorSong, viewAddSongMenuDialog);
                break;
            //删除
            case R.id.item_delete:
                deleteSong(mContext, db, Variate.downloadSongListTable, cursorSong, songListAdapter);
                cursorSong = db.rawQuery(sql, null);
                break;
        }
    }

    //解决报错 can only be called from within the same library group..
    @SuppressLint("RestrictedApi")
    private void titleMenu(View v) {
        PopupMenu pm = new PopupMenu(mContext, v);
        pm.getMenuInflater().inflate(R.menu.menu_pm_song_list_title, pm.getMenu());
        pm.setOnMenuItemClickListener(menuItem -> {
            boolean b = false;
            int order = Variate.SORT_TIME_DESC;
            switch (menuItem.getItemId()) {
                case R.id.item_delete_all:
                    AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                    builder.setTitle("提示")
                            .setMessage("确定清空列表？")
                            .setPositiveButton("确定", (dialog, which) -> {
                                db.execSQL("delete from " + Variate.downloadSongListTable);
                                songListAdapter.changeData();
                                songListAdapter.notifyDataSetChanged();
                                sql = getSqlBaseOrder(Variate.downloadSongListTable, preferencesSet);
                                cursorSong = db.rawQuery(sql, null);
                            })
                            .setNegativeButton("取消", null)
                            .show();
                    break;
                case R.id.item_select_sort_way:
                    order = preferencesSet.getInt(Variate.keyDownloadSort, Variate.SORT_TIME_DESC);
                    pm.getMenu().getItem(0).getSubMenu().getItem(order).setChecked(true);
                    break;
                case R.id.item_sort_by_name_asc:
                    order = Variate.SORT_NAME_ASC;
                    b = true;
                    break;
                case R.id.item_sort_by_name_desc:
                    order = Variate.SORT_NAME_DESC;
                    b = true;
                    break;
                case R.id.item_sort_by_singer_asc:
                    order = Variate.SORT_SINGER_ASC;
                    b = true;
                    break;
                case R.id.item_sort_by_singer_desc:
                    order = Variate.SORT_SINGER_DESC;
                    b = true;
                    break;
                case R.id.item_sort_by_time_asc:
                    order = Variate.SORT_TIME_ASC;
                    b = true;
                    break;
                case R.id.item_sort_by_time_desc:
                    order = Variate.SORT_TIME_DESC;
                    b = true;
                    break;
            }
            if (b) {
                editorSet.putInt(Variate.keyDownloadSort, order);
                editorSet.apply();
                songListAdapter.changeData();
                songListAdapter.notifyDataSetChanged();
                sql = getSqlBaseOrder(Variate.downloadSongListTable, preferencesSet);
                cursorSong = db.rawQuery(sql, null);
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

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        cursorSong = db.rawQuery(sql, null);
        songListAdapter.changeData();
        songListAdapter.notifyDataSetChanged();
    }

    @Override
    public void OnPlaySongChange() {
        songListAdapter.changeData();
        songListAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onStart() {
        super.onStart();
        playBarLayout.mBindService(mContext);
    }

    @Override
    protected void onRestart() {
        super.onRestart();
    }

    @Override
    protected void onPause() {
        playBarLayout.mUnBindService(mContext);
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        setResult(1);
        if (dataBase != null) {
            dataBase.close();
        }
        if (db != null) {
            db.close();
        }
        if (cursorSong != null) {
            cursorSong.close();
        }
        super.onDestroy();
    }

}
