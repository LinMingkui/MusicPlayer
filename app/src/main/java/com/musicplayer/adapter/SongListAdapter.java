package com.musicplayer.adapter;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.musicplayer.R;
import com.musicplayer.database.DataBase;
import com.musicplayer.utils.Song;
import com.musicplayer.utils.StaticVariate;

import java.util.ArrayList;

import static android.content.Context.MODE_PRIVATE;
import static com.musicplayer.utils.MethodUtils.getSqlBaseOrder;

public class SongListAdapter extends BaseAdapter {

    private DataBase dataBase;
    private SQLiteDatabase db;
    private int songNumber, playPosition;
    private String table;
    private String playTable = "";
    private String fileUrl = "";
    private Context mContext;
    private SharedPreferences preferencesPlayList;
    private SharedPreferences preferencesSet;
    private ArrayList<Song> song = null;
    private Cursor cursor = null;
    private LayoutInflater mLayoutInflater;
    private OnSongListItemMenuClickListener onSongListItemMenuClickListener;

    public SongListAdapter(Context context, ArrayList<Song> song, String table) {
        mLayoutInflater = LayoutInflater.from(context);
        this.songNumber = song.size();
        this.song = song;
        this.table = table;
        mContext = context;
        preferencesPlayList = context.getSharedPreferences(StaticVariate.playList, MODE_PRIVATE);
        preferencesSet = context.getSharedPreferences(StaticVariate.keySet,MODE_PRIVATE);
        playPosition = preferencesPlayList.getInt("position", 0);
        dataBase = new DataBase(mContext, StaticVariate.dataBaseName, null, 1);
        db = dataBase.getWritableDatabase();
    }

    public SongListAdapter(Context context, Cursor cursor, String table) {
        mLayoutInflater = LayoutInflater.from(context);
        if (cursor == null) {
            this.songNumber = 0;
            this.cursor = null;
        } else {
            this.songNumber = cursor.getCount();
            this.cursor = cursor;
        }
        this.table = table;
        mContext = context;
        preferencesPlayList = context.getSharedPreferences(StaticVariate.playList, MODE_PRIVATE);
        preferencesSet = context.getSharedPreferences(StaticVariate.keySet,MODE_PRIVATE);
        playPosition = preferencesPlayList.getInt("position", 0);
        playTable = preferencesPlayList.getString(StaticVariate.keyListName, StaticVariate.localSongListTable);
        fileUrl = preferencesPlayList.getString(StaticVariate.fileUrl, "");
        dataBase = new DataBase(mContext, StaticVariate.dataBaseName, null, 1);
        db = dataBase.getWritableDatabase();
    }

    public int getCount() {
        return songNumber;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = mLayoutInflater.inflate(R.layout.item_song_list, null);
            holder = new ViewHolder();
            holder.textSongName = convertView.findViewById(R.id.text_song_name);
            holder.textSinger = convertView.findViewById(R.id.text_song_singer);
            holder.textSongPosition = convertView.findViewById(R.id.text_song_position);
            holder.imgPlaying = convertView.findViewById(R.id.img_playing);
            holder.imgSongListMenu = convertView.findViewById(R.id.img_song_list_menu);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        if (cursor == null && song != null) {
            if ((playPosition == position) && playTable.equals(table)) {
                holder.imgPlaying.setVisibility(View.VISIBLE);
                holder.textSongPosition.setVisibility(View.GONE);
            } else {
                holder.textSongPosition.setText("" + (position + 1));
                holder.textSongPosition.setVisibility(View.VISIBLE);
                holder.imgPlaying.setVisibility(View.GONE);

            }
            holder.textSongName.setText(song.get(position).getTitle());
            holder.textSinger.setText(song.get(position).getSinger());
            holder.imgSongListMenu.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onSongListItemMenuClickListener.onSongListItemMenuClick(position);
                }
            });
        } else if (cursor != null) {
            boolean b ;                  //判断是否设置播放图标
            cursor.moveToPosition(position);
            if (playTable.equals(table)) {
                if (fileUrl.isEmpty()) {
                    b = (playPosition == position);
                } else {
                    b = fileUrl.equals(cursor.getString(cursor.getColumnIndex(StaticVariate.fileUrl)));
                }
            }else {
                b = false;
            }
            if (b) {
                holder.imgPlaying.setVisibility(View.VISIBLE);
                holder.textSongPosition.setVisibility(View.GONE);
            } else {
                holder.textSongPosition.setText("" + (position + 1));
                holder.textSongPosition.setVisibility(View.VISIBLE);
                holder.imgPlaying.setVisibility(View.GONE);
            }
            holder.textSongName.setText(cursor.getString(1));
            holder.textSinger.setText(cursor.getString(2));
            holder.imgSongListMenu.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onSongListItemMenuClickListener.onSongListItemMenuClick(position);
                }
            });
        }
        return convertView;
    }

    static class ViewHolder {
        TextView textSongName;
        TextView textSinger;
        TextView textSongPosition;
        ImageView imgPlaying;
        ImageView imgSongListMenu;
    }

    public interface OnSongListItemMenuClickListener {
        void onSongListItemMenuClick(int position);
    }

    public void setOnItemMenuClickListener(OnSongListItemMenuClickListener listener) {
        this.onSongListItemMenuClickListener = listener;
    }

    public void clear() {
        songNumber = 0;
    }

    public void changeData() {
        if(table.equals(StaticVariate.recentlySongListTable)){
            cursor = db.rawQuery("select * from " + table
                    + " order by " + StaticVariate.addTime
                    + " desc ", null);
        }else {
            cursor = db.rawQuery(getSqlBaseOrder(table,preferencesSet), null);
        }
        songNumber = cursor.getCount();
        playPosition = preferencesPlayList.getInt("position", 0);
        playTable = preferencesPlayList.getString(StaticVariate.keyListName, StaticVariate.localSongListTable);
        fileUrl = preferencesPlayList.getString(StaticVariate.fileUrl, "");
    }

    public void setPlayPosition() {
        playPosition = -1;
    }
}
