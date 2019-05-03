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
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
import static com.musicplayer.utils.MethodUtils.addSong;
import static com.musicplayer.utils.MethodUtils.addSongMenu;
import static com.musicplayer.utils.MethodUtils.deleteSong;
import static com.musicplayer.utils.MethodUtils.setPlayMessage;

public class LocalSearchActivity extends BaseActivity implements View.OnClickListener, TextWatcher, SongListAdapter.OnSongListItemMenuClickListener, PlayBarLayout.OnPlaySongChangeListener {

    private Context mContext;
    private final String TAG = "*LocalSearchActivity";
    private DataBase dataBase;
    private SQLiteDatabase db;
    private Cursor cursorSong = null;
    private SharedPreferences preferencesPlayList;
    private SharedPreferences.Editor editorPlayList;
    private LinearLayout linearLayoutParent;
    private EditText edtSearch;
    private TextView tvNoResultHint;
    private ImageView imgTitleBack, imgClear;
    private ListView lvLocalSearch;
    private String fromTable, searchText;
    private SongListAdapter songListAdapter;
    private int editKey = 0, threadKey = 0;
    private int songListItemPosition;
    private PlayBarLayout playBarLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_local_search);

        init();
        setOnClickListener();
        songListAdapter = new SongListAdapter(mContext,
                cursorSong, Variate.localSearchSongListTable);
        songListAdapter.setOnItemMenuClickListener(this);

        edtSearch.addTextChangedListener(this);
        lvLocalSearch.setOnItemClickListener((parent, view, position, id) -> {
            if (editKey == threadKey) {
                cursorSong.moveToPosition(position);
                startPlay(mContext, editorPlayList, Variate.localSearchSongListTable, position);
                setPlayMessage(editorPlayList, cursorSong, Variate.localSearchSongListTable, position);
                songListAdapter.changeData();
                songListAdapter.notifyDataSetChanged();
            } else {
//                (new PlayThread(cursorSong, position)).start();
                db.execSQL("delete from " + Variate.localSearchSongListTable);
                for (int i = 0; i < cursorSong.getCount(); i++) {
                    cursorSong.moveToPosition(i);
                    addSong(db, Variate.localSearchSongListTable, cursorSong);
                    Log.e(TAG, cursorSong.getString(cursorSong.getColumnIndex(Variate.keySongName)));
                }
                cursorSong.moveToPosition(position);
                startPlay(mContext, editorPlayList, Variate.localSearchSongListTable, position);
                setPlayMessage(editorPlayList, cursorSong, Variate.localSearchSongListTable, position);
                songListAdapter.changeData();
                songListAdapter.notifyDataSetChanged();
                threadKey = editKey;
            }
        });
    }

    private void setOnClickListener() {
        linearLayoutParent.setOnClickListener(this);
        imgTitleBack.setOnClickListener(this);
        imgClear.setOnClickListener(this);
        playBarLayout.setOnPlaySongChangeListener(this);
    }

    private void init() {
        mContext = LocalSearchActivity.this;
        dataBase = new DataBase(mContext, Variate.dataBaseName, null, 1);
        db = dataBase.getWritableDatabase();
        preferencesPlayList = getSharedPreferences(Variate.playList, MODE_PRIVATE);
        editorPlayList = preferencesPlayList.edit();
        Intent intent = getIntent();
        fromTable = intent.getStringExtra("tableName");
        Log.e(TAG, fromTable);
        linearLayoutParent = findViewById(R.id.linear_layout_parent);
        tvNoResultHint = findViewById(R.id.tv_no_result_hint);
        edtSearch = findViewById(R.id.edt_title_search);
        imgTitleBack = findViewById(R.id.img_title_back);
        imgClear = findViewById(R.id.img_title_clear);
        lvLocalSearch = findViewById(R.id.lv_local_search);
        playBarLayout = findViewById(R.id.play_bar_layout);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.linear_layout_parent:
//                edtSearch.setFocusable(false);
//                InputMethodManager imm1 = (InputMethodManager)
//                        getSystemService(Context.INPUT_METHOD_SERVICE);
//                imm1.hideSoftInputFromWindow(v.getWindowToken(), 0);
                Log.e(TAG, "linear_layout_parent");
                break;
            case R.id.img_title_back:
                LocalSearchActivity.this.finish();
                break;
            case R.id.img_title_clear:
                edtSearch.setText("");
                break;

        }
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {
        if (edtSearch.getText().toString().isEmpty()) {
            imgClear.setVisibility(View.GONE);
            tvNoResultHint.setVisibility(View.GONE);
            if (songListAdapter != null) {
                songListAdapter.clear();
                songListAdapter.notifyDataSetChanged();
            }
        } else {
            imgClear.setVisibility(View.VISIBLE);
            searchText = edtSearch.getText().toString();
            cursorSong = searchLocalSong(db, fromTable, searchText);
            if (cursorSong.getCount() == 0) {
                tvNoResultHint.setVisibility(View.VISIBLE);
                songListAdapter.clear();
                songListAdapter.notifyDataSetChanged();
            } else {
                tvNoResultHint.setVisibility(View.GONE);
                Log.e(TAG, "*搜索结果数:" + cursorSong.getCount());
                songListAdapter = new SongListAdapter(mContext, cursorSong, Variate.localSearchSongListTable);
                songListAdapter.setOnItemMenuClickListener(this);
                lvLocalSearch.setAdapter(songListAdapter);
                editKey++;
            }
        }
    }

    @Override
    public void OnPlaySongChange() {
        songListAdapter.changeData();
        songListAdapter.notifyDataSetChanged();
    }

    //把音乐列表存入数据库然后播放
    class PlayThread extends Thread {
        Cursor cursor;
        int position;

        PlayThread(Cursor cursor, int position) {
            this.cursor = cursor;
            this.position = position;
        }

        public void run() {
            db.execSQL("delete from " + Variate.localSearchSongListTable);
            for (int i = 0; i < cursor.getCount(); i++) {
                cursor.moveToPosition(i);
                addSong(db, Variate.localSearchSongListTable, cursor);
                Log.e(TAG, cursor.getString(cursor.getColumnIndex(Variate.keySongName)));
            }
            runOnUiThread(() -> {
                setPlayMessage(editorPlayList, cursorSong, Variate.localSearchSongListTable, position);
                startPlay(mContext, editorPlayList, Variate.localSearchSongListTable, position);
                songListAdapter.changeData();
                songListAdapter.notifyDataSetChanged();
            });
            threadKey = editKey;
        }
    }

    //把音乐列表存入数据库
    class SaveThread extends Thread {
        Cursor cursor;

        SaveThread(Cursor cursor) {
            this.cursor = cursor;
        }

        public void run() {
            db.execSQL("delete from " + Variate.localSearchSongListTable);
            for (int i = 0; i < cursor.getCount(); i++) {
                cursor.moveToPosition(i);
                addSong(db, Variate.localSearchSongListTable, cursor);
                Log.e("****", cursor.getString(cursor.getColumnIndex(Variate.keySongName)));
            }
            threadKey = editKey;
        }
    }


    //音乐列表菜单点击事件
    @SuppressLint("RestrictedApi")
    public void onSongListItemMenuClick(View view, int position) {
        if (threadKey != editKey) {
//            (new SaveThread(cursorSong)).run();
            db.execSQL("delete from " + Variate.localSearchSongListTable);
            for (int i = 0; i < cursorSong.getCount(); i++) {
                cursorSong.moveToPosition(i);
                addSong(db, Variate.localSearchSongListTable, cursorSong);
                Log.e("****", cursorSong.getString(cursorSong.getColumnIndex(Variate.keySongName)));
            }
            threadKey = editKey;
        }
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
                db.delete(Variate.localSearchSongListTable, Variate.keySongUrl
                        + " = ?", new String[]{cursorSong.getString(
                        cursorSong.getColumnIndex(Variate.keySongUrl))});
                deleteSong(mContext, db, fromTable, cursorSong, songListAdapter);
                cursorSong = db.rawQuery("select * from " + Variate.localSearchSongListTable
                        + " order by " + Variate.keySongName + " collate localized asc", null);
                break;
        }
    }

    //本地音乐搜索
    public static Cursor searchLocalSong(SQLiteDatabase db, String table, String searchText) {
        Cursor cursor = db.rawQuery(
                "select * from "
                        + table
                        + " where "
                        + Variate.keySongName + " like ?"
                        + " or "
                        + Variate.keySongUrl + " like ?"
                        + " order by " + Variate.keySongName + " collate localized asc"
                , new String[]{"%" + searchText + "%", "%" + searchText + "%"});
        cursor.moveToFirst();
        return cursor;
    }

    @Override
    protected void onStart() {
        super.onStart();
        playBarLayout.mBindService(mContext);
    }

    @Override
    protected void onPause() {
        super.onPause();
        playBarLayout.mUnBindService(mContext);
    }

    protected void onDestroy() {
        super.onDestroy();
        dataBase.close();
        db.close();
        if (cursorSong != null) {
            cursorSong.close();
        }
        Log.e(TAG, "关闭Activity");
    }
}
