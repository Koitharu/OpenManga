package org.nv95.openmanga.components.reader.webtoon;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
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

public class WebtoonReader extends SurfaceView implements MangaReader, SurfaceHolder.Callback, PageLoadListener {

    private ImagesPool mPool;
    private final GestureDetector mGestureDetector;
    private final ScaleGestureDetector mScaleDetector;
    private DrawThread mDrawThread;
    private int mCurrentPage;
    private int mScrollOffset;
    private int mHorizontalOffset;
    private float mScale;

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
        getHolder().addCallback(this);
        mHorizontalOffset = 0;
        mScale = 1;
        mScrollOffset = 0;
    }

    @Override
    public void applyConfig(boolean vertical, boolean reverse, boolean sticky) {

    }

    @Override
    public boolean scrollToNext(boolean animate) {
        return false;
    }

    @Override
    public boolean scrollToPrevious(boolean animate) {
        return false;
    }

    @Override
    public int getCurrentPosition() {
        return mCurrentPage;
    }

    @Override
    public void scrollToPosition(int position) {
        mCurrentPage = position;
        mScrollOffset = 0;
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
        mDrawThread.mOffsetY = -1;
    }

    @Override
    public PageWrapper getItem(int position) {
        return null;
    }

    @Override
    public void setScaleMode(int scaleMode) {

    }

    @Override
    public void reload(int position) {

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
        mDrawThread = new DrawThread(getHolder());
        mDrawThread.setRunning(true);
        mDrawThread.start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int format, int width, int height) {
        notifyDataSetChanged();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        // завершаем работу потока
        mDrawThread.setRunning(false);
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

        @Override
        public boolean onScaleBegin(ScaleGestureDetector detector) {
            mInitialScale = mScale;
            return super.onScaleBegin(detector);
        }

        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            mScale = Math.max(1, mInitialScale * detector.getScaleFactor());
            notifyDataSetChanged();
            return super.onScale(detector);
        }
    }

    private class GestureListener extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            mScrollOffset -= distanceY;
            mHorizontalOffset -= distanceX;
            if (mHorizontalOffset < 0) {
                mHorizontalOffset = 0;
            }
            Log.d("WTR", "Scroll offset: " + mScrollOffset);
            return super.onScroll(e1, e2, distanceX, distanceY);
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            if (mScale > 1) {
                mScale = 1;
            } else {
                mScale = 1.8f;
            }
            notifyDataSetChanged();
            return super.onDoubleTap(e);
        }
    }

    private class DrawThread extends Thread {

        private final SurfaceHolder mHolder;
        private final Paint mPaint;
        volatile private boolean mIsRunning;
        private int mOffsetY = -1;
        private int mOffsetX = -1;

        DrawThread(SurfaceHolder surfaceHolder) {
            mIsRunning = false;
            mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            mHolder = surfaceHolder;
        }

        void setRunning(boolean run) {
            mIsRunning = run;
        }

        @Override
        public void run() {
            while (mIsRunning) {
                Canvas canvas = null;
                try {
                    while (mOffsetY == mScrollOffset && mHorizontalOffset == mOffsetX) {
                        try {
                            if (!mIsRunning) return;
                            sleep(100);
                        } catch (InterruptedException e) {
                            break;
                        }
                    }
                    mOffsetY = mScrollOffset;
                    mOffsetX = mHorizontalOffset;
                    // получаем объект Canvas и выполняем отрисовку
                    canvas = mHolder.lockCanvas(null);
                    if (canvas == null) {
                        continue;
                    }
                    synchronized (mHolder) {
                        canvas.drawColor(Color.LTGRAY);
                        if (mPool != null) {
                            int offset = mScrollOffset;
                            int page = mCurrentPage;
                            while (offset < canvas.getHeight() && mIsRunning) {
                                PageImage image = mPool.get(page);
                                if (image == null) break;
                                float scale = canvas.getWidth() / (float)image.getWidth();
                                scale += mScale - 1;
                                image.scale(scale);
                                Rect rect = image.draw(canvas, mPaint, mOffsetX, offset);
                                /*if (rect.top > 0) {
                                    image = mPool.get(page);
                                    if (image != null) {
                                        image.scale(canvas.getWidth() / (float) image.getWidth());
                                        offset -= image.getHeight();
                                        rect = image.draw(canvas, mPaint, 0, offset);
                                        mCurrentPage--;
                                        mScrollOffset = rect.top;
                                        continue;
                                    }
                                }*/
                                if (rect.bottom < 0) {
                                    mCurrentPage++;
                                    mScrollOffset = rect.bottom;
                                }
                                page++;
                                offset = rect.bottom;
                            }
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
