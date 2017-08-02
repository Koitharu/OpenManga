package org.nv95.openmanga.components.reader.webtoon;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.v4.view.ScaleGestureDetectorCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import org.nv95.openmanga.components.reader.MangaReader;
import org.nv95.openmanga.components.reader.OnOverScrollListener;
import org.nv95.openmanga.components.reader.PageLoadListener;
import org.nv95.openmanga.components.reader.PageLoader;
import org.nv95.openmanga.components.reader.PageWrapper;
import org.nv95.openmanga.components.reader.recyclerpager.RecyclerViewPager;
import org.nv95.openmanga.items.MangaPage;
import org.nv95.openmanga.utils.InternalLinkMovement;

import java.util.List;

/**
 * Created by admin on 01.08.17.
 */

public class WebtoonReader extends SurfaceView implements MangaReader, SurfaceHolder.Callback,
        PageLoadListener, ScrollController.Callback {

    private ImagesPool mPool;
    private final GestureDetector mGestureDetector;
    private final ScaleGestureDetector mScaleDetector;
    private DrawThread mDrawThread;
    private int mCurrentPage;
    private final ScrollController mScrollCtrl;

    public WebtoonReader(Context context) {
        this(context, null, 0);
    }

    public WebtoonReader(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public WebtoonReader(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mGestureDetector = new GestureDetector(context, new GestureListener());
        mScaleDetector = new ScaleGestureDetector(context, new ScaleListener());
        ScaleGestureDetectorCompat.setQuickScaleEnabled(mScaleDetector, false);
        getHolder().addCallback(this);
        mScrollCtrl = new ScrollController(this);
    }

    @Override
    public void applyConfig(boolean vertical, boolean reverse, boolean sticky) {

    }

    @Override
    public boolean scrollToNext(boolean animate) {
        int h = mScrollCtrl.viewportHeight();
        if (animate && h > 0) {
            mScrollCtrl.smoothScrollBy(0, -h * 0.9f);
            return true;
        } else if (mCurrentPage == getItemCount() - 1) {
            return false;
        } else {
            scrollToPosition(mCurrentPage + 1);
            return true;
        }
    }

    @Override
    public boolean scrollToPrevious(boolean animate) {
        int h = mScrollCtrl.viewportHeight();
        if (animate && h > 0) {
            mScrollCtrl.smoothScrollBy(0, h * 0.9f);
            return true;
        } else if (mCurrentPage == 0) {
            return false;
        } else {
            scrollToPosition(mCurrentPage - 1);
            return true;
        }
    }

    @Override
    public int getCurrentPosition() {
        return mCurrentPage;
    }

    @Override
    public void scrollToPosition(int position) {
        mCurrentPage = position;
        mScrollCtrl.setOffsetY(0);
        notifyDataSetChanged();
    }

    @Override
    public void setTapNavs(boolean val) {

    }

    @Override
    public void addOnPageChangedListener(RecyclerViewPager.OnPageChangedListener listener) {

    }

    @Override
    public void setOnOverScrollListener(OnOverScrollListener listener) {

    }

    @Override
    public boolean isReversed() {
        return false;
    }

    @Override
    public int getItemCount() {
        return getLoader().getWrappersList().size();
    }

    @Override
    public void initAdapter(Context context, InternalLinkMovement.OnLinkClickListener linkListener) {
        mPool = new ImagesPool(context);
        mPool.getLoader().addListener(this);
    }

    @Override
    public PageLoader getLoader() {
        return mPool.getLoader();
    }

    @Override
    public void notifyDataSetChanged() {
        /*try {
            mDrawThread.notify();
        } catch (Exception ignored) {

        }*/
        mDrawThread.mChanged = true;
    }

    @Override
    public PageWrapper getItem(int position) {
        return mPool.getLoader().getWrappersList().get(position);
    }

    @Override
    public void setScaleMode(int scaleMode) {

    }

    @Override
    public void reload(int position) {
        notifyDataSetChanged();
    }

    @Override
    public void setPages(List<MangaPage> mangaPages) {
        getLoader().setPages(mangaPages);
    }

    @Override
    public void finish() {
        getLoader().cancelAll();
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mScaleDetector.onTouchEvent(event);
        mGestureDetector.onTouchEvent(event);
        return true;
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        //mScrollCtrl.setViewportWidth(surfaceHolder.getSurfaceFrame().width());
        mDrawThread = new DrawThread(getHolder());
        mDrawThread.setRunning(true);
        mDrawThread.start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int format, int width, int height) {
        mScrollCtrl.setViewportWidth(width);
        mScrollCtrl.setViewportHeight(height);
        notifyDataSetChanged();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        // завершаем работу потока
        mDrawThread.setRunning(false);
        notifyDataSetChanged();
        try {
            mDrawThread.join();
        } catch (InterruptedException ignored) {
        }
    }

    @Override
    public void onLoadingStarted(PageWrapper page, boolean shadow) {

    }

    @Override
    public void onProgressUpdated(PageWrapper page, boolean shadow, int percent) {

    }

    @Override
    public void onLoadingComplete(PageWrapper page, boolean shadow) {
        notifyDataSetChanged();
    }

    @Override
    public void onLoadingFail(PageWrapper page, boolean shadow) {

    }

    @Override
    public void onLoadingCancelled(PageWrapper page, boolean shadow) {

    }

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {

        private float mInitialScale;
        private float mX, mY;

        @Override
        public boolean onScaleBegin(ScaleGestureDetector detector) {
            mInitialScale = mScrollCtrl.getScale();
            return super.onScaleBegin(detector);
        }

        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            mScrollCtrl.setZoom(
                    Math.max(1, mInitialScale * detector.getScaleFactor()),
                    detector.getFocusX(),
                    detector.getFocusY()
            );
            notifyDataSetChanged();
            return super.onScale(detector);
        }
    }

    private class GestureListener extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            if (e2.getPointerCount() > 1) return false;
            mScrollCtrl.scrollBy(-distanceX, -distanceY);
            return true;
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            if (mScrollCtrl.getScale() > 1) {
                mScrollCtrl.resetZoom(true);
            } else {
                mScrollCtrl.zoomTo(1.8f, e.getX(), e.getY());
            }
            return super.onDoubleTap(e);
        }
    }

    private class DrawThread extends Thread {

        private final SurfaceHolder mHolder;
        private final Paint mPaint;
        volatile private boolean mIsRunning;
        volatile private boolean mChanged;

        DrawThread(SurfaceHolder surfaceHolder) {
            mIsRunning = false;
            mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            mHolder = surfaceHolder;
            mChanged = true;
        }

        void setRunning(boolean run) {
            mIsRunning = run;
        }

        @Override
        public void run() {
            while (mIsRunning) {
                Canvas canvas = null;
                while (!mChanged) {
                    try {
                        sleep(10);
                    } catch (InterruptedException e) {
                        break;
                    }
                }
                mChanged = false;
                try {
                    // получаем объект Canvas и выполняем отрисовку
                    canvas = mHolder.lockCanvas(null);
                    if (canvas == null) {
                        continue;
                    }
                    synchronized (mHolder) {
                        canvas.drawColor(Color.LTGRAY);
                        if (mPool != null) {
                            int offset = (int) mScrollCtrl.offsetY();
                            int page = mCurrentPage;
                            //draw previous pages
                            while (offset > 0) {
                                PageImage image = mPool.get(page - 1);
                                if (image == null) break;
                                image.scale(canvas.getWidth() / (float) image.getWidth());
                                offset -= image.getHeight();
                                Rect rect = image.draw(canvas, mPaint, (int) mScrollCtrl.offsetX(), offset);
                                page--;
                                Log.d("WTR", "Draw page: " + page);
                                if (!mScrollCtrl.isFlying()) {
                                    mCurrentPage--;
                                    mScrollCtrl.setOffsetY(rect.top);
                                }
                            }
                            //draw current page and next
                            while (offset < canvas.getHeight() && mIsRunning) {
                                PageImage image = mPool.get(page);
                                if (image == null) break;
                                float scale = canvas.getWidth() / (float)image.getWidth();
                                scale += mScrollCtrl.getScale() - 1;
                                image.scale(scale);
                                Rect rect = image.draw(canvas, mPaint, (int) mScrollCtrl.offsetX(), offset);
                                Log.d("WTR", "Draw page: " + page);
                                if (rect.bottom < 0 && !mScrollCtrl.isFlying()) {
                                    mCurrentPage++;
                                    mScrollCtrl.setOffsetY(rect.bottom);
                                }
                                page++;
                                offset = rect.bottom;
                            }
                            //prefetch next
                            mPool.get(page);
                        }
                    }
                } finally {
                    if (canvas != null) {
                        // отрисовка выполнена. выводим результат на экран
                        mHolder.unlockCanvasAndPost(canvas);
                    }
                }
            }
        }
    }
}
