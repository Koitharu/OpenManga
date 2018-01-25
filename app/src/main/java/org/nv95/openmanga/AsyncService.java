package org.nv95.openmanga;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.WorkerThread;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by koitharu on 25.01.18.
 */

public abstract class AsyncService<R> extends Service implements Handler.Callback {

	private static final int MSG_PRE_EXECUTE = 1;
	private static final int MSG_POST_EXECUTE = 2;
	private static final int MSG_UPDATE_PROGRESS = 3;
	private static final int MSG_STOP_SELF = 4;

	private Handler mHandler;
	@Nullable
	private BackgroundThread mThread;
	private LinkedBlockingQueue<R> mQueue;
	private final AtomicBoolean mCancelled = new AtomicBoolean();

	@Override
	public void onCreate() {
		super.onCreate();
		mHandler = new Handler(this);
		mQueue = new LinkedBlockingQueue<>(2);
		mThread = null;
	}

	@Override
	public int onStartCommand(@Nullable Intent intent, int flags, int startId) {
		final String action = intent == null ? null : intent.getAction();
		//noinspection ConstantConditions
		if (action != null && onNewIntent(action, intent.getExtras())) {
			//TODO
		} else {
			stopSelf();
		}
		return START_NOT_STICKY;
	}

	public abstract boolean onNewIntent(@NonNull String action, @NonNull Bundle extras);

	public boolean onStopService() {
		return true;
	}

	@Nullable
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	@SuppressWarnings("unchecked")
	public final boolean handleMessage(Message msg) {
		switch (msg.what) {
			case MSG_STOP_SELF:
				if (onStopService()) {
					stopSelf();
				}
				return true;
			case MSG_PRE_EXECUTE:
				onPreExecute((R) msg.obj);
				return true;
			case MSG_POST_EXECUTE:
				onPostExecute((R) msg.obj, msg.arg1);
				return true;
			case MSG_UPDATE_PROGRESS:
				onProgressUpdate(msg.arg1, msg.arg2, msg.obj);
				return true;
			default:
				return false;
		}
	}

	@MainThread
	public abstract void onPreExecute(R r);

	@WorkerThread
	public abstract int doInBackground(R r);

	@MainThread
	public abstract void onPostExecute(R r, int result);

	@WorkerThread
	protected final void setProgress(int progress, int max, @Nullable Object extra) {
		Message msg = Message.obtain();
		msg.what = MSG_UPDATE_PROGRESS;
		msg.arg1 = progress;
		msg.arg2 = max;
		msg.obj = extra;
		mHandler.sendMessage(msg);
	}

	public abstract void onProgressUpdate(int progress, int max, @Nullable Object extra);

	@MainThread
	public void startBackground(R r) {
		try {
			mQueue.put(r);
			if (mThread == null) {
				mThread = new BackgroundThread();
			}
			mCancelled.set(false);
			mThread.start();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	@MainThread
	public void cancelBackground() {
		mCancelled.set(true);
	}

	public boolean isCancelled() {
		return mCancelled.get();
	}

	private final class BackgroundThread extends Thread {

		@Override
		public void run() {
			R r;
			while (!mCancelled.get() && (r = mQueue.poll()) != null) {
				Message msg = Message.obtain();
				msg.what = MSG_PRE_EXECUTE;
				msg.obj = r;
				mHandler.sendMessage(msg);
				final int result = doInBackground(r);
				msg = Message.obtain();
				msg.what = MSG_POST_EXECUTE;
				msg.arg1 = result;
				msg.obj = r;
				mHandler.sendMessage(msg);
			}
			mHandler.sendEmptyMessage(MSG_STOP_SELF);
		}
	}
}
