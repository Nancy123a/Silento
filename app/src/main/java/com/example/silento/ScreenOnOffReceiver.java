package com.example.silento;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import java.util.Iterator;
import java.util.List;

import static android.content.Context.ACTIVITY_SERVICE;

/**
 * Created by Jerry on 1/5/2018.
 */

public class ScreenOnOffReceiver extends BroadcastReceiver {

    private final static String SCREEN_TOGGLE_TAG = "NANCY";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
         if(Intent.ACTION_SCREEN_ON.equals(action))
        {
            if(!isAppRunning(context)){
                Intent i =context.getPackageManager().getLaunchIntentForPackage("com.example.silento");
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                context.startActivity(i);

            }
        }
    }

    private boolean isAppRunning(Context context) {
        ActivityManager m = (ActivityManager) context.getSystemService( ACTIVITY_SERVICE );
        List<ActivityManager.RunningTaskInfo> runningTaskInfoList =  m.getRunningTasks(10);
        Iterator<ActivityManager.RunningTaskInfo> itr = runningTaskInfoList.iterator();
        int n=0;
        while(itr.hasNext()){
            n++;
            itr.next();
        }
        if(n==1){ // App is killed
            return false;
        }

        return true; // App is in background or foreground
    }
}