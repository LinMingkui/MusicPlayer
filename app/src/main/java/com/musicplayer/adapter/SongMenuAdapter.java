package com.musicplayer.adapter;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.musicplayer.R;
import com.musicplayer.utils.Variate;


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
            holder.imgSongMenu = convertView.findViewById(R.id.img_song_menu);
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
//        holder.imgSongMenu.
        }
        return convertView;
    }

    class ViewHolder {
        ImageView imgSongMenu;
        TextView textViewSongMenuName;
        TextView textViewSongNumber;
    }
}
