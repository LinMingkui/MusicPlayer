package com.musicplayer.ui.fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.lauzy.freedom.library.Lrc;
import com.musicplayer.R;
import com.musicplayer.database.DataBase;
import com.musicplayer.ui.widget.LrcView;
import com.musicplayer.utils.LrcHelper;
import com.musicplayer.utils.NetworkUtils;
import com.musicplayer.utils.Variate;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static com.musicplayer.utils.LrcHelper.parseLrcFromString;

public class LyricFragment extends Fragment {

    private Context context;
    private String TAG = "*LyricFragment";
    private SharedPreferences preferencesPlayList;
    private SharedPreferences.Editor editorPlayList;
    private NetworkUtils networkUtils;
    private View view;
    private LrcView lrcView;
    private FrameLayout frameLayoutLyric;
    private List<Lrc> lrc = new ArrayList<>();
    private boolean run = true;
    private boolean updateLrc = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.e(TAG, "onCreate");
        preferencesPlayList = getActivity().getSharedPreferences(Variate.playList, Context.MODE_PRIVATE);
        editorPlayList = preferencesPlayList.edit();
        editorPlayList.apply();
        networkUtils = new NetworkUtils();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_lyric, container, false);
        Log.e(TAG, "onCreateView");
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        context = getContext();
        run = true;
        lrcView = view.findViewById(R.id.lrc_view);
        frameLayoutLyric = view.findViewById(R.id.fl_lyric);
        lrcView.setEmptyContent("暂无歌词");
        initLyric();
        networkUtils.setOnGetSongInfoListener(song -> {
            lrc = parseLrcFromString(song.getLrc());
            if (getActivity() != null) {
                (getActivity()).runOnUiThread(() -> lrcView.setLrcData(lrc));
            }
            updateLrc = true;
        });
        Log.e(TAG, "lrc.size() " + lrc.size());
        lrcView.setOnPlayIndicatorLineListener((var1, var3) -> {
            Log.e(TAG, "歌词跳转播放");
            Variate.setPlayProgress = (int) (var1);
            Variate.isSetProgress = true;
            lrcView.updateTime(var1);
        });
        ChangeUI changeUI = new ChangeUI();
        changeUI.start();
    }

    private class ChangeUI extends Thread {
        @Override
        public void run() {
            while (run) {
                if (Variate.isInitLyric) {
                    handler.sendEmptyMessage(2);
                    updateLrc = false;
                } else if (updateLrc && !Variate.isSetProgress) {
                    handler.sendEmptyMessage(1);
                }
                try {
                    sleep(200);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    lrcView.updateTime(Variate.playProgress);
                    break;
                case 2:
                    Log.e(TAG, "初始化歌词");
                    initLyric();
                    break;
            }
            return false;
        }
    });

    private void initLyric() {
        Variate.isInitLyric = false;
        String path, lrcPath;
        if (preferencesPlayList.getInt(Variate.keySongType, Variate.SONG_TYPE_LOCAL) == Variate.SONG_TYPE_LOCAL) {
            path = preferencesPlayList.getString(Variate.keySongUrl, "");
            Log.e(TAG, "songPath " + path);
            lrcPath = new StringBuilder(Variate.LRC_PATH)
                    .append('/').append(path.substring(path.lastIndexOf('/'), path.lastIndexOf('.')))
                    .append(".lrc").toString();
            Log.e(TAG, "lrcPath " + lrcPath);
        } else {
            String songName = preferencesPlayList.getString(Variate.keySongName, "");
            String singer = preferencesPlayList.getString(Variate.keySinger, "");
            singer = singer.replace('/', ' ');
            lrcPath = new StringBuilder(Variate.LRC_PATH).append('/').append(singer).append(" - ")
                    .append(songName).append(".lrc").toString();
        }
        File fileLrc = new File(lrcPath);
        if (fileLrc.exists()) {
            lrc = LrcHelper.parseLrcFromFile(fileLrc);
            lrcView.setLrcData(lrc);
            updateLrc = true;
        } else {
            String keyWord = new StringBuilder(preferencesPlayList.getString(Variate.keySinger, "")
                    .replace('/', ' ')).append(" ")
                    .append(preferencesPlayList.getString(Variate.keySongName, "")).toString();
            if (!keyWord.equals("")) {
                networkUtils.getSongInfo(keyWord, "qq", Variate.FILTER_NAME);
            }
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        run = false;
    }

}
