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
import static com.musicplayer.utils.MethodUtils.addSongMenu;
import static com.musicplayer.utils.MethodUtils.deleteSong;
import static com.musicplayer.utils.MethodUtils.getSqlBaseOrder;
import static com.musicplayer.utils.MethodUtils.showOrderPopupMenu;

public class FavoriteSongActivity extends BaseActivity implements View.OnClickListener, SongListAdapter.OnSongListItemMenuClickListener {

    private Context mContext;
    private final String TAG = "*FavoriteSongActivity";
    private DataBase dataBase = new DataBase(this, StaticVariate.dataBaseName,
            null, 1);
    private SQLiteDatabase db;
    private SharedPreferences preferencesPlayList;
    private SharedPreferences.Editor editorPlayList;
    private SetListPlayIconThread setListPlayIconThread;
    private SharedPreferences preferencesSet;
    private SharedPreferences.Editor editorSet;
    private Cursor cursorSong;
    private SongListAdapter songListAdapter;

    private static ProgressDialog progressDialog;
    private ImageView imgTitleBack,imgTitleSearch,imgTitleMenu;
    private TextView tvTitleName;
    private ListView listFavoriteSong;
    private int songListItemPosition,result;
    private boolean run = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorite_song);
        init();
        setOnClickListener();
        String sql = getSqlBaseOrder(StaticVariate.favoriteSongListTable,preferencesSet);
        Log.e(TAG,sql);
        cursorSong = db.rawQuery(sql, null);
        Log.e(TAG, "" + cursorSong.getCount());
        songListAdapter = new SongListAdapter(mContext,
                cursorSong, StaticVariate.favoriteSongListTable);
        songListAdapter.setOnItemMenuClickListener(this);
        listFavoriteSong.setAdapter(songListAdapter);
        //音乐列表点击事件
        listFavoriteSong.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                startPlay(mContext,editorPlayList,
                        StaticVariate.favoriteSongListTable,position);
                songListAdapter.changeData();
                songListAdapter.notifyDataSetChanged();
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
        mContext = FavoriteSongActivity.this;
        preferencesPlayList = getSharedPreferences(StaticVariate.playList, MODE_PRIVATE);
        editorPlayList = preferencesPlayList.edit();
        preferencesSet = getSharedPreferences(StaticVariate.keySet,MODE_PRIVATE);
        editorSet = preferencesSet.edit();
        db = dataBase.getWritableDatabase();
        imgTitleBack = findViewById(R.id.img_title_back);
        imgTitleSearch = findViewById(R.id.img_title_search);
        imgTitleMenu = findViewById(R.id.img_title_menu);
        tvTitleName = findViewById(R.id.tv_title_name);
        tvTitleName.setText("我的收藏");
        listFavoriteSong = findViewById(R.id.list_favorite_song);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.img_title_back:
                FavoriteSongActivity.this.finish();
                break;
            case R.id.img_title_search:
                Intent intent = new Intent(mContext,LocalSearchActivity.class);
                intent.putExtra("tableName",StaticVariate.favoriteSongListTable);
                startActivityForResult(intent,1);
                break;
            case R.id.img_title_menu:
                titleMenu();
                break;
        }
    }

    private void titleMenu() {
        final PopupMenu pm = new PopupMenu(mContext, imgTitleMenu);
        pm.getMenuInflater().inflate(R.menu.menu_popupmenu_song_list_title, pm.getMenu());
        pm.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.item_select_order_way:
                        pm.dismiss();
                        progressDialog = new ProgressDialog(mContext);
                        progressDialog.setMessage("正在存入数据库");
                        progressDialog.setCancelable(false);
                        showOrderPopupMenu(mContext, imgTitleMenu, db,
                                StaticVariate.favoriteSongListTable, preferencesSet,
                                editorSet, changeUIHandler, progressDialog,
                                StaticVariate.keyFavoriteOrder);
                        break;
                    case R.id.item_delete_all:
                        pm.dismiss();
                        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                        builder.setTitle("提示").setMessage("确定要清空列表？")
                                .setNegativeButton("取消", null)
                                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        db.execSQL("delete from "+ StaticVariate.favoriteSongListTable);
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

    @Override
    public void onSongListItemMenuClick(int position) {
        songListItemPosition = position;
        cursorSong.moveToPosition(songListItemPosition);
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        String[] item = new String[]{"添加到歌单", "从列表删除"};
        builder.setItems(item, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                songListItemMenuItemClick(which);
            }
        }).show();
    }

    private void songListItemMenuItemClick(int dialogItemPosition) {
        switch (dialogItemPosition) {
            //添加到歌单
            case 0:
                View viewAddSongMenuDialog = getLayoutInflater().inflate(R.layout.dialog_add_song_menu, null);
                addSongMenu(mContext,db,cursorSong,viewAddSongMenuDialog);
                break;
            //删除
            case 1:
                deleteSong(mContext,db,StaticVariate.favoriteSongListTable,
                        cursorSong,preferencesPlayList,songListItemPosition,songListAdapter);
                cursorSong = db.rawQuery("select * from " + StaticVariate.localSongListTable, null);
                break;
        }
    }

    class SetListPlayIconThread extends Thread {
        public void run() {
            Log.e(TAG,"SetListPlayIconThread start");
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
            Log.e(TAG,"SetListPlayIconThread close");
        }
    }

    private Handler changeUIHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what){
                case 1:
                    StaticVariate.isSetListPlayIcon = false;
                    songListAdapter.changeData();
                    songListAdapter.notifyDataSetChanged();
                    cursorSong = db.rawQuery("select * from "
                            +StaticVariate.favoriteSongListTable, null);
                    break;
                case StaticVariate.ORDER_COMPLETE:
                    cursorSong = db.rawQuery("select * from " +
                            StaticVariate.favoriteSongListTable, null);
                    songListAdapter.changeData();
                    songListAdapter.notifyDataSetChanged();
                    progressDialog.dismiss();
                    break;
            }

        }
    };

    protected void onDestroy() {
        super.onDestroy();
        run = false;
        setResult(result);
        dataBase.close();
        db.close();
        cursorSong.close();
        Log.e(TAG,"关闭Activity");
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case 1:
                if(resultCode == 1) {
                    songListAdapter.changeData();
                    songListAdapter.notifyDataSetChanged();
                }
                break;
        }
    }
}
