package carnero.netmap.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import carnero.netmap.common.Constants;

public class ConnectivityReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo wifi = manager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        boolean wifiConnected = (wifi != null && wifi.isConnected());

        final Intent serviceIntent = new Intent(context, MainService.class);
        if (wifiConnected && MainService.isRunning()) {
            context.stopService(serviceIntent);
            Log.d(Constants.TAG, "WiFi connected .. disabling service");
        } else if (!wifiConnected && !MainService.isRunning()) {
            context.startService(serviceIntent);
            Log.d(Constants.TAG, "WiFi disconnected .. resuming service");
        }
    }
}