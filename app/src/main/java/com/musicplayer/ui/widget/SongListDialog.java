package com.musicplayer.ui.widget;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.musicplayer.R;

class SongListDialog extends Dialog {
    private ListView listSongListDialog;
    private Context context;
    private String item[] = {"添加或移除收藏", "添加到歌单","删除"};
    public SongListDialog(Context context) {
        super(context);
        this.context = context;
    }

//    public SongListDialog(Context context, int themeResId) {
//        super(context, themeResId);
//    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_song_list);
        listSongListDialog = findViewById(R.id.list_song_list_dialog);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(context,R.layout.item_song_list_dialog,
                R.id.text_song_list_dialog_item,item);
        listSongListDialog.setAdapter(adapter);
        listSongListDialog.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                myOnItemClickListener.myOnItemClick(position);
            }
        });
    }

    interface MyOnItemClickListener{
        void myOnItemClick(int position);
    }
    private MyOnItemClickListener myOnItemClickListener;

    public void setMyOnItemMenuClickListener(MyOnItemClickListener myOnItemClickListener){
        this.myOnItemClickListener = myOnItemClickListener;
    }
}
