package org.nv95.openmanga.feature.read.reader.webtoon;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import androidx.annotation.MainThread;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.concurrent.atomic.AtomicReference;

/**
 * Created by admin on 14.08.17.
 */

public abstract class DrawableView extends SurfaceView implements SurfaceHolder.Callback, ScaleGestureDetector.OnScaleGestureListener, AsyncScroller.OnFlyListener, ScaleAnimator.ZoomCallback {

    @Nullable
    private DrawThread mThread;
    private final GestureDetector mGestureDetector;
    private final ScaleGestureDetector mScaleDetector;
    private final AsyncScroller mScroller;
    private final Rect mViewport;
    protected final AtomicReference<ScrollState> mScrollState;

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
        mScroller = new AsyncScroller(context, this);
        mViewport = new Rect(0, 0, 0, 0);
        mThread = null;
        mScrollState = new AtomicReference<>(new ScrollState(1f, 0, 0));
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
        int oldHeight = mViewport.height();
        int oldWidth = mViewport.width();
        mViewport.set(0, 0, width, height);
        onViewportChanged(oldHeight, oldWidth, height, width);
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
        mGestureDetector.onTouchEvent(event);
        mScaleDetector.onTouchEvent(event);
        return true;
    }

    @Override
    public boolean onScale(ScaleGestureDetector detector) {
        ScrollState state = mScrollState.get();

        float scaleFactor = state.scale;
        scaleFactor *= detector.getScaleFactor();
        if (scaleFactor < 1f) {
            if (state.scale == 1f) {
                return false;
            } else {
                scaleFactor = 1f;
            }
        } else if (scaleFactor > 2f) {
            if (state.scale == 2f) {
                return false;
            } else {
                scaleFactor = 2f;
            }
        }


        float scrollFactor = scaleFactor - state.scale;
        int newScrollX = (int) (state.scrollX +  detector.getFocusX() * scrollFactor);
        int newScrollY = (int) (state.scrollY + detector.getFocusY() * scrollFactor);

        mScrollState.set(new ScrollState(scaleFactor, newScrollX, newScrollY));
        onZoomChanged();
        return true;
    }

    @Override
    public boolean onScaleBegin(ScaleGestureDetector scaleGestureDetector) {
        return true;
    }

    @Override
    public void onScaleEnd(ScaleGestureDetector scaleGestureDetector) {

    }

    @Override
    public void onZoomAnimated(float scale, int scrollX, int scrollY) {
        Rect bounds = computeScrollRange(scale);

        if (scrollX < bounds.left) scrollX = bounds.left;
        else if (scrollX > bounds.right) scrollX = bounds.right;

        if (scrollY < bounds.top) scrollY = bounds.top;
        else if (scrollY > bounds.bottom) scrollY = bounds.bottom;

        mScrollState.set(new ScrollState(scale, scrollX, scrollY));
    }

    @Override
    public void onZoomAnimationFinished() {
        onZoomChanged();
    }

    private class GestureListener extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            Rect bounds = getScrollBounds();
            ScrollState state = mScrollState.get();
            mScroller.fling(
                    state.scrollX,
                    state.scrollY,
                    -(int) velocityX,
                    -(int) velocityY,
                    bounds.left,
                    bounds.right,
                    bounds.top,
                    bounds.bottom);
            awakenScrollBars(mScroller.getDuration());
            return true;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            if (e2.getPointerCount() != 1) return false;

            Rect bounds = getScrollBounds();
            ScrollState state = mScrollState.get();
            int scrollY = (int) (state.scrollY + distanceY);
            int scrollX = (int) (state.scrollX + distanceX);

            if (scrollX < bounds.left) scrollX = bounds.left;
            else if (scrollX > bounds.right) scrollX = bounds.right;

            if (scrollY < bounds.top) scrollY = bounds.top;
            else if (scrollY > bounds.bottom) scrollY = bounds.bottom;

            mScrollState.set(state.offset(scrollX, scrollY));

            return super.onScroll(e1, e2, distanceX, distanceY);
        }

        @Override
        public boolean onDown(MotionEvent e) {
            if (e.getPointerCount() == 1) {
                mScroller.abortAnimation();
            }
            return super.onDown(e);
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            ScaleAnimator animator = new ScaleAnimator(DrawableView.this);
            ScrollState state = mScrollState.get();
            if (state.scale == 1f) {
                animator.animate(
                        state.scale,
                        state.scrollX,
                        state.scrollY,
                        1.6f,
                        (int) (state.scrollX + e.getX() * 1.6f),
                        (int) (state.scrollY + e.getY() * 1.6f)
                );
            } else {
                animator.animate(
                        state.scale,
                        state.scrollX,
                        state.scrollY,
                        1f,
                        (int) (state.scrollX - e.getX() * 1.6f),
                        (int) (state.scrollY - e.getY() * 1.6f)
                );
            }
            return super.onDoubleTap(e);
        }
    }


    @Override
    public void onScrolled(int currentX, int currentY) {
        mScrollState.set(mScrollState.get().offset(currentX, currentY));
    }

    public void forceRedraw() {
        if (mThread != null) {
            mThread.force = true;
        }
    }

    @Override
    public boolean canScrollHorizontally(int direction) {
        Rect bounds = getScrollBounds();
        ScrollState state = mScrollState.get();
        if (direction < 0) {
            return state.scrollX > bounds.right;
        } else if (direction > 0) {
            return state.scrollX < bounds.left;
        } else return super.canScrollHorizontally(direction);
    }

    @Override
    public boolean canScrollVertically(int direction) {
        Rect bounds = getScrollBounds();
        ScrollState state = mScrollState.get();
        if (direction < 0) {
            return state.scrollY > bounds.top;
        } else if (direction > 0) {
            return state.scrollY < bounds.bottom;
        } else return super.canScrollVertically(direction);
    }

    private class DrawThread extends Thread {

        private final SurfaceHolder mSurface;

        private int lastOffsetX = 0, lastOffsetY = 0;
        private float lastZoom = 0;
        boolean force;

        DrawThread(SurfaceHolder surface) {
            mSurface = surface;
        }

        @Override
        public void run() {
            Canvas canvas;
            while (!interrupted()) {
                canvas = null;
                try {
                    // получаем объект Canvas и выполняем отрисовку
                    ScrollState state = mScrollState.get();
                    int offsetX = -state.scrollX;
                    int offsetY = -state.scrollY;
                    if (!force && lastOffsetX == offsetX && lastOffsetY == offsetY && lastZoom == state.scale) {
                        continue;
                    }
                    force = false;
                    canvas = mSurface.lockCanvas(null);
                    synchronized (mSurface) {
                        //long time = System.currentTimeMillis();
                        onSurfaceDraw(canvas, lastOffsetX - offsetX, lastOffsetY - offsetY, state.scale);
                        /*time = System.currentTimeMillis() - time;
                        Log.d("FPS", 1f / time * 1000f + " fps");*/

                        lastOffsetX = offsetX;
                        lastOffsetY = offsetY;
                        lastZoom = state.scale;
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

    public boolean smoothScrollBy(int dX, int dY) {
        mScroller.forceFinished(true);
        Rect bounds = getScrollBounds();
        ScrollState state = mScrollState.get();
        if (dY < 0) {
            dY = Math.max(dY, bounds.top - state.scrollY);
        } else if (dY > 0) {
            dY = Math.min(dY, bounds.bottom - state.scrollY);
        }
        if (dX < 0) {
            dX = Math.max(dX, bounds.left - state.scrollY);
        } else if (dX > 0) {
            dX = Math.min(dX, bounds.right - state.scrollY);
        }
        if (dX == 0 && dY == 0) {
            return false;
        }
        mScroller.startScroll(
                state.scrollX,
                state.scrollY,
                dX,
                dY,
                800
        );
        return true;
    }

    public float getScaleFactor() {
        return mScrollState.get().scale;
    }

    public int getViewportWidth() {
        return mViewport.width();
    }

    public int getViewportHeight() {
        return mViewport.height();
    }

    @WorkerThread
    protected abstract void onSurfaceDraw(Canvas canvas, int dX, int dY, float zoom);

    protected void onIdle(){}

    protected void onViewportChanged(int oldHeight, int oldWidth, int newHeight, int newWidth){};

    protected void onZoomChanged(){};

    @MainThread
    protected abstract Rect getScrollBounds();

    protected abstract Rect computeScrollRange(float scale);
}
