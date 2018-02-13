package org.nv95.openmanga.common.dialogs;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetDialog;
import android.support.design.widget.BottomSheetDialogFragment;

import org.nv95.openmanga.common.utils.ThemeUtils;

public abstract class AppBaseBottomSheetDialogFragment extends BottomSheetDialogFragment {

	@NonNull
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		return new BottomSheetDialog(getContext(), ThemeUtils.getBottomSheetTheme(getContext()));
	}
}
