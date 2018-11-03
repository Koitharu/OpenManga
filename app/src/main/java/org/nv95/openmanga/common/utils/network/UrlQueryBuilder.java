package org.nv95.openmanga.common.utils.network;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.HashMap;
import java.util.Map;

public final class UrlQueryBuilder {

	private final String mPath;
	private final HashMap<String,String> mParams;

	public UrlQueryBuilder(String path) {
		mPath = path;
		mParams = new HashMap<>();
	}

	@NonNull
	@Override
	public String toString() {
		if (mParams.isEmpty()) {
			return mPath;
		} else {
			final StringBuilder builder = new StringBuilder(mPath).append("?");
			for (Map.Entry<String,String> o : mParams.entrySet()) {
				builder.append(o.getKey())
						.append("=")
						.append(o.getValue())
						.append("&");
			}
			builder.setLength(builder.length() - 1);
			return builder.toString();
		}
	}

	public UrlQueryBuilder put(String param, String value) {
		mParams.put(param, value);
		return this;
	}

	public UrlQueryBuilder put(String param, int value) {
		mParams.put(param, String.valueOf(value));
		return this;
	}

	public UrlQueryBuilder put(String param, Object value) {
		mParams.put(param, value.toString());
		return this;
	}
}
