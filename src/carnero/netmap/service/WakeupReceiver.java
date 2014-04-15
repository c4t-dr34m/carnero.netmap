package carnero.netmap.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import carnero.netmap.common.Constants;
import carnero.netmap.common.Preferences;

public class WakeupReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		if (!Preferences.isKilled(context)) {
			final Intent serviceIntent = new Intent(context, MainService.class);
			serviceIntent.putExtra(Constants.EXTRA_WAKEUP, true);
			context.startService(serviceIntent);
		}
	}
}
