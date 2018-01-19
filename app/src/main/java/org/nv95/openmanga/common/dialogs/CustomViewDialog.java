package org.nv95.openmanga.common.dialogs;

import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;

/**
 * Created by koitharu on 19.01.18.
 */

public abstract class CustomViewDialog implements DialogInterface, DialogInterface.OnClickListener {

	protected final AlertDialog.Builder mDialogBuilder;
	@Nullable
	private AlertDialog mDialog = null;

	public CustomViewDialog(Context context) {
		mDialogBuilder = new AlertDialog.Builder(context);
		final View view = onCreateView(LayoutInflater.from(context));
		onViewCreated(view);
		mDialogBuilder.setView(view);
	}

	@NonNull
	public abstract View onCreateView(@NonNull LayoutInflater inflater);

	public abstract void onViewCreated(@NonNull View view);

	public void show() {
		mDialog = mDialogBuilder.create();
		mDialog.show();
	}

	@Override
	public void cancel() {
		if (mDialog != null) {
			mDialog.cancel();
		}
	}

	@Override
	public void dismiss() {
		if (mDialog != null) {
			mDialog.dismiss();
		}
	}

	@Override
	public void onClick(DialogInterface dialog, int which) {

	}
}
