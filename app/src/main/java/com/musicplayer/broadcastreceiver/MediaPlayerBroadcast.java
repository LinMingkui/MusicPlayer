package com.musicplayer.broadcastreceiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class MediaPlayerBroadcast extends BroadcastReceiver {
    private static final int PLAY = 1;
    private static final int PAUSE = 2;
    private static final int STOP = 3;
    public static final int FORWARD = 4;
    public static final int BACKWARD = 5;

    private static boolean isPause = false;
    private static boolean isStop = false;
    @Override
    public void onReceive(Context context, Intent intent) {
//            Log.e("****", "广播");
        int type = intent.getIntExtra("type", -1);
        switch (type) {
            case PLAY:
                isPause = false;
                break;
            case PAUSE:
                isPause = true;
                break;
            case STOP:
                isStop = true;
                Log.e("****", "停止广播");
                break;
//                case FORWARD:
//                    ;
//                    break;
//                case BACKWARD:
//                    ;
//                    break;
            default:
                break;
        }
    }
}
