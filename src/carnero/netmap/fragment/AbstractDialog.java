package carnero.netmap.fragment;

import android.app.Activity;
import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import carnero.netmap.R;

public abstract class AbstractDialog extends DialogFragment {

	protected Activity mActivity;
	//
	protected TextView vTitle;
	protected TextView vMessage;
	protected View vButtons;
	protected Button vButtonPositive;
	protected Button vButtonNegative;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		mActivity = getActivity();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle state) {
		getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);

		View layout = inflater.inflate(R.layout.dialog, container, false);

		vTitle = (TextView)layout.findViewById(R.id.title);
		vMessage = (TextView)layout.findViewById(R.id.message);
		vButtons = layout.findViewById(R.id.buttons);
		vButtonPositive = (Button)layout.findViewById(R.id.btn_positive);
		vButtonNegative = (Button)layout.findViewById(R.id.btn_negative);

		setupView();

		return layout;
	}

	protected abstract void setupView();
}
