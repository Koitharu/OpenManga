package org.nv95.openmanga.reader;

import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;

import org.nv95.openmanga.R;
import org.nv95.openmanga.core.models.MangaHistory;

/**
 * Created by koitharu on 30.01.18.
 */

final class ReaderModeDialog implements DialogInterface.OnClickListener {

	private final AlertDialog.Builder mBuilder;
	@Nullable
	private OnReaderModeChangeListener mListener = null;

	ReaderModeDialog(@NonNull Context context, @NonNull MangaHistory history) {
		mBuilder = new AlertDialog.Builder(context);
		mBuilder.setTitle(R.string.reader_mode);
		mBuilder.setSingleChoiceItems(R.array.reader_modes, history.readerPreset, this);
		mBuilder.setNegativeButton(R.string.close, null);
		mBuilder.setCancelable(true);
	}

	ReaderModeDialog setListener(@Nullable OnReaderModeChangeListener listener) {
		mListener = listener;
		return this;
	}

	public void show() {
		mBuilder.create().show();
	}

	@Override
	public void onClick(DialogInterface dialog, int which) {
		if (mListener != null) {
			mListener.onReaderModeChanged((short) which);
		}
		dialog.dismiss();
	}

	interface OnReaderModeChangeListener {

		void onReaderModeChanged(short mode);
	}
}
