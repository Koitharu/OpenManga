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

	public ListHeader(@Nullable String text) {
		this.text = text;
		textResId = 0;
	}

	public ListHeader(int textResId) {
		this.textResId = textResId;
		text = null;
	}
}
