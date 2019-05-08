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
import com.musicplayer.utils.Variate;

import static android.content.Context.MODE_PRIVATE;
import static com.musicplayer.utils.MethodUtils.getSqlBaseOrder;

public class SongListAdapter extends BaseAdapter {

    private DataBase dataBase;
    private SQLiteDatabase db;
    private int songNumber, playPosition;
    private String table;
    private String playTable;
    private String songUrl, songMid;
    private Context mContext;
    private SharedPreferences preferencesPlayList;
    private SharedPreferences preferencesSet;
    private Cursor cursor;
    private LayoutInflater mLayoutInflater;
    private OnSongListItemMenuClickListener onSongListItemMenuClickListener;

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
        preferencesPlayList = context.getSharedPreferences(Variate.playList, MODE_PRIVATE);
        preferencesSet = context.getSharedPreferences(Variate.set, MODE_PRIVATE);
        playPosition = preferencesPlayList.getInt("position", 0);
        playTable = preferencesPlayList.getString(Variate.keyTableName, Variate.localSongListTable);
        songUrl = preferencesPlayList.getString(Variate.keySongUrl, "");
        songMid = preferencesPlayList.getString(Variate.keySongMid, "");
        dataBase = new DataBase(mContext, Variate.dataBaseName, null, 1);
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
            holder.imgSongType = convertView.findViewById(R.id.img_song_type);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        if (cursor != null) {
            boolean b;                  //判断是否设置播放图标
            cursor.moveToPosition(position);
            if (playTable.equals(table)) {
                if (preferencesPlayList.getInt(Variate.keySongType, Variate.SONG_TYPE_LOCAL) == Variate.SONG_TYPE_LOCAL) {
                    b = songUrl.equals(cursor.getString(cursor.getColumnIndex(Variate.keySongUrl)));
                } else {
                    b = songMid.equals(cursor.getString(cursor.getColumnIndex(Variate.keySongMid)));
                }
            } else {
                b = false;
            }
            switch (cursor.getInt(cursor.getColumnIndex(Variate.keySongType))) {
                case Variate.SONG_TYPE_QQ:
                    holder.imgSongType.setImageResource(R.drawable.ic_song_type_qq);
                    break;
                case Variate.SONG_TYPE_KG:
                    holder.imgSongType.setImageResource(R.drawable.ic_song_type_kg);
                    break;
                case Variate.SONG_TYPE_WYY:
                    holder.imgSongType.setImageResource(R.drawable.ic_song_type_wyy);
                    break;
                default:
                    holder.imgSongType.setImageResource(R.drawable.ic_song_type_local);
                    break;
            }
            if (b) {
                holder.imgPlaying.setVisibility(View.VISIBLE);
                holder.textSongPosition.setVisibility(View.GONE);
            } else {
                holder.textSongPosition.setText(String.valueOf(position + 1));
                holder.textSongPosition.setVisibility(View.VISIBLE);
                holder.imgPlaying.setVisibility(View.GONE);
            }
            holder.textSongName.setText(cursor.getString(cursor.getColumnIndex(Variate.keySongName)));
            holder.textSinger.setText(cursor.getString(cursor.getColumnIndex(Variate.keySinger)));
            View view = convertView;
            holder.imgSongListMenu.setOnClickListener(v -> {
                if (onSongListItemMenuClickListener != null) {
                    onSongListItemMenuClickListener.onSongListItemMenuClick(view, position);
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
        ImageView imgSongType;
    }

    public interface OnSongListItemMenuClickListener {
        void onSongListItemMenuClick(View view, int position);
    }

    public void setOnItemMenuClickListener(OnSongListItemMenuClickListener listener) {
        this.onSongListItemMenuClickListener = listener;
    }

    public void clear() {
        songNumber = 0;
    }

    public void changeData() {
        cursor = db.rawQuery(getSqlBaseOrder(table, preferencesSet), null);
        songNumber = cursor.getCount();
        playPosition = preferencesPlayList.getInt("position", 0);
        playTable = preferencesPlayList.getString(Variate.keyTableName, Variate.localSongListTable);
        songUrl = preferencesPlayList.getString(Variate.keySongUrl, "");
        songMid = preferencesPlayList.getString(Variate.keySongMid, "");
    }
}
