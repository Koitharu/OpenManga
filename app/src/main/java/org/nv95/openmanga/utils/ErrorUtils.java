package org.nv95.openmanga.utils;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;

import org.json.JSONException;
import org.nv95.openmanga.R;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * Created by koitharu on 12.01.18.
 */

public final class ErrorUtils {

	@StringRes
	public static int getErrorMessage(@Nullable Throwable error) {
		if (error == null) {
			return R.string.error;
		} else if (error instanceof JSONException) {
			return R.string.error_bad_response;
		} else if (error instanceof IOException) {
			return R.string.loading_error;
		} else { //TODO
			return R.string.error;
		}
	}

	@NonNull
	public static String getStackTrace(@NonNull Throwable throwable) {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		throwable.printStackTrace(pw);
		return sw.toString();
	}
}
