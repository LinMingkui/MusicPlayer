package com.musicplayer.ui.fragment;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.musicplayer.R;
import com.musicplayer.adapter.SongMenuAdapter;
import com.musicplayer.database.DataBase;
import com.musicplayer.ui.activity.DownloadSongActivity;
import com.musicplayer.ui.activity.FavoriteSongActivity;
import com.musicplayer.ui.activity.LocalSongActivity;
import com.musicplayer.ui.activity.RecentlyPlayActivity;
import com.musicplayer.ui.activity.SongMenuActivity;
import com.musicplayer.ui.widget.NoScrollListView;
import com.musicplayer.utils.Variate;

import static android.content.Context.MODE_PRIVATE;


public class MyFragment extends Fragment implements View.OnClickListener {

    private Context mContext;
    private View view;
    private LinearLayout linearLayoutLocalMusic, linearLayoutDownloadSong,
            linearLayoutFavoriteSong, linearLayoutRecentlyPlay;
    private RelativeLayout relativeLayoutSongMenu;
    private LinearLayout linearLayoutAddSongMenu;
    private LinearLayout linearLayoutSet, linearLayoutExit;

    private DataBase dataBase;
    private SQLiteDatabase db;
    private SharedPreferences preferencesSet;
    private SharedPreferences.Editor editorSet;

    private SongMenuAdapter songMenuAdapter;
    private Cursor cursorSongMenuList;

    private TextView textLocalSongNumber, textFavoriteSongNumber, textRecentlySongNumber;
    private ImageView imgExpand;
    private NoScrollListView listSongMenu;

    public static MyFragment newInstance() {
        MyFragment fragment = new MyFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_my, container, false);
        return view;
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        init();
        setClickListener();
        setSongNumber();

        if (!preferencesSet.getBoolean(Variate.keySongMenuExpand, true)) {
            listSongMenu.setVisibility(View.GONE);
            imgExpand.setImageResource(R.drawable.ic_expand_more);
        }

        songMenuAdapter = new SongMenuAdapter(mContext, cursorSongMenuList);
        listSongMenu.setAdapter(songMenuAdapter);
        listSongMenu.setOnItemClickListener((parent, view, position, id) -> {
            TextView tvSongMenuName = view.findViewById(R.id.text_song_menu_name);
            Cursor cursorSongMenuId = db.rawQuery("select "+Variate.keySongMenuId+" from "
                            + Variate.songMenuNameTable + " where "
                            + Variate.keySongMenuName + " = ?",
                    new String[]{tvSongMenuName.getText().toString()});
            if (cursorSongMenuId.getCount() == 0) {
                Toast.makeText(mContext, "打开出错", Toast.LENGTH_SHORT).show();
            } else {
                cursorSongMenuId.moveToFirst();
                Intent intent = new Intent(mContext, SongMenuActivity.class);
                intent.putExtra(Variate.keySongMenuId, cursorSongMenuId.getInt(0));
                intent.putExtra(Variate.keySongMenuName, tvSongMenuName.getText());
                startActivityForResult(intent, 5);
            }
        });

        //删除歌单
        listSongMenu.setOnItemLongClickListener((parent, view, position, id) -> {
            deleteSongMenuDialog(view);
            return true;
        });
    }

    // TODO: Rename method, update argument and hook method into UI event
//    public void onButtonPressed(Uri uri) {
//        if (mListener != null) {
//            mListener.onFragmentInteraction(uri);
//        }
//    }

//    @Override
//    public void onAttach(Context context) {
//        super.onAttach(context);
//        if (context instanceof OnFragmentInteractionListener) {
//            mListener = (OnFragmentInteractionListener) context;
//        } else {
//            throw new RuntimeException(context.toString()
//                    + " must implement OnFragmentInteractionListener");
//        }
//    }

