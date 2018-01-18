package org.nv95.openmanga.core.models;

import android.support.annotation.Nullable;
import android.support.annotation.StringRes;

/**
 * Created by koitharu on 24.12.17.
 */

public class ListHeader {

	@StringRes
	public final int textResId;
	@Nullable
	public final String text;
	@Nullable
	public final Object extra;

	public ListHeader(@Nullable String text, @Nullable Object extra) {
		this.text = text;
		this.textResId = 0;
		this.extra = extra;
	}

	public ListHeader(int textResId, @Nullable Object extra) {
		this.textResId = textResId;
		this.text = null;
		this.extra = extra;
	}
}
