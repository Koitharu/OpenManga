package org.nv95.openmanga.feature.read.reader.webtoon;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import androidx.annotation.Nullable;
import android.widget.Scroller;


/**
 * Created by admin on 15.08.17.
 */

public class AsyncScroller extends Scroller implements Handler.Callback {

    private final OnFlyListener mListener;
    private final Handler mHandler;
    @Nullable
    private WatcherThread mThread = null;

    public AsyncScroller(Context context, OnFlyListener listener) {
        super(context);
        mHandler = new Handler(this);
        mListener = listener;
    }

    @Override
    public void fling(int startX, int startY, int velocityX, int velocityY, int minX, int maxX, int minY, int maxY) {
        if (mThread != null) {
            mThread.interrupt();
        }
        mThread = new WatcherThread(startX, startY);
        super.fling(startX, startY, velocityX, velocityY, minX, maxX, minY, maxY);
        mThread.start();
    }

    @Override
    public void startScroll(int startX, int startY, int dx, int dy) {
        if (mThread != null) {
            mThread.interrupt();
        }
        mThread = new WatcherThread(startX, startY);
        super.startScroll(startX, startY, dx, dy);
        mThread.start();
    }

    @Override
    public void startScroll(int startX, int startY, int dx, int dy, int duration) {
        if (mThread != null) {
            mThread.interrupt();
        }
        mThread = new WatcherThread(startX, startY);
        super.startScroll(startX, startY, dx, dy, duration);
        mThread.start();
    }

    @Override
    public boolean handleMessage(Message message) {
        mListener.onScrolled(message.arg1, message.arg2);
        return false;
    }

    private class WatcherThread extends Thread {

        private int oldX, oldY;

        public WatcherThread(int oldX, int oldY) {
            this.oldX = oldX;
            this.oldY = oldY;
        }

        @Override
        public void run() {
            while (!isInterrupted() && !AsyncScroller.this.isFinished()) {
                if (computeScrollOffset()) {
                    Message msg = new Message();
                    msg.arg1 = getCurrX();
                    msg.arg2 = getCurrY();
                    if (msg.arg1 != oldX || msg.arg2 != oldY) {
                        mHandler.sendMessage(msg);
                        oldX = msg.arg1;
                        oldY = msg.arg2;
                    }
                }
            }
            super.run();
        }
    }

    @Override
    public void abortAnimation() {
        if (mThread != null) {
            mThread.interrupt();
            mThread = null;
        }
        super.abortAnimation();
    }

    public interface OnFlyListener {
        void onScrolled(int currentX, int currentY);
    }
}
