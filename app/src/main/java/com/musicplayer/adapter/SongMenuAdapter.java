package com.musicplayer.adapter;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.musicplayer.R;
import com.musicplayer.utils.NetworkUtils;
import com.musicplayer.utils.Variate;

import java.io.File;

import static com.musicplayer.utils.MethodUtils.savePic;


public class SongMenuAdapter extends BaseAdapter {

    private LayoutInflater mLayoutInflater;
    private Context context;
    private Cursor cursor;

    public SongMenuAdapter(Context context, Cursor cursor) {
        mLayoutInflater = LayoutInflater.from(context);
        this.context = context;
        this.cursor = cursor;
        this.cursor.moveToFirst();
    }

    public int getCount() {
        return cursor.getCount();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }


    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = mLayoutInflater.inflate(R.layout.item_song_menu, null);
            holder = new ViewHolder();
            holder.textViewSongMenuName = convertView.findViewById(R.id.text_song_menu_name);
            holder.textViewSongNumber = convertView.findViewById(R.id.text_song_number);
            holder.imgSinger = convertView.findViewById(R.id.img_singer);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        if (cursor.getCount() != 0) {
            cursor.moveToPosition(position);
            holder.textViewSongMenuName.setText(cursor.getString(
                    cursor.getColumnIndex(Variate.keySongMenuName)));
            int number = cursor.getInt(cursor.getColumnIndex(Variate.keySongNumber));
            holder.textViewSongNumber.setText(number + "首");
            String singer = cursor.getString(cursor.getColumnIndex(Variate.keySinger));
            if(singer.equals("默认")){
                holder.imgSinger.setImageResource(R.mipmap.img_default_singer);
            }else {
                singer = singer.replace('/', ' ');

                File file = new File(Variate.PIC_PATH, singer);
                if (file.exists()) {
                    Glide.with(context).load(file).into(holder.imgSinger);
                } else {
                    NetworkUtils networkUtils = new NetworkUtils();
                    networkUtils.getSongInfo(cursor.getString(cursor.getColumnIndex(Variate.keySinger)),
                            "qq", Variate.FILTER_NAME);
                    networkUtils.setOnGetSongInfoListener(song -> {
                        savePic(context, song.getSingerUrl(), file);
                        ((Activity) context).runOnUiThread(() -> {
                            Glide.with(context).load(file)
                                    .error(R.mipmap.img_default_singer).into(holder.imgSinger);
                        });

                    });
                }
            }
        }
        return convertView;
    }

    class ViewHolder {
        ImageView imgSinger;
        TextView textViewSongMenuName;
        TextView textViewSongNumber;
    }
}
