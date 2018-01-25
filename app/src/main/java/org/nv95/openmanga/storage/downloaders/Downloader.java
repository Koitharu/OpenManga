package org.nv95.openmanga.storage.downloaders;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.WorkerThread;

import java.io.File;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Created by koitharu on 25.01.18.
 */

public abstract class Downloader<T> implements Runnable {

	private final T mSource;
	private final File mDestination;
	private final AtomicReference<Boolean> mStatus;
	@Nullable
	private Callback mCallback;

	public Downloader(@NonNull T source, @NonNull File destination) {
		mSource = source;
		mDestination = destination;
		mStatus = new AtomicReference<>(null);
	}

	public Downloader(@NonNull T source, @NonNull String destination) {
		this(source, new File(destination));
	}

	public Downloader<T> setCallback(@Nullable Callback callback) {
		mCallback = callback;
		return this;
	}

	@Override
	public final void run() {
		final boolean result = onDownload(mSource, mDestination);
		mStatus.set(result);
	}

	public final boolean isCompleted() {
		return mStatus.get() != null;
	}

	public final boolean isSuccess() {
		return Boolean.TRUE.equals(mStatus.get());
	}

	protected boolean isCancelled() {
		return mCallback != null && mCallback.isCancelled();
	}

	protected boolean isPaused() {
		return mCallback != null && mCallback.isPaused();
	}

	@WorkerThread
	protected abstract boolean onDownload(@NonNull T source, @NonNull File destination);

	public interface Callback {

		@WorkerThread
		boolean isCancelled();

		@WorkerThread
		boolean isPaused();
	}
}
