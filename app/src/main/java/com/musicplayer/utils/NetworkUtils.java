package com.musicplayer.utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class NetworkUtils {
    private OnGetSongInfoListener onGetSongInfoListener;

    public void getSongInfo(String keyWord, String type, String filter) {
        new Thread(() -> {
//            String QQMid = "002ReDzj13wRz0";
            OkHttpClient client = new OkHttpClient.Builder()
                    .connectTimeout(10, TimeUnit.SECONDS).build();
            FormBody formBody = new FormBody.Builder()
                    .add("input", keyWord)
                    .add("filter", filter)
                    .add("type", type)
                    .add("page", "1")
                    .build();
            Request request = new Request.Builder()
                    .url("http://music.wandhi.com/")
                    .addHeader("X-Requested-With", "XMLHttpRequest")
                    .post(formBody)
                    .build();
            try {
                Response response = client.newCall(request).execute();
                if (response.isSuccessful()) {
//                    Log.e(TAG, " " + response.body().string());
                    String jsonSongInfo = UnicodeUtils.unicodeToString(response.body().string());
//                    jsonSongInfo = jsonSongInfo.replace("\\n","");
//                    jsonSongInfo = jsonSongInfo.replace("\\","");
//                    Log.e("NetworkUtils.getSong",jsonSongInfo);
                    JSONArray array = new JSONObject(jsonSongInfo).getJSONArray("data");
                    if (array.length() != 0) {
                        JSONObject object = array.getJSONObject(0);
                        Song song = new Song();
                        song.setSongName(object.getString("title"));
                        song.setSinger(object.getString("author").equals("")
                                ? "群星" : object.getString("author"));
                        song.setSongUrl(object.getString("url").replace("\\", ""));
                        song.setSingerUrl(object.getString("pic").replace("\\", ""));
                        song.setLrc(object.getString("lrc"));
                        song.setType(Song.getTypeByString(type));
                        song.setSongMid(object.getString("songid"));
                        if (onGetSongInfoListener != null) {
                            onGetSongInfoListener.onGetSongInfo(song);
                        }
                    } else {
                        if (onGetSongInfoListener != null) {
                            onGetSongInfoListener.onGetSongInfo(null);
                        }
                    }
                } else {
//                    Toast.makeText(getActivity(), "连接失败", Toast.LENGTH_SHORT).show();
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }).start();
    }

    public interface OnGetSongInfoListener {
        void onGetSongInfo(Song song);
    }

    public void setOnGetSongInfoListener(OnGetSongInfoListener listener) {
        onGetSongInfoListener = listener;
    }
}
