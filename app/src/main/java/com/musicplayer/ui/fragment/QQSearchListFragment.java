package com.musicplayer.ui.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.musicplayer.R;
import com.musicplayer.adapter.QQMusicListAdapter;
import com.musicplayer.ui.widget.LoadMoreListView;
import com.musicplayer.utils.Song;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


public class QQSearchListFragment extends Fragment implements QQMusicListAdapter.OnSongListItemMenuClickListener, AdapterView.OnItemClickListener, LoadMoreListView.OnLoadMoreListener {

    private String TAG = "*QQSearchListFragment";
    private Context mContext = getActivity();
    private View view;
    private TextView tvNoResultHint;
    private LoadMoreListView listView;
    private LinearLayout linearLayoutLoading;
    private List<Song> songList = new ArrayList<>();
    private String keyWord = "";
    private QQMusicListAdapter qqMusicListAdapter;
    private int page = 1;
    private boolean isOver = false;

    public static QQSearchListFragment newInstance(String keyWord) {
        QQSearchListFragment fragment = new QQSearchListFragment();
        Bundle args = new Bundle();
        args.putString("keyWord", keyWord);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            Log.e(TAG, "keyWord1:" + keyWord);
            keyWord = getArguments().getString("keyWord");
            Log.e(TAG, "keyWord2:" + keyWord);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_qqsearch_list, container, false);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        tvNoResultHint = view.findViewById(R.id.tv_no_result_hint);
        linearLayoutLoading = view.findViewById(R.id.ll_loading);
        tvNoResultHint.setText(keyWord);
        listView = view.findViewById(R.id.lmlv);
        listView.setOnItemClickListener(this);
        listView.setOnLoadMoreListener(this);
        Log.e(TAG, "list.size:" + songList.size());
        if (songList.size() != 0) {
            qqMusicListAdapter = new QQMusicListAdapter(getActivity(), songList);
            qqMusicListAdapter.setOnItemMenuClickListener(this);
            listView.setAdapter(qqMusicListAdapter);
        } else if (!keyWord.equals("")) {
            linearLayoutLoading.setVisibility(View.VISIBLE);
            synchronized (this) {
                searchFromQQ(keyWord, page);
            }
        }
    }

    private void searchFromQQ(String keyWord, int page) {
        new Thread(() -> {
            try {
                OkHttpClient client = new OkHttpClient.Builder().build();
                Request request = new Request.Builder()
                        .url("https://c.y.qq.com/soso/fcgi-bin/client_search_cp?p=" + page + "&n=30&w=" + keyWord)
                        .build();
                Response response = client.newCall(request).execute();
                if (response.isSuccessful()) {
                    resolveJson(response.body().string());
                } else {
                    Toast.makeText(getActivity(), "连接失败", Toast.LENGTH_SHORT).show();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void resolveJson(final String response) {
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                try {
                    String json = response.substring(response.indexOf('(') + 1, response.lastIndexOf(')'));
                    JSONObject jsonObject = new JSONObject(json).getJSONObject("data").getJSONObject("song");
                    JSONArray jsonArray = jsonObject.getJSONArray("list");
                    //是否下一页
                    if (jsonArray.length() < 30) {
                        isOver = true;
                    } else {
                        page++;
                    }
                    StringBuilder builderSinger;
                    String songName, songMid;
                    String[] singers;
                    JSONArray array;
                    for (int i = 0; i < jsonArray.length(); i++) {
                        jsonObject = jsonArray.getJSONObject(i);
                        songName = jsonObject.getString("songname");
                        songMid = jsonObject.getString("songmid");
                        array = jsonObject.getJSONArray("singer");
                        singers = new String[array.length()];
                        builderSinger = new StringBuilder();
                        for (int j = 0; j < array.length(); j++) {
                            singers[j] = array.getJSONObject(j).getString("name");
                        }
                        for (String s : singers) {
                            builderSinger.append(s + "/");
                        }
                        builderSinger.deleteCharAt(builderSinger.lastIndexOf("/"));
//                    Log.e(TAG, "songName:" + songName + " /singer:" + builderSinger.toString() + " /songMid:" + songMid);
                        Song song = new Song();
                        song.setSongName(songName);
                        song.setSinger(builderSinger.toString());
                        song.setQQSongMid(songMid);
                        songList.add(song);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                if (songList.size() == 0) {
                    tvNoResultHint.setVisibility(View.VISIBLE);
                    linearLayoutLoading.setVisibility(View.GONE);
                    listView.setVisibility(View.GONE);
                } else {
                    linearLayoutLoading.setVisibility(View.GONE);
                    tvNoResultHint.setVisibility(View.GONE);
                    listView.setVisibility(View.VISIBLE);
                    if (qqMusicListAdapter == null) {
                        qqMusicListAdapter = new QQMusicListAdapter(getActivity(), songList);
                        qqMusicListAdapter.setOnItemMenuClickListener(this);
                        listView.setAdapter(qqMusicListAdapter);
                    } else {
                        qqMusicListAdapter.notifyDataSetChanged();
                        listView.setLoadCompleted();
                    }
                }
            });
        }
    }


    //音乐列表item点击事件
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Toast.makeText(getActivity(), songList.get(position).getSongName(), Toast.LENGTH_SHORT).show();
    }

    //音乐列表item imgMenu点击事件
    @Override
    public void onSongListItemMenuClick(int position) {
        Toast.makeText(getActivity(), "" + position, Toast.LENGTH_SHORT).show();
    }

    //丄滑加载数据
    @Override
    public void onloadMore() {
        if (!isOver) {
            Log.e(TAG, "onloadMore()");
            synchronized (this) {
                Log.e(TAG, "synchronized()");
                searchFromQQ(keyWord, page);
            }
        } else {
            Toast.makeText(getActivity(), "已加载所有搜索结果", Toast.LENGTH_SHORT).show();
            listView.setLoadCompleted();
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();

//        songList.clear();
    }

}
