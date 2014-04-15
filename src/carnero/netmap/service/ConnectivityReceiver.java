package carnero.netmap.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import carnero.netmap.common.Constants;
import carnero.netmap.common.Preferences;

public class ConnectivityReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		if (Preferences.isKilled(context)) {
			return;
		}

		ConnectivityManager manager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo wifi = manager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		boolean wifiConnected = (wifi != null && wifi.isConnected());

		final Intent serviceIntent = new Intent(context, MainService.class);
		if (wifiConnected && MainService.isRunning()) {
			serviceIntent.putExtra(Constants.EXTRA_KILL_WITH_FIRE, true);
			context.startService(serviceIntent);

			Log.d(Constants.TAG, "WiFi connected .. disabling service");
		} else if (!wifiConnected && !MainService.isRunning()) {
			context.startService(serviceIntent);

			Log.d(Constants.TAG, "WiFi disconnected .. resuming service");
		}
	}
}