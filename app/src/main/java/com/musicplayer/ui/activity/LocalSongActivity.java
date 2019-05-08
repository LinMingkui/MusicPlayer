package com.musicplayer.ui.activity;

import android.annotation.SuppressLint;
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
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.musicplayer.R;
import com.musicplayer.adapter.SongListAdapter;
import com.musicplayer.database.DataBase;
import com.musicplayer.ui.widget.PlayBarLayout;
import com.musicplayer.utils.BaseActivity;
import com.musicplayer.utils.Song;
import com.musicplayer.utils.Variate;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Locale;

import static com.musicplayer.utils.AudioUtils.getAllSongs;
import static com.musicplayer.utils.AudioUtils.startPlay;
import static com.musicplayer.utils.MethodUtils.addSongMenu;
import static com.musicplayer.utils.MethodUtils.addToFavorite;
import static com.musicplayer.utils.MethodUtils.deleteSong;
import static com.musicplayer.utils.MethodUtils.getSqlBaseOrder;
import static com.musicplayer.utils.MethodUtils.saveSong;
import static com.musicplayer.utils.MethodUtils.setPlayMessage;


public class LocalSongActivity extends BaseActivity implements View.OnClickListener,
        SongListAdapter.OnSongListItemMenuClickListener, PlayBarLayout.OnPlaySongChangeListener {

    private Context mContext;
    private String TAG = "*LocalSongActivity";
    private DataBase dataBase;
    private SQLiteDatabase db;

    private SharedPreferences preferencesPlayList;
    private SharedPreferences.Editor editorPlayList;
    private SharedPreferences preferencesSet;
    private SharedPreferences.Editor editorSet;

    private PlayBarLayout playBarLayout;
    private ListView lvSong;
    private SongListAdapter songListAdapter;
    private ImageView imgTitleBack, imgTitleSearch, imgTitleMenu;
    private TextView tvTitleName;
    private LinearLayout linearLayoutLoading;

    private List<Song> songs;
    private Cursor cursorSong;
    private int result = 1;
    private String sql;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_local_song);

        init();
        setClickListener();

        //初始化本地音乐列表
        if (preferencesSet.getBoolean(Variate.keyIsInitLocalList, true)) {
            linearLayoutLoading.setVisibility(View.VISIBLE);
            songs = getAllSongs(this);
            SaveSongThread saveSongThread = new SaveSongThread();
            saveSongThread.start();
        } else {
            sql = getSqlBaseOrder(Variate.localSongListTable, preferencesSet);
            Log.e(TAG, sql);
            cursorSong = db.rawQuery(sql, null);
            cursorSong.moveToFirst();
            songListAdapter = new SongListAdapter(LocalSongActivity.this,
                    cursorSong, Variate.localSongListTable);
            songListAdapter.setOnItemMenuClickListener(this);
            lvSong.setAdapter(songListAdapter);
        }

        //音乐列表点击事件
        lvSong.setOnItemClickListener((parent, view, position, id) -> {
            cursorSong.moveToPosition(position);
            startPlay(mContext, editorPlayList, Variate.localSongListTable, position);
            setPlayMessage(editorPlayList, cursorSong, Variate.localSongListTable, position);
            songListAdapter.changeData();
            songListAdapter.notifyDataSetChanged();
        });
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
                deleteSong(mContext, db, Variate.localSongListTable, cursorSong, songListAdapter);
                cursorSong = db.rawQuery(sql, null);
                break;
        }
    }

    //上下一曲
    @Override
    public void OnPlaySongChange() {
        if (songListAdapter != null) {
            songListAdapter.changeData();
            songListAdapter.notifyDataSetChanged();
        }
    }

    //把音乐列表存入数据库
    public class SaveSongThread extends Thread {
        public void run() {
            saveSong(db, songs);
            editorSet.putBoolean(Variate.keyIsInitLocalList, false);
            editorSet.apply();
            sql = getSqlBaseOrder(Variate.localSongListTable, preferencesSet);
            Log.e(TAG, sql);
            cursorSong = db.rawQuery(sql, null);
            runOnUiThread(() -> {
                linearLayoutLoading.setVisibility(View.GONE);
                songListAdapter = new SongListAdapter(mContext,
                        cursorSong, Variate.localSongListTable);
                Toast.makeText(mContext, "初始化完成，共发现" + cursorSong.getCount() + "首音乐", Toast.LENGTH_SHORT).show();
                lvSong.setAdapter(songListAdapter);
                songListAdapter.setOnItemMenuClickListener(LocalSongActivity.this);
            });
        }
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.img_title_back:
                LocalSongActivity.this.finish();
                break;
            case R.id.img_title_search:
                Intent intent = new Intent(mContext, LocalSearchActivity.class);
                intent.putExtra("tableName", Variate.localSongListTable);
                startActivityForResult(intent, 1);
                break;
            case R.id.img_title_menu:
                titleMenu();
                break;
        }
    }

    //解决报错 can only be called from within the same library group..
    @SuppressLint("RestrictedApi")
    private void titleMenu() {
        final PopupMenu pm = new PopupMenu(mContext, imgTitleMenu);
        pm.getMenuInflater().inflate(R.menu.menu_pm_local_song, pm.getMenu());
        pm.setOnMenuItemClickListener(menuItem -> {
            boolean b = false;
            int order = Variate.SORT_TIME_DESC;
            switch (menuItem.getItemId()) {
                case R.id.item_scan_local_song:
                    Intent intent = new Intent(mContext, ScanLocalSongActivity.class);
                    startActivityForResult(intent, 2);
                    pm.dismiss();
                    break;
                case R.id.item_select_sort_way:
                    Log.e(TAG, "排序");
                    order = preferencesSet.getInt(Variate.keyLocalSort, Variate.SORT_TIME_DESC);
                    pm.getMenu().getItem(1).getSubMenu().getItem(order).setChecked(true);
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
                editorSet.putInt(Variate.keyLocalSort, order);
                editorSet.apply();
                sql = getSqlBaseOrder(Variate.localSongListTable, preferencesSet);
                cursorSong = db.rawQuery(sql, null);
                songListAdapter.changeData();
                songListAdapter.notifyDataSetChanged();
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

    private void setClickListener() {
        imgTitleBack.setOnClickListener(this);
        imgTitleSearch.setOnClickListener(this);
        imgTitleMenu.setOnClickListener(this);
        playBarLayout.setOnPlaySongChangeListener(this);
    }

    @SuppressLint("CommitPrefEdits")
    private void init() {
        mContext = LocalSongActivity.this;
        preferencesPlayList = getSharedPreferences(Variate.playList, MODE_PRIVATE);
        editorPlayList = preferencesPlayList.edit();
        editorPlayList.apply();
        preferencesSet = getSharedPreferences(Variate.set, MODE_PRIVATE);
        editorSet = preferencesSet.edit();
        editorSet.apply();
        dataBase = new DataBase(this, Variate.dataBaseName, null, 1);
        db = dataBase.getWritableDatabase();
        db.setLocale(Locale.SIMPLIFIED_CHINESE);

        playBarLayout = findViewById(R.id.play_bar_layout);
        linearLayoutLoading = findViewById(R.id.ll_loading);
        lvSong = findViewById(R.id.list_local_song);
        tvTitleName = findViewById(R.id.tv_title_name);
        tvTitleName.setText("本地音乐");
        imgTitleBack = findViewById(R.id.img_title_back);
        imgTitleSearch = findViewById(R.id.img_title_search);
        imgTitleMenu = findViewById(R.id.img_title_menu);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        cursorSong = db.rawQuery(sql, null);
        songListAdapter.changeData();
        songListAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onStart() {
        super.onStart();
        playBarLayout.mBindService(mContext);
        if (songListAdapter != null) {
            songListAdapter.changeData();
            songListAdapter.notifyDataSetChanged();
        }
    }

    @Override
    protected void onPause() {
        playBarLayout.mUnBindService(mContext);
        super.onPause();
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