//    @Override
//    public void onDetach() {
//        super.onDetach();
//        mListener = null;
//    }


    public void onClick(View v) {
        switch (v.getId()) {
//            case R.id.img_menu:
            case R.id.linear_layout_local_music:
                Intent localMusicIntent = new Intent(mContext, LocalSongActivity.class);
                startActivityForResult(localMusicIntent, 1);
                break;
            case R.id.linear_layout_download:
                Intent downloadSongIntent = new Intent(mContext, DownloadSongActivity.class);
                startActivityForResult(downloadSongIntent, 2);
                break;
            case R.id.linear_layout_my_favorite:
                Intent favoriteSongIntent = new Intent(mContext, FavoriteSongActivity.class);
                startActivityForResult(favoriteSongIntent, 3);
                break;
            case R.id.linear_layout_recently:
                Intent recentlyPlaySongIntent = new Intent(mContext, RecentlyPlayActivity.class);
                startActivityForResult(recentlyPlaySongIntent, 4);
                break;
            //创建歌单
            case R.id.linear_layout_add_song_menu:
                addSongMenuDialog();
//                AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
//                builder.
                break;
            case R.id.img_expand:
            case R.id.rl_song_menu:
                setSongMenuExpand();
                break;
        }
    }

    private void setClickListener() {
        linearLayoutLocalMusic.setOnClickListener(this);
        linearLayoutRecentlyPlay.setOnClickListener(this);
        linearLayoutFavoriteSong.setOnClickListener(this);
        linearLayoutDownloadSong.setOnClickListener(this);
        linearLayoutAddSongMenu.setOnClickListener(this);
        relativeLayoutSongMenu.setOnClickListener(this);
        imgExpand.setOnClickListener(this);
    }

    //初始化
    private void init() {
        mContext = getActivity();
        dataBase = new DataBase(mContext, Variate.dataBaseName,
                null, 1);
        db = dataBase.getWritableDatabase();
        cursorSongMenuList = db.rawQuery("select * from " +
                Variate.songMenuNameTable, null);
        preferencesSet = mContext.getSharedPreferences(Variate.set, MODE_PRIVATE);
        editorSet = preferencesSet.edit();
        linearLayoutLocalMusic = view.findViewById(R.id.linear_layout_local_music);
        linearLayoutDownloadSong = view.findViewById(R.id.linear_layout_download);
        linearLayoutFavoriteSong = view.findViewById(R.id.linear_layout_my_favorite);
        linearLayoutRecentlyPlay = view.findViewById(R.id.linear_layout_recently);
        linearLayoutAddSongMenu = view.findViewById(R.id.linear_layout_add_song_menu);
        relativeLayoutSongMenu = view.findViewById(R.id.rl_song_menu);

        textLocalSongNumber = view.findViewById(R.id.text_local_song_number);
        textFavoriteSongNumber = view.findViewById(R.id.text_favorite_song_number);
        textRecentlySongNumber = view.findViewById(R.id.text_recently_song_number);

        imgExpand = view.findViewById(R.id.img_expand);

        listSongMenu = view.findViewById(R.id.list_song_menu);
    }

    //设置歌单是否展开
    private void setSongMenuExpand() {
        if (preferencesSet.getBoolean(Variate.keySongMenuExpand, true)) {
            listSongMenu.setVisibility(View.GONE);
            imgExpand.setImageResource(R.drawable.ic_expand_more);
            editorSet.putBoolean(Variate.keySongMenuExpand, false);
            editorSet.apply();
        } else {
            listSongMenu.setVisibility(View.VISIBLE);
            imgExpand.setImageResource(R.drawable.ic_expand_less);
            editorSet.putBoolean(Variate.keySongMenuExpand, true);
            editorSet.apply();
        }
    }

    //设置音乐数量
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case 1:
            case 3:
            case 4:
            case 5:
                setSongNumber();
                break;
        }
    }

    //设置音乐数量
    private void setSongNumber() {
        Cursor cursor = db.rawQuery("select "+Variate.keySongId+" from "+Variate.localSongListTable,null);
        textLocalSongNumber.setText(cursor.getCount() + "首");
        cursor = db.rawQuery("select "+Variate.keySongId+" from "+Variate.favoriteSongListTable, null);
        textFavoriteSongNumber.setText(cursor.getCount() + "首");
        cursor = db.rawQuery("select "+Variate.keySongId+" from "+Variate.recentlySongListTable, null);
        textRecentlySongNumber.setText(cursor.getCount() + "首");
        cursorSongMenuList = db.rawQuery("select * from " + Variate.songMenuNameTable, null);
        songMenuAdapter = new SongMenuAdapter(mContext, cursorSongMenuList);
        listSongMenu.setAdapter(songMenuAdapter);
        cursor.close();
    }

    //创建歌单对话框
    private void addSongMenuDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        View viewAddSongMenuDialog = getLayoutInflater().inflate(R.layout.dialog_add_song_menu, null);
        builder.setView(viewAddSongMenuDialog);
        builder.create();
        final AlertDialog dialog = builder.show();
        final EditText editSongMenuName = viewAddSongMenuDialog.
                findViewById(R.id.edit_song_menu_name);
        Button btnOK = viewAddSongMenuDialog.findViewById(R.id.btn_ok);
        Button btnCancel = viewAddSongMenuDialog.findViewById(R.id.btn_cancel);
        btnCancel.setOnClickListener(v -> dialog.dismiss());
        btnOK.setOnClickListener(v -> {
            if (!editSongMenuName.getText().toString().isEmpty()) {
                Cursor cursor = db.rawQuery("select " + Variate.keySongMenuName +
                                " from " + Variate.songMenuNameTable +
                                " where " + Variate.keySongMenuName +
                                " = ?",
                        new String[]{editSongMenuName.getText().toString()});
                if (cursor.getCount() == 0) {
                    ContentValues values = new ContentValues();
                    Log.e("****", editSongMenuName.getText().toString());
                    values.put(Variate.keySongMenuName, editSongMenuName.getText().toString());
                    values.put(Variate.keySongNumber, 0);
                    db.insert(Variate.songMenuNameTable,
                            null, values);
                    dialog.dismiss();
                    cursorSongMenuList = db.rawQuery("select * from " +
                            Variate.songMenuNameTable, null);
                    songMenuAdapter = new SongMenuAdapter(mContext, cursorSongMenuList);
                    listSongMenu.setAdapter(songMenuAdapter);
                    Cursor cursorSongMenuId = db.rawQuery("select " + Variate.keySongMenuId + " from "
                                    + Variate.songMenuNameTable + " where "
                                    + Variate.keySongMenuName + " = ?",
                            new String[]{editSongMenuName.getText().toString()});
                    cursorSongMenuId.moveToFirst();
                    db.execSQL("create table songMenuTable"
                            + "_" + cursorSongMenuId.getInt(0)
                            + "(songId integer primary key AUTOINCREMENT, "
                            + "songName text, "
                            + "singer text, "
                            + "songUrl text,"
                            + "songType interger)");
                    Toast.makeText(mContext, "创建成功", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(mContext, "歌单已存在", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(mContext, "歌单名不能为空", Toast.LENGTH_SHORT).show();
            }
        });

    }

    //删除歌单对话框
    private void deleteSongMenuDialog(View v) {
        final View view = v;
        TextView textViewSongMenuName = view.findViewById(R.id.text_song_menu_name);
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle("删除歌单")
                .setMessage("确定删除“" + textViewSongMenuName.getText() + "”歌单")
                .setPositiveButton("确定", (dialog, which) -> {
                    TextView textViewSongMenuName1 = view.findViewById(R.id.text_song_menu_name);
                    String[] songMenuName = new String[]{textViewSongMenuName1.getText().toString()};
                    Cursor cursorSongMenuId = db.rawQuery("select " + Variate.keySongMenuId + " from " +
                            Variate.songMenuNameTable
                            + " where "
                            + Variate.keySongMenuName
                            + " = ?", songMenuName);
                    if (cursorSongMenuId.getCount() == 0) {
                        Toast.makeText(mContext, "获取歌单id失败", Toast.LENGTH_SHORT).show();
                    } else {
                        cursorSongMenuId.moveToFirst();
                        int id = cursorSongMenuId.getInt(0);
                        //删除表
                        String sql = "drop table " + Variate.songMenuTable + "_" + id;
                        db.execSQL(sql);
                        db.delete(Variate.songMenuNameTable, Variate.keySongMenuName + " =?",
                                songMenuName);
                        cursorSongMenuList = db.rawQuery("select * from " +
                                Variate.songMenuNameTable, null);
                        songMenuAdapter = new SongMenuAdapter(mContext, cursorSongMenuList);
                        listSongMenu.setAdapter(songMenuAdapter);
                        Toast.makeText(mContext, "删除成功", Toast.LENGTH_SHORT).show();
                    }
                    cursorSongMenuId.close();
                })
                .setNegativeButton("取消", null).show();
    }
}
