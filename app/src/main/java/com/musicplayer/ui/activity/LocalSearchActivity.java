package com.musicplayer.ui.activity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
import static com.musicplayer.utils.MethodUtils.insertSong;
import static com.musicplayer.utils.MethodUtils.searchLocalSong;

public class LocalSearchActivity extends BaseActivity implements View.OnClickListener, TextWatcher, SongListAdapter.OnSongListItemMenuClickListener {

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_local_search);

        init();
        setOnClickListener();
        songListAdapter = new SongListAdapter(mContext,
                cursorSong, StaticVariate.localSearchSongListTable);
        songListAdapter.setOnItemMenuClickListener(this);

        edtSearch.addTextChangedListener(this);
        lvLocalSearch.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (editKey == threadKey) {
                    startPlay(mContext, editorPlayList,
                            StaticVariate.localSearchSongListTable, position);
                    songListAdapter.changeData();
                    songListAdapter.notifyDataSetChanged();
                } else {
                    (new PlayThread(cursorSong, position)).run();
                    songListAdapter.changeData();
                    songListAdapter.notifyDataSetChanged();
                }
            }
        });
    }

    private void setOnClickListener() {
        linearLayoutParent.setOnClickListener(this);
        imgTitleBack.setOnClickListener(this);
        imgClear.setOnClickListener(this);
    }

    private void init() {
        mContext = LocalSearchActivity.this;
        dataBase = new DataBase(mContext, StaticVariate.dataBaseName, null, 1);
        db = dataBase.getWritableDatabase();
        preferencesPlayList = getSharedPreferences(StaticVariate.playList, MODE_PRIVATE);
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
                songListAdapter = new SongListAdapter(mContext,
                        cursorSong, StaticVariate.localSearchSongListTable);
                songListAdapter.setOnItemMenuClickListener(this);
//                songListAdapter.setPlayPosition();
//                songListAdapter.notifyDataSetChanged();
                lvLocalSearch.setAdapter(songListAdapter);
                editKey++;
            }
        }
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
            db.execSQL("delete from " + StaticVariate.localSearchSongListTable);
            for (int i = 0; i < cursor.getCount(); i++) {
                cursor.moveToPosition(i);
                insertSong(db, StaticVariate.localSearchSongListTable, cursor, false);
                Log.e(TAG, cursor.getString(cursor.getColumnIndex(StaticVariate.title)));
            }
            startPlay(mContext, editorPlayList,
                    StaticVariate.localSearchSongListTable, position);
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
            db.execSQL("delete from " + StaticVariate.localSearchSongListTable);
            for (int i = 0; i < cursor.getCount(); i++) {
                cursor.moveToPosition(i);
                insertSong(db, StaticVariate.localSearchSongListTable, cursor, false);
                Log.e("****", cursor.getString(cursor.getColumnIndex(StaticVariate.title)));
            }
            threadKey = editKey;
        }
    }

    public void onSongListItemMenuClick(int position) {
        songListItemPosition = position;
        if (threadKey != editKey) {
            (new SaveThread(cursorSong)).run();
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setItems(new String[]{"添加或移除收藏", "添加到歌单", "从列表删除"}, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Log.e(TAG, "" + which);
                songListItemMenuItemClick(which);
            }
        }).show();
    }

    private void songListItemMenuItemClick(int dialogItemPosition) {
        cursorSong.moveToPosition(songListItemPosition);
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
                deleteSong(mContext, db, fromTable,
                        cursorSong, preferencesPlayList, songListItemPosition, songListAdapter);
                cursorSong = db.rawQuery("select * from " + StaticVariate.localSongListTable, null);
                break;
        }
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
