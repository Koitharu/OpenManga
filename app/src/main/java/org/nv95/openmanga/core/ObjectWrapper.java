package org.nv95.openmanga.core;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * Created by koitharu on 12.01.18.
 */

public class ObjectWrapper<T> {

	@Nullable
	protected final T mObject;
	@Nullable
	private final Throwable mThrowable;

	public ObjectWrapper(@NonNull T object) {
		mObject = object;
		mThrowable = null;
	}

	public ObjectWrapper(@NonNull Throwable error) {
		mObject = null;
		mThrowable = error;
	}

	public T get() {
		return mObject;
	}

	public Throwable getError() {
		return mThrowable;
	}

	public boolean isSuccess() {
		return mThrowable == null;
	}

	public boolean isFailed() {
		return mThrowable != null;
	}

	public static class BadResultException extends Exception {}

	@NonNull
	public static <T> ObjectWrapper<T> badObject() {
		return new ObjectWrapper<T>(new BadResultException());
	}
}
