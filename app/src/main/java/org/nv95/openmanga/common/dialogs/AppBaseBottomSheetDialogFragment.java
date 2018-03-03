package org.nv95.openmanga.common.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetDialog;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatDialogFragment;

import org.nv95.openmanga.common.utils.LayoutUtils;
import org.nv95.openmanga.common.utils.ResourceUtils;
import org.nv95.openmanga.common.utils.ThemeUtils;

public abstract class AppBaseBottomSheetDialogFragment extends AppCompatDialogFragment {

	@NonNull
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		final Context context = getContext();
		return new BottomSheetDialog(context, ThemeUtils.getBottomSheetTheme(getContext()));
	}
}
