package carnero.netmap.activity;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import carnero.netmap.R;
import carnero.netmap.common.Preferences;
import carnero.netmap.common.Util;
import carnero.netmap.fragment.NetMapFragment;
import carnero.netmap.iface.IBackHandler;
import carnero.netmap.model.Sector;
import carnero.netmap.service.MainService;

public class MainActivity extends Activity {

	protected Fragment mFragment;
	//
	protected View vBtnSectorsContainer;
	protected TextView vBtnSectors;
	protected ImageView vBtnMarkers;

	@Override
	public void onCreate(Bundle state) {
		super.onCreate(state);

		setContentView(R.layout.activity_main);

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
			Window window = getWindow();
			window.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION, WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
			window.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
		}

		vBtnSectorsContainer = findViewById(R.id.btn_sectors_container);
		vBtnSectors = (TextView)findViewById(R.id.btn_sectors);
		vBtnMarkers = (ImageButton)findViewById(R.id.btn_markers);

		checkMarkers();
		vBtnMarkers.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Preferences.switchMarkers(MainActivity.this);
				checkMarkers();
			}
		});

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
	protected void onResume() {
		super.onResume();

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
			findViewById(R.id.actionbar_background).getLayoutParams().height = Util.getTopPanelsHeight(this);
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

	protected void checkMarkers() {
		if (Preferences.isSetMarkers(this)) {
			vBtnMarkers.setImageResource(R.drawable.ic_ab_markers_off);
		} else {
			vBtnMarkers.setImageResource(R.drawable.ic_ab_markers);
		}

		if (mFragment instanceof NetMapFragment) {
			((NetMapFragment)mFragment).checkMarkers();
		}
	}

	public void displayInfo(Sector sector) {
		vBtnSectors.setText(getResources().getTextArray(R.array.network_types)[sector.network]);

		vBtnSectorsContainer.setVisibility(View.VISIBLE);
	}

	public void hideInfo() {
		vBtnSectorsContainer.setVisibility(View.GONE);
	}
}