package carnero.netmap.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import carnero.netmap.common.Preferences;

public class BootReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		if (!Preferences.isKilled(context)) {
			final Intent serviceIntent = new Intent(context, MainService.class);
			context.startService(serviceIntent);
		}
	}
}