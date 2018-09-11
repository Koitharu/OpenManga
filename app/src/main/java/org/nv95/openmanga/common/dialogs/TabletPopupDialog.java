package org.nv95.openmanga.common.dialogs;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.StyleRes;
import android.support.v7.app.AppCompatDialog;
import android.view.Window;

//TODO use it
class TabletPopupDialog extends AppCompatDialog {


	public TabletPopupDialog(@NonNull Context context) {
		this(context, 0);
	}

	public TabletPopupDialog(@NonNull Context context, @StyleRes int theme) {
		super(context, theme);
		// We hide the title bar for any style configuration. Otherwise, there will be a gap
		// above the bottom sheet when it is expanded.
		supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
	}
}
