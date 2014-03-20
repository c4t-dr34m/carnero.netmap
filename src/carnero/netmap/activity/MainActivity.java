package carnero.netmap.activity;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import carnero.netmap.R;
import carnero.netmap.fragment.NetMapFragment;
import carnero.netmap.iface.IBackHandler;
import carnero.netmap.model.Sector;
import carnero.netmap.service.MainService;

public class MainActivity extends Activity {

	protected Fragment mFragment;
	//
	protected View vSectorInfo;
	protected TextView vSectorCoords;
	protected TextView vSectorNetwork;

	@Override
	public void onCreate(Bundle state) {
		super.onCreate(state);

		setContentView(R.layout.activity_main);

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
			Window window = getWindow();
			window.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION, WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
			window.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
		}

		vSectorInfo = findViewById(R.id.sector_info);
		vSectorCoords = (TextView)findViewById(R.id.sector_coords);
		vSectorNetwork = (TextView)findViewById(R.id.sector_network);

		if (state == null) {
			mFragment = new NetMapFragment();

			getFragmentManager()
				.beginTransaction()
				.replace(R.id.fragment_container, mFragment)
				.commit();
		}

		if (!MainService.isRunning()) {
			final Intent serviceIntent = new Intent(this, MainService.class);
			startService(serviceIntent);
		}
	}

	@Override
	public void onBackPressed() {
		boolean status = false;
		if (mFragment instanceof IBackHandler) {
			status = ((IBackHandler)mFragment).onBackPressed();
		}

		if (!status) {
			super.onBackPressed();
		}
	}

	public void displayInfo(Sector sector) {
		vSectorCoords.setText(sector.index.x + "," + sector.index.y);
		vSectorNetwork.setText(getResources().getTextArray(R.array.network_types)[sector.network]);

		vSectorInfo.setVisibility(View.VISIBLE);
	}

	public void hideInfo() {
		vSectorInfo.setVisibility(View.GONE);
	}
}