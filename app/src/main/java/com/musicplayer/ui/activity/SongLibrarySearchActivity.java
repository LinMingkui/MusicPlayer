package com.musicplayer.ui.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.view.menu.MenuPopupHelper;
import android.support.v7.widget.PopupMenu;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.musicplayer.R;
import com.musicplayer.database.DataBase;
import com.musicplayer.service.PlayService;
import com.musicplayer.ui.widget.PlayBarLayout;
import com.musicplayer.utils.BaseActivity;
import com.musicplayer.utils.DownloadUtils;
import com.musicplayer.utils.NetworkUtils;
import com.musicplayer.utils.Song;
import com.musicplayer.utils.Variate;

import java.lang.reflect.Field;

import static com.musicplayer.utils.MethodUtils.addSongMenu;
import static com.musicplayer.utils.MethodUtils.addToFavorite;
import static com.musicplayer.utils.Song.songToCursor;

public class SongLibrarySearchActivity extends BaseActivity {

    private String TAG = "*SongLibrarySearchActivity";
    private Context mContext = SongLibrarySearchActivity.this;
    private DataBase dataBase;
    private SQLiteDatabase db;
    private LinearLayout linearLayout;
    private LinearLayout linearLayoutLoad;
    private TextView tvName,tvSinger;
    private ImageView imgMenu;
    private ImageView imgSongType;
    private PlayBarLayout playBarLayout;
    private String id;
    private String type;
    private Song mSong;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_song_library_search);
        dataBase = new DataBase(mContext,Variate.dataBaseName,null,1);
        db = dataBase.getWritableDatabase();
        tvName = findViewById(R.id.tv_name);
        tvSinger = findViewById(R.id.tv_singer);
        imgMenu = findViewById(R.id.img_song_list_menu);
        imgSongType = findViewById(R.id.img_song_type);
        playBarLayout = findViewById(R.id.play_bar_layout);
        linearLayout = findViewById(R.id.ll_song);
        linearLayoutLoad = findViewById(R.id.ll_loading);
        linearLayoutLoad.setVisibility(View.VISIBLE);

        Intent intent = getIntent();
        id = intent.getStringExtra("id");
        type = intent.getStringExtra("type");

        NetworkUtils networkUtils = new NetworkUtils();
        networkUtils.getSongInfo(id,type, Variate.FILTER_ID);
        networkUtils.setOnGetSongInfoListener(song -> {
            mSong = song;
            Variate.song = song;
            runOnUiThread(() -> {
                tvName.setText(song.getSongName());
                tvSinger.setText(song.getSinger());
                linearLayout.setVisibility(View.VISIBLE);
                linearLayoutLoad.setVisibility(View.GONE);
            });
        });

        if(type.equals("qq")){
            imgSongType.setImageResource(R.drawable.ic_song_type_qq);
        }else if (type.equals("kugou")){
            imgSongType.setImageResource(R.drawable.ic_song_type_kg);
        }else if (type.equals("netease")){
            imgSongType.setImageResource(R.drawable.ic_song_type_wyy);
        }

        linearLayout.setOnClickListener(v -> {
            Intent intent1 = new Intent(mContext, PlayService.class);
            intent1.putExtra(Variate.keyIsLocal, false);
            startService(intent1);
        });
        imgMenu.setOnClickListener(v -> menu(v));
    }

    //音乐列表item imgMenu点击事件
    @SuppressLint("RestrictedApi")
    public void menu(View view) {
        PopupMenu pm = new PopupMenu(mContext, view);
        pm.getMenuInflater().inflate(R.menu.memu_pm_local_song_list, pm.getMenu());
        pm.getMenu().getItem(2).setVisible(false);
        pm.setOnMenuItemClickListener(menuItem -> {
            menuItemClick(menuItem.getItemId());
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
    private void menuItemClick(int dialogItemId) {
        switch (dialogItemId) {
            //添加或移除收藏
            case R.id.item_add_favorite:
                addToFavorite(mContext, db, mSong);
                break;
            //添加到歌单
            case R.id.item_add_song_menu:
                View viewAddSongMenuDialog = getLayoutInflater().inflate(R.layout.dialog_add_song_menu, null);
                addSongMenu(mContext, db, songToCursor(mSong), viewAddSongMenuDialog);
                break;
            //下载
            case R.id.item_download:
                DownloadUtils downloadUtils = new DownloadUtils(mContext, null, mSong);
                downloadUtils.startDownload();
                break;
        }
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
}
