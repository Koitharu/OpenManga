package org.nv95.openmanga.common;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * Created by koitharu on 06.02.18.
 */

public final class StringJoinerCompat {

	private final String mPrefix;
	private final String mDelimiter;
	private final String mSuffix;

	@Nullable
	private StringBuilder mValue;

	private String mEmptyValue;

	public StringJoinerCompat(@NonNull CharSequence delimiter) {
		this(delimiter, "", "");
	}

	public StringJoinerCompat(@NonNull CharSequence delimiter, @NonNull CharSequence prefix, @NonNull CharSequence suffix) {
		mPrefix = prefix.toString();
		mDelimiter = delimiter.toString();
		mSuffix = suffix.toString();
		mEmptyValue = mPrefix + mSuffix;
	}

	public StringJoinerCompat setEmptyValue(@NonNull CharSequence emptyValue) {
		mEmptyValue = emptyValue.toString();
		return this;
	}

	@Override
	public String toString() {
		if (mValue == null) {
			return mEmptyValue;
		} else {
			if (mSuffix.equals("")) {
				return mValue.toString();
			} else {
				int initialLength = mValue.length();
				String result = mValue.append(mSuffix).toString();
				mValue.setLength(initialLength);
				return result;
			}
		}
	}

	public StringJoinerCompat add(CharSequence newElement) {
		prepareBuilder().append(newElement);
		return this;
	}

	public StringJoinerCompat merge(@NonNull StringJoinerCompat other) {
		if (other.mValue != null) {
			final int length = other.mValue.length();
			StringBuilder builder = prepareBuilder();
			builder.append(other.mValue, other.mPrefix.length(), length);
		}
		return this;
	}

	private StringBuilder prepareBuilder() {
		if (mValue != null) {
			mValue.append(mDelimiter);
		} else {
			mValue = new StringBuilder().append(mPrefix);
		}
		return mValue;
	}

	public int length() {
		return (mValue != null ? mValue.length() + mSuffix.length() : mEmptyValue.length());
	}
}
