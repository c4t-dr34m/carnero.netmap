package carnero.netmap.fragment;

import java.text.DateFormat;
import java.util.Date;

import android.content.Context;
import android.view.View;

import carnero.netmap.R;
import carnero.netmap.database.DatabaseHelper;
import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.MapBuilder;

public class ImportDialog extends AbstractDialog {

	protected Context mContext;
	//
	protected static Listener listener;

	public static ImportDialog newInstance(Listener listener) {
		ImportDialog fragment = new ImportDialog();
		fragment.listener = listener;
		return fragment;
	}

	public void setupView() {
		mContext = getActivity().getApplicationContext();

		long fileDate = DatabaseHelper.getImportFileDate();
		DateFormat dateFormat = DateFormat.getDateInstance();
		StringBuilder fileInfo = new StringBuilder();
		fileInfo.append(DatabaseHelper.getImportFileName());
		if (fileDate > 0) {
			fileInfo.append(", ");
			fileInfo.append(dateFormat.format(new Date(fileDate)));
		}

		String description = getResources().getString(R.string.dialog_import_description, fileInfo.toString());
		vMessage.setText(description);

		vButtonPositive.setText(R.string.dialog_import);
		vButtonPositive.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (listener != null) {
					listener.ok();
				}

				// analytics
				EasyTracker easyTracker = EasyTracker.getInstance(getActivity());
				easyTracker.send(MapBuilder.createEvent(
						"ui", // category
						"import", // action
						null, // label
						null // value
					).build()
				);

				dismiss();
			}
		});

		vButtonNegative.setText(R.string.dialog_cancel);
		vButtonNegative.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (listener != null) {
					listener.cancel();
				}
				dismiss();
			}
		});
	}

	// classes

	public interface Listener {

		public void ok();

		public void cancel();
	}
}
