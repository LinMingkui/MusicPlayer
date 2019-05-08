package com.musicplayer.ui.fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.view.menu.MenuPopupHelper;
import android.support.v7.widget.PopupMenu;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.musicplayer.R;
import com.musicplayer.adapter.NetworkSongListAdapter;
import com.musicplayer.database.DataBase;
import com.musicplayer.service.PlayService;
import com.musicplayer.ui.widget.LoadMoreListView;
import com.musicplayer.utils.DownloadUtils;
import com.musicplayer.utils.Song;
import com.musicplayer.utils.ToastUtils;
import com.musicplayer.utils.UnicodeUtils;
import com.musicplayer.utils.Variate;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static com.musicplayer.utils.MethodUtils.addSongMenu;
import static com.musicplayer.utils.MethodUtils.addToFavorite;
import static com.musicplayer.utils.Song.songToCursor;

public class WYYSearchListFragment extends Fragment implements AdapterView.OnItemClickListener, LoadMoreListView.OnLoadMoreListener, NetworkSongListAdapter.OnSongListItemMenuClickListener {

    private String TAG = "*WYYSearchListFragment";
    private DataBase dataBase;
    private SQLiteDatabase db;
    private View view;
    private TextView tvNoResultHint;
    private LoadMoreListView listView;
    private LinearLayout linearLayoutLoading;
    private List<Song> songList = new ArrayList<>();
    private String keyWord = "";
    private NetworkSongListAdapter songListAdapter;
    private int page = 1;
    private int position;
    private boolean isOver = false;

    public static WYYSearchListFragment newInstance(String keyWord) {
        WYYSearchListFragment fragment = new WYYSearchListFragment();
        Bundle args = new Bundle();
        args.putString("keyWord", keyWord);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            keyWord = getArguments().getString("keyWord");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_wyysearch_list, container, false);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        tvNoResultHint = view.findViewById(R.id.tv_no_result_hint);
        linearLayoutLoading = view.findViewById(R.id.ll_loading);
        listView = view.findViewById(R.id.lmlv);
        listView.setOnItemClickListener(this);
        listView.setOnLoadMoreListener(this);


        if (songList.size() != 0) {
            songListAdapter = new NetworkSongListAdapter(getActivity(), songList, Variate.SONG_TYPE_WYY);
            songListAdapter.setOnItemMenuClickListener(this);
            listView.setAdapter(songListAdapter);
        } else if (!keyWord.equals("")) {
            linearLayoutLoading.setVisibility(View.VISIBLE);
            synchronized (this) {
                searchFromWYY(keyWord, page);
            }
        }
    }

    private void searchFromWYY(String keyWord, int page) {
        new Thread(() -> {
//            String QQMid = "002ReDzj13wRz0";
            OkHttpClient client = new OkHttpClient.Builder()
                    .connectTimeout(10, TimeUnit.SECONDS).build();
            FormBody formBody = new FormBody.Builder()
                    .add("input", keyWord)
                    .add("filter", "name")
                    .add("type", "netease")
                    .add("page", String.valueOf(page))
                    .build();
            Request request = new Request.Builder()
                    .url("http://music.wandhi.com/")
                    .addHeader("X-Requested-With", "XMLHttpRequest")
                    .post(formBody)
                    .build();
            try {
                Response response = client.newCall(request).execute();
                if (response.isSuccessful()) {
                    resolveSearchJson(response.body().string());
                } else {
//                    Toast.makeText(getActivity(), "连接失败", Toast.LENGTH_SHORT).show();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void resolveSearchJson(String json) {
//        Log.e(TAG, json);
        json = UnicodeUtils.unicodeToString(json);
//                    jsonSongInfo = jsonSongInfo.replace("\\n","");
//                    jsonSongInfo = jsonSongInfo.replace("\\","");
//                    Log.e("NetworkUtils.getSong",jsonSongInfo);
        try {
            JSONArray array = null;
            JSONObject object = null;
            Song song = null;
            array = new JSONObject(json).getJSONArray("data");
            if (array.length() < 10) {
                isOver = true;
            } else {
                page++;
            }
            for (int i = 0; i < array.length(); i++) {
                object = array.getJSONObject(i);
                if (!object.getString("url").replace("\\", "").equals("null")) {
                    song = new Song();
                    song.setSongName(object.getString("title"));
                    song.setSinger(object.getString("author").equals("")
                            ? "群星" : object.getString("author"));
                    song.setSongUrl(object.getString("url").replace("\\", ""));
                    song.setSingerUrl(object.getString("pic").replace("\\", ""));
                    song.setLrc(object.getString("lrc"));
                    song.setType(Variate.SONG_TYPE_WYY);
                    song.setSongMid(object.getString("songid"));
                    songList.add(song);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                if (songList.size() == 0) {
                    tvNoResultHint.setVisibility(View.VISIBLE);
                    linearLayoutLoading.setVisibility(View.GONE);
                    listView.setVisibility(View.GONE);
                } else {
                    linearLayoutLoading.setVisibility(View.GONE);
                    tvNoResultHint.setVisibility(View.GONE);
                    listView.setVisibility(View.VISIBLE);
                    if (songListAdapter == null) {
                        songListAdapter = new NetworkSongListAdapter(getActivity(), songList, Variate.SONG_TYPE_WYY);
                        songListAdapter.setOnItemMenuClickListener(this);
                        listView.setAdapter(songListAdapter);
                    } else {
                        songListAdapter.notifyDataSetChanged();
                        listView.setLoadCompleted();
                    }
                }
            });
        }
    }

    //音乐列表item点击事件
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Variate.song = songList.get(position);
        Log.e(TAG, songList.get(position).getSongUrl());
        if (getActivity() != null) {
            Intent intent = new Intent(getActivity(), PlayService.class);
            intent.putExtra(Variate.keyIsLocal, false);
            getActivity().startService(intent);
        }
    }

    //丄滑加载数据
    @Override
    public void onLoadMore() {
        if (!isOver) {
            Log.e(TAG, "onLoadMore()");
            synchronized (this) {
                Log.e(TAG, "synchronized()");
                searchFromWYY(keyWord, page);
            }
        } else {
            ToastUtils.show(getActivity(), "已加载所有搜索结果");
            listView.setLoadCompleted();
        }
    }

    //音乐列表item imgMenu点击事件
    @SuppressLint("RestrictedApi")
    @Override
    public void onSongListItemMenuClick(View view, int position) {
        this.position = position;
        PopupMenu pm = new PopupMenu(getActivity(), view.findViewById(R.id.img_song_list_menu));
        pm.getMenuInflater().inflate(R.menu.memu_pm_local_song_list, pm.getMenu());
        pm.getMenu().getItem(2).setVisible(false);
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
                addToFavorite(getActivity(), db, songList.get(position));
                break;
            //添加到歌单
            case R.id.item_add_song_menu:
                View viewAddSongMenuDialog = getLayoutInflater().inflate(R.layout.dialog_add_song_menu, null);
                addSongMenu(getActivity(), db, songToCursor(songList.get(position)), viewAddSongMenuDialog);
                break;
            case R.id.item_download:
                DownloadUtils downloadUtils = new DownloadUtils(getActivity(), null, songList.get(position));
                downloadUtils.startDownload();
                break;
        }
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Log.e(TAG, "onAttach");
        dataBase = new DataBase(getActivity(), Variate.dataBaseName, null, 1);
        db = dataBase.getWritableDatabase();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        Log.e(TAG, "onDetach");
        if (dataBase != null) {
            dataBase.close();
        }
        if (db != null) {
            db.close();
        }
    }
}
