package org.nv95.openmanga.components.reader.webtoon;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.support.annotation.MainThread;
import android.support.annotation.Nullable;
import android.support.annotation.WorkerThread;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Scroller;

/**
 * Created by admin on 14.08.17.
 */

public abstract class DrawableView extends SurfaceView implements SurfaceHolder.Callback, ScaleGestureDetector.OnScaleGestureListener {

    @Nullable
    private DrawThread mThread;
    private final GestureDetector mGestureDetector;
    private final ScaleGestureDetector mScaleDetector;
    private final Scroller mScroller;
    private final Rect mViewport;
    private volatile float mScaleFactor = 1;
    private volatile int mScrollX = 0, mScrollY = 0;

    public DrawableView(Context context) {
        this(context, null, 0);
    }

    public DrawableView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DrawableView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mGestureDetector = new GestureDetector(context, new GestureListener());
        mScaleDetector = new ScaleGestureDetector(context, this);
        mScroller = new Scroller(context);
        mViewport = new Rect(0, 0, 0, 0);
        mThread = null;
        SurfaceHolder holder = getHolder();
        holder.addCallback(this);
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        mThread = new DrawThread(surfaceHolder);
        mThread.start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int format, int width, int height) {
        mViewport.set(0, 0, width, height);
        forceRedraw();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        if (mThread != null) {
            mThread.interrupt();
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // check for tap and cancel fling
        if ((event.getAction() & MotionEvent.ACTION_MASK) == MotionEvent.ACTION_DOWN) {
            if (!mScroller.isFinished()) mScroller.abortAnimation();
        }

        // handle pinch zoom gesture
        // don't check return value since it is always true
        mScaleDetector.onTouchEvent(event);

        // check for scroll gesture
        if (mGestureDetector.onTouchEvent(event)) return true;

        // check for pointer release
        if ((event.getPointerCount() == 1) && ((event.getAction() & MotionEvent.ACTION_MASK) == MotionEvent.ACTION_UP)) {
            int hRange = computeHorizontalScrollRange();
            int vRange = computeVerticalScrollRange();
            int newScrollX = getScrollX();
            if (hRange < getWidth())
                newScrollX = -(getWidth() - hRange) / 2;
            else if (getScrollX() < 0) newScrollX = 0;
            else if (getScrollX() > hRange - getWidth())
                newScrollX = hRange - getWidth();

            int newScrollY = getScrollY();
            if (vRange < getHeight())
                newScrollY = -(getHeight() - vRange) / 2;
            else if (getScrollY() < 0) newScrollY = 0;
            else if (getScrollY() > vRange - getHeight())
                newScrollY = vRange - getHeight();

            if ((newScrollX != getScrollX()) || (newScrollY != getScrollY())) {
                mScroller.startScroll(getScrollX(), getScrollY(), newScrollX - getScrollX(), newScrollY - getScrollY());
                awakenScrollBars(mScroller.getDuration());
            } else {
                onIdle();
            }
        }

        return true;
    }

    @Override
    public boolean onScale(ScaleGestureDetector detector) {
        mScaleFactor *= detector.getScaleFactor();
        if (mScaleFactor < 1f) {
            mScaleFactor = 1f;
            return false;
        }

        int newScrollX = (int) ((getScrollX() + detector.getFocusX()) * detector.getScaleFactor() - detector.getFocusX());
        int newScrollY = (int) ((getScrollY() + detector.getFocusY()) * detector.getScaleFactor() - detector.getFocusY());
        scrollTo(newScrollX, newScrollY);

        return true;
    }

    @Override
    public boolean onScaleBegin(ScaleGestureDetector scaleGestureDetector) {
        return true;
    }

    @Override
    public void onScaleEnd(ScaleGestureDetector scaleGestureDetector) {

    }

    private class GestureListener extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            int hRange = computeHorizontalScrollRange();
            int vRange = computeVerticalScrollRange();
            int fixedScrollX = 0, fixedScrollY = 0;
            int maxScrollX = hRange, maxScrollY = vRange;

            if (maxScrollX < getViewportWidth()) {
                fixedScrollX = -(getViewportWidth() - maxScrollX) / 2;
                maxScrollX = +fixedScrollX;
            }

            if (maxScrollY < getViewportHeight()) {
                fixedScrollY = -(getViewportHeight() - maxScrollY) / 2;
                maxScrollY += fixedScrollY;
            }

            boolean scrollBeyondImage = (fixedScrollX < 0) || (fixedScrollX > maxScrollX) || (fixedScrollY < 0) || (fixedScrollY > maxScrollY);
            if (scrollBeyondImage) return false;
            mScroller.fling(
                    getScrollX(),
                    getScrollY(),
                    -(int) velocityX,
                    -(int) velocityY,
                    0,
                    hRange - mViewport.width(),
                    0,
                    vRange - mViewport.width());
            awakenScrollBars(mScroller.getDuration());
            return true;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            int hRange = computeHorizontalScrollRange();
            int vRange = computeVerticalScrollRange();
            int scrollY = (int) (getScrollY() + distanceY);
            if (scrollY < 0) {
                scrollY = 0;
            }
            else if (scrollY > vRange - getViewportHeight()) {
                scrollY = vRange - getViewportHeight();
            }
            int scrollX = (int) (getScrollX() + distanceX);
            if (scrollX < 0) {
                scrollX = 0;
            }
            else if (scrollX > hRange - getViewportWidth()) {
                scrollX = hRange - getViewportWidth();
            }
            scrollTo(scrollX, scrollY);
            return super.onScroll(e1, e2, distanceX, distanceY);
        }
    }

    @Override
    public void computeScroll() {
        if (mScroller.computeScrollOffset()) {
            int oldX = getScrollX();
            int oldY = getScrollY();
            int x = mScroller.getCurrX();
            int y = mScroller.getCurrY();
            scrollTo(x, y);
            if (oldX != getScrollX() || oldY != getScrollY()) {
                onScrollChanged(getScrollX(), getScrollY(), oldX, oldY);
            }
        }
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        mScrollX = l;
        mScrollY = t;
        super.onScrollChanged(l, t, oldl, oldt);
    }

    public void forceRedraw() {
        if (mThread != null) {
            mThread.force = true;
        }
    }

    private class DrawThread extends Thread {

        private final SurfaceHolder mSurface;

        private int lastOffsetX = -1, lastOffsetY = -1;
        private float lastZoom = 0;
        boolean force;

        DrawThread(SurfaceHolder surfaceHolder) {
            mSurface = surfaceHolder;
        }

        @Override
        public void run() {
            Canvas canvas;
            while (!interrupted()) {
                canvas = null;
                try {
                    // получаем объект Canvas и выполняем отрисовку

                    computeScroll();
                    int offsetX = -mScrollX;
                    int offsetY = -mScrollY;
                    if (!force && lastOffsetX == offsetX && lastOffsetY == offsetY && lastZoom == mScaleFactor) {
                        continue;
                    }
                    force = false;
                    canvas = mSurface.lockCanvas(null);
                    synchronized (mSurface) {
                        long time = System.currentTimeMillis();
                        onSurfaceDraw(canvas, lastOffsetX - offsetX, lastOffsetY - offsetY, mScaleFactor);
                        time = System.currentTimeMillis() - time;
                        Log.d("FPS", 1f / time * 1000f + " fps");

                        lastOffsetX = offsetX;
                        lastOffsetY = offsetY;
                        lastZoom = mScaleFactor;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (canvas != null) {
                        // отрисовка выполнена. выводим результат на экран
                        mSurface.unlockCanvasAndPost(canvas);
                    }
                }
            }
        }
    }

    public float getScaleFactor() {
        return mScaleFactor;
    }

    public int getViewportWidth() {
        return mViewport.width();
    }

    public int getViewportHeight() {
        return mViewport.height();
    }

    @WorkerThread
    protected abstract void onSurfaceDraw(Canvas canvas, int dX, int dY, float zoom);

    @MainThread
    @Override
    protected abstract int computeVerticalScrollRange();

    @MainThread
    @Override
    protected abstract int computeHorizontalScrollRange();

    protected void onIdle(){}
}
