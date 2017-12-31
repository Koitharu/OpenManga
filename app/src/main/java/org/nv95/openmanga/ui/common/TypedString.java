package org.nv95.openmanga.ui.common;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;

import org.nv95.openmanga.R;

public final class TypedString {

	@StringRes
	private final int mStringResId;
	private final String mString;
	private final int mType;
	private final int mSubPos;

	public TypedString(@NonNull Context context, @StringRes int stringResId, int type, int subPos) {
		mStringResId = stringResId;
		mString = context.getString(stringResId);
		mType = type;
		mSubPos = subPos;
	}

	public int getType() {
		return mType;
	}

	public int getSubPosition() {
		return mSubPos;
	}

	@NonNull
	@Override
	public String toString() {
		return mString;
	}

	@Override
	public int hashCode() {
		return mStringResId;
	}
}