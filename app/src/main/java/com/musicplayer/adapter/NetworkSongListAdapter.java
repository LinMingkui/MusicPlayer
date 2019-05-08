package com.musicplayer.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.musicplayer.R;
import com.musicplayer.utils.Song;
import com.musicplayer.utils.Variate;

import java.util.List;

public class NetworkSongListAdapter extends BaseAdapter {

    private Context mContext;
    private OnSongListItemMenuClickListener onSongListItemMenuClickListener;
    private List<Song> songList;
    private LayoutInflater layoutInflater;
    private int songType;

    public NetworkSongListAdapter(Context context, List<Song> list, int songType) {
        layoutInflater = LayoutInflater.from(context);
        mContext = context;
        songList = list;
        this.songType = songType;
    }

    @Override
    public int getCount() {
        return songList.size();
    }

    @Override
    public Song getItem(int position) {
        return songList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = layoutInflater.inflate(R.layout.item_song_list, null);
            holder.tvPosition = convertView.findViewById(R.id.text_song_position);
            holder.tvTitle = convertView.findViewById(R.id.text_song_name);
            holder.tvSinger = convertView.findViewById(R.id.text_song_singer);
            holder.imgMenu = convertView.findViewById(R.id.img_song_list_menu);
            holder.imgSongType = convertView.findViewById(R.id.img_song_type);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        switch (songType) {
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
        holder.tvTitle.setText(songList.get(position).getSongName());
        holder.tvSinger.setText(songList.get(position).getSinger());
        holder.tvPosition.setText(String.valueOf(position + 1));
        View finalConvertView = convertView;
        holder.imgMenu.setOnClickListener(v -> onSongListItemMenuClickListener.onSongListItemMenuClick(finalConvertView, position));
        return convertView;
    }

    private class ViewHolder {
        private TextView tvPosition;
        private TextView tvTitle;
        private TextView tvSinger;
        private ImageView imgMenu;
        private ImageView imgSongType;
    }

    public interface OnSongListItemMenuClickListener {
        void onSongListItemMenuClick(View view, int position);
    }

    public void setOnItemMenuClickListener(OnSongListItemMenuClickListener listener) {
        this.onSongListItemMenuClickListener = listener;
    }
}
