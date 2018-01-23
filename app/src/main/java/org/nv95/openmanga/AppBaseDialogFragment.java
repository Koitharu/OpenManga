package org.nv95.openmanga;

import android.support.v7.app.AppCompatDialogFragment;
import android.view.ViewGroup;
import android.view.Window;

/**
 * Created by koitharu on 23.01.18.
 */

public abstract class AppBaseDialogFragment extends AppCompatDialogFragment {

	@Override
	public void onResume() {
		super.onResume();
		Window window = getDialog().getWindow();
		if (window != null) {
			window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
		}
	}
}
