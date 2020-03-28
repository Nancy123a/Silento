package com.example.silento;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

public class InternetConnector_Receiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        try {
            ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
            if (networkInfo != null && networkInfo.isConnected()) {
                    Log.d("action: ","Nancy Internet abc is connected");
            }
            else{
                    Log.d("action: ","Nancy Internet abc not connected");
                   // MainActivity.ConnectToInternet();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
