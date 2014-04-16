package carnero.netmap.activity;

import java.io.File;

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

import carnero.netmap.App;
import carnero.netmap.R;
import carnero.netmap.common.Constants;
import carnero.netmap.common.Preferences;
import carnero.netmap.common.Util;
import carnero.netmap.database.DatabaseHelper;
import carnero.netmap.fragment.ImportDialog;
import carnero.netmap.fragment.NetMapFragment;
import carnero.netmap.iface.IBackHandler;
import carnero.netmap.model.Sector;
import carnero.netmap.service.MainService;
import com.google.analytics.tracking.android.EasyTracker;

public class MainActivity extends Activity {

	protected Fragment mFragment;
	//
	protected View vSeparator;
	protected TextView vOperator;
	protected View vSectorContainer;
	protected TextView vSector;
	protected ImageView vMarkers;
	protected ImageView vKill;

	@Override
	public void onCreate(Bundle state) {
		super.onCreate(state);

		setContentView(R.layout.activity_main);

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
			Window window = getWindow();
			window.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION, WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
			window.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
		}

		File importFile = DatabaseHelper.getImportFile();
		if (importFile.exists()) {
			ImportDialog.Listener listener = new ImportDialog.Listener() {
				@Override
				public void ok() {
					App.getDatabaseHelper().importDB();

					// kill it, kill it with fire
					System.exit(0);
				}

				@Override
				public void cancel() {
					// empty
				}
			};

			ImportDialog dialog = ImportDialog.newInstance(listener);
			dialog.show(getFragmentManager(), ImportDialog.class.getName());
		}

		vSeparator = findViewById(R.id.actionbar_separator);
		vOperator = (TextView)findViewById(R.id.operator);
		vMarkers = (ImageButton)findViewById(R.id.btn_markers);
		vKill = (ImageButton)findViewById(R.id.btn_kill);
		vSectorContainer = findViewById(R.id.sector_container);
		vSector = (TextView)findViewById(R.id.sector);

		StringBuilder operator = new StringBuilder(App.getOperatorID());
		operator.insert(3, "'");
		vOperator.setText(operator.toString());

		checkMarkers();
		vMarkers.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Preferences.switchMarkers(MainActivity.this);
				checkMarkers();
			}
		});
		vKill.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				killService();
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
			serviceIntent.putExtra(Constants.EXTRA_RESURRECT, true);
			startService(serviceIntent);
		}
	}

	@Override
	protected void onResume() {
		super.onResume();

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
			findViewById(R.id.actionbar).getLayoutParams().height = Util.getTopPanelsHeight(this);
		}
	}

	@Override
	protected void onStart() {
		super.onStart();
		EasyTracker.getInstance(this).activityStart(this);
	}

	@Override
	protected void onStop() {
		super.onStop();
		EasyTracker.getInstance(this).activityStop(this);
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
			vMarkers.setImageResource(R.drawable.ic_ab_markers_off);
		} else {
			vMarkers.setImageResource(R.drawable.ic_ab_markers);
		}

		if (mFragment instanceof NetMapFragment) {
			((NetMapFragment)mFragment).checkMarkers();
		}
	}

	protected void killService() {
		final Intent killIntent = new Intent(this, MainService.class);
		killIntent.putExtra(Constants.EXTRA_KILL_WITH_FIRE, true);
		killIntent.putExtra(Constants.EXTRA_KILL_REMEMBER, true);

		startService(killIntent);
	}

	public void displayInfo(Sector sector) {
		vSector.setText(getResources().getTextArray(R.array.network_types)[sector.network]);

		vSeparator.setBackgroundResource(R.color.bcg_actionbar_line_on);
		vSectorContainer.setVisibility(View.VISIBLE);
	}

	public void hideInfo() {
		vSectorContainer.setVisibility(View.GONE);
		vSeparator.setBackgroundResource(R.color.bcg_actionbar_line_off);
	}
}