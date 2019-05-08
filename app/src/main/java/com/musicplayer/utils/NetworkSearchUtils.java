package com.musicplayer.utils;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class NetworkSearchUtils {
    private String TAG = "*NetworkSearchUtils";

    private Song[] searchFromQQ(String keyWord, int page) {
        final Song[] songs = new Song[30];
        new Thread(() -> {
            try {
                OkHttpClient client = new OkHttpClient.Builder().build();
                Request request = new Request.Builder()
                        .url("https://c.y.qq.com/soso/fcgi-bin/client_search_cp?p=" + page + "&n=30&w=" + keyWord)
                        .build();
                Response response = client.newCall(request).execute();
                //解析Json
                if (response.isSuccessful()) {
//                    resolveJson(response.body().string());
                    String json = response.body().string();
                    json = json.substring(json.indexOf('(') + 1, json.lastIndexOf(')'));
                    JSONObject jsonObject = new JSONObject(json).getJSONObject("data").getJSONObject("song");
                    JSONArray jsonArray = jsonObject.getJSONArray("list");
                    StringBuilder builderSinger;
                    String songName, songMid;
                    String[] singers;
                    JSONArray array;
//                    songs = new Song[jsonArray.length()];
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
                        Log.e(TAG, "songName:" + songName + " /singer:" + builderSinger.toString() + " /songMid:" + songMid);
                        songs[i].setSongName(songName);
                        songs[i].setSinger(builderSinger.toString());
                        songs[i].setSongMid(songMid);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }).start();
        return songs;
    }

//    private Song[] resolveJson(final String response) {
//        try {
//
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
//        return null;
//    }
}
