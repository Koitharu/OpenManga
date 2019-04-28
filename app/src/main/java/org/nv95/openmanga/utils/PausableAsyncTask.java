package org.nv95.openmanga.utils;

import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import androidx.annotation.UiThread;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by admin on 21.07.17.
 */

public abstract class PausableAsyncTask<Param, Progress, Result> extends AsyncTask<Param, Progress, Result> {

    private final AtomicBoolean mPaused = new AtomicBoolean(false);
    private final Handler mStateHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message message) {
            switch (message.what) {
                case 1:
                    onResumed();
                    break;
                case -1:
                    onPaused();
                    break;
            }
            return false;
        }
    });

    public void setPaused(boolean value) {
        if (getStatus() == Status.FINISHED) return;
        boolean oldValue = mPaused.getAndSet(value);
        if (value != oldValue) {
            if (value) {
                mStateHandler.sendEmptyMessage(-1);
            } else {
                mStateHandler.sendEmptyMessage(1);
            }
        }
    }

    public void pause() {
        setPaused(true);
    }

    public void resume() {
        setPaused(false);
    }

    public boolean isPaused() {
        return mPaused.get();
    }

    @UiThread
    public void onPaused() {}

    @UiThread
    public void onResumed() {}

    protected boolean waitForResume() {
        while(isPaused()) {
            try {
                if (isCancelled()) {
                    return false;
                }
                Thread.sleep(100);
            } catch (InterruptedException e) {
                return false;
            }
        }
        return !isCancelled();
    }

    public ExStatus getExStatus() {
        Status status = getStatus();
        switch (status) {
            case FINISHED:
                return ExStatus.FINISHED;
            case PENDING:
                return ExStatus.PENDING;
            default:
                return isPaused() ? ExStatus.PAUSED : ExStatus.RUNNING;
        }
    }

    public boolean canCancel() {
        return getStatus() != Status.FINISHED;
    }

    public boolean isActive() {
        return getStatus() == Status.RUNNING && !isPaused();
    }

    public enum ExStatus {
        FINISHED,
        PENDING,
        RUNNING,
        PAUSED
    }
}
