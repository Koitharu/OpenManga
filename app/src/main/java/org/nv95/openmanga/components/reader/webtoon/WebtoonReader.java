package org.nv95.openmanga.components.reader.webtoon;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.view.ScaleGestureDetectorCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import org.nv95.openmanga.R;
import org.nv95.openmanga.components.reader.MangaReader;
import org.nv95.openmanga.components.reader.OnOverScrollListener;
import org.nv95.openmanga.components.reader.PageLoadListener;
import org.nv95.openmanga.components.reader.PageLoader;
import org.nv95.openmanga.components.reader.PageWrapper;
import org.nv95.openmanga.components.reader.recyclerpager.RecyclerViewPager;
import org.nv95.openmanga.items.MangaPage;
import org.nv95.openmanga.utils.FileLogger;
import org.nv95.openmanga.utils.InternalLinkMovement;
import org.nv95.openmanga.utils.LayoutUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by admin on 01.08.17.
 */

public class WebtoonReader extends SurfaceView implements MangaReader, SurfaceHolder.Callback,
        PageLoadListener, Handler.Callback, ChangesListener {

    private static final int MSG_PAGE_CHANGED = 1;
    private static final int MSG_OVERSCROLL_START = 2;
    private static final int MSG_OVERSCROLL_END = 3;

    private ImagesPool mPool;
    private final GestureDetector mGestureDetector;
    private final ScaleGestureDetector mScaleDetector;
    private DrawThread mDrawThread;
    private int mCurrentPage;
    private boolean mTapNavs;
    private volatile boolean mShowNumbers;
    private final ScrollController mScrollCtrl;
    private final Vector<RecyclerViewPager.OnPageChangedListener> mPageChangeListeners;
    @Nullable
    private OnOverScrollListener mOverscrollListener;
    private final Handler handler = new Handler(this);

    public WebtoonReader(Context context) {
        this(context, null, 0);
    }

    public WebtoonReader(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public WebtoonReader(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mPageChangeListeners = new Vector<>();
        mGestureDetector = new GestureDetector(context, new GestureListener());
        mScaleDetector = new ScaleGestureDetector(context, new ScaleListener());
        ScaleGestureDetectorCompat.setQuickScaleEnabled(mScaleDetector, false);
        getHolder().addCallback(this);
        mScrollCtrl = new ScrollController(this);
    }

    @Override
    public void applyConfig(boolean vertical, boolean reverse, boolean sticky, boolean showNumbers) {
        mShowNumbers = showNumbers;
        notifyDataSetChanged();
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
        int oldPage = mCurrentPage;
        mCurrentPage = position;
        mScrollCtrl.setOffsetY(0);
        for (RecyclerViewPager.OnPageChangedListener o : mPageChangeListeners) {
            o.OnPageChanged(oldPage, mCurrentPage);
        }
        notifyDataSetChanged();
    }

    @Override
    public void setTapNavs(boolean val) {
        mTapNavs = val;
    }

    @Override
    public void addOnPageChangedListener(RecyclerViewPager.OnPageChangedListener listener) {
        mPageChangeListeners.add(listener);
    }

    @Override
    public void setOnOverScrollListener(OnOverScrollListener listener) {
        mOverscrollListener = listener;
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
        mPool = new ImagesPool(context, this);
        mPool.getLoader().addListener(this);
    }

    @Override
    public PageLoader getLoader() {
        return mPool.getLoader();
    }

    @Override
    public void notifyDataSetChanged() {
        if (mDrawThread != null) {
            mDrawThread.mChanged = true;
        }
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
        mPool.getLoader().requestPage(position);
        notifyDataSetChanged();
    }

    @Override
    public void setPages(List<MangaPage> mangaPages) {
        getLoader().setPages(mangaPages);
    }

    @Override
    public void finish() {
        getLoader().cancelAll();
        mPool.recycle();
    }

    @Override
    public List<MangaPage> getPages() {
        List<PageWrapper> wrappers = mPool.getLoader().getWrappersList();
        ArrayList<MangaPage> pages = new ArrayList<>(wrappers.size());
        for (PageWrapper o : wrappers) {
            pages.add(o.page);
        }
        return pages;
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
        mDrawThread.setProgress(page.position, percent);
    }

    @Override
    public void onLoadingComplete(PageWrapper page, boolean shadow) {
        mDrawThread.setProgress(page.position, 100);
        notifyDataSetChanged();
    }

    @Override
    public void onLoadingFail(PageWrapper page, boolean shadow) {
        final int pos = page.position;
        mDrawThread.setProgress(pos, -1);
        if (!shadow) {
            Snackbar.make(this, FileLogger.getInstance().getFailMessage(getContext(), page.getError()), Snackbar.LENGTH_LONG)
                    .setAction(R.string.retry, new OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            reload(pos);
                        }
                    }).show();
        }
    }

    @Override
    public void onLoadingCancelled(PageWrapper page, boolean shadow) {

    }

    @Override
    public boolean handleMessage(Message message) {
        switch (message.what) {
            case MSG_PAGE_CHANGED:
                int oldPage = mCurrentPage;
                mCurrentPage = message.arg1;
                mScrollCtrl.setOffsetY(message.arg2);
                for (RecyclerViewPager.OnPageChangedListener o : mPageChangeListeners) {
                    o.OnPageChanged(oldPage, mCurrentPage);
                }
                return true;
            case MSG_OVERSCROLL_END:
                float factor = message.arg1;
                if (mOverscrollListener != null) {
                    mOverscrollListener.onOverScrollFlying(OnOverScrollListener.BOTTOM, factor);
                }
                return true;
            default:
                return false;
        }
    }

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {

        private float mInitialScale;

        @Override
        public boolean onScaleBegin(ScaleGestureDetector detector) {
            mInitialScale = mScrollCtrl.getScale();
            mScrollCtrl.cancelAnimation();
            return super.onScaleBegin(detector);
        }

        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            float scale = Math.max(1, mInitialScale * detector.getScaleFactor());
            float scaleDelta = scale - mScrollCtrl.getScale() * 0.5f;
            mScrollCtrl.setScaleAndOffset(
                    scale,
                    -detector.getFocusX() * scaleDelta,
                    -detector.getFocusY() * scaleDelta
            );
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

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            if (mTapNavs) {
                if (e.getY() > mScrollCtrl.viewportHeight() * 0.7f) {
                    return scrollToNext(true);
                } else if (e.getY() < mScrollCtrl.viewportHeight() * 0.3f) {
                    return scrollToPrevious(true);
                }
            }
            return super.onSingleTapUp(e);
        }
    }

    private class DrawThread extends Thread {

        private final SurfaceHolder mHolder;
        private final Paint mPaint;
        volatile private boolean mIsRunning;
        volatile private boolean mChanged;
        private ScrollController.ZoomState mState;
        private final ConcurrentHashMap<Integer,Integer> mProgressMap;

        DrawThread(SurfaceHolder surfaceHolder) {
            super("SurfaceDrawer");
            mState = null;
            mIsRunning = false;
            mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            mHolder = surfaceHolder;
            mChanged = true;
            mPaint.setColor(Color.DKGRAY);
            mPaint.setSubpixelText(true);
            mProgressMap = new ConcurrentHashMap<>(getItemCount());
            mPaint.setTextSize(LayoutUtils.DpToPx(getResources(), 16));
        }

        void setRunning(boolean run) {
            mIsRunning = run;
        }

        void setProgress(int page, int percent) {
            mProgressMap.put(page, percent);
            if (page == mCurrentPage) {
                mChanged = true;
            }
        }

        private int getProgress(int page) {
            Integer p = mProgressMap.get(page);
            return p == null ? 0 : p;
        }

        @Override
        public void run() {
            mState = mScrollCtrl.getCurrentState();
            while (mIsRunning) {
                Canvas canvas = null;
                while (!mChanged) {
                    ScrollController.ZoomState newState = mScrollCtrl.getCurrentState();
                    if (!newState.equals(mState)) {
                        mState = newState;
                        break;
                    }
                    try {
                        sleep(5);
                    } catch (InterruptedException e) {
                        break;
                    }
                }
                if (mChanged) {
                    mState = mScrollCtrl.getCurrentState();
                    mChanged = false;
                }
                try {
                    // получаем объект Canvas и выполняем отрисовку
                    canvas = mHolder.lockCanvas(null);
                    if (canvas == null) {
                        continue;
                    }
                    synchronized (mHolder) {
                        canvas.drawColor(Color.LTGRAY);
                        if (mPool != null) {
                            int offset = (int) mState.offsetY;
                            int page = mCurrentPage;
                            //draw previous pages
                            while (offset > 0) {
                                PageImage image = mPool.get(page - 1);
                                if (image == null) break;
                                float scale = canvas.getWidth() / (float)image.getWidth();
                                scale *= mState.scale;
                                image.scale(scale);
                                offset -= image.getHeight();
                                Rect rect = image.draw(canvas, mPaint, (int) mState.offsetX, offset);
                                page--;
                                Log.d("WTR", "Draw page: " + page);
                                if (!mScrollCtrl.isFlying()) {
                                    mState = mState.offsetY(rect.top);
                                    notifyPageChanged(mCurrentPage - 1, rect.top);
                                }
                            }
                            //draw current page and next
                            while (offset < canvas.getHeight() && mIsRunning) {
                                PageImage image = mPool.get(page);
                                if (image == null) break;
                                float scale = canvas.getWidth() / (float)image.getWidth();
                                scale *= mState.scale;
                                image.scale(scale);
                                Rect rect = image.draw(canvas, mPaint, (int) mState.offsetX, offset);
                                Log.d("WTR", "Draw page: " + page);
                                if (rect.bottom < 0) {
                                    //if last page
                                    if (page == getItemCount() - 1)  {
                                        notifyOverscrollEnd(canvas.getWidth() - rect.bottom);
                                        break;
                                    }
                                    if (!mScrollCtrl.isFlying()) {
                                        mState = mState.offsetY(rect.bottom);
                                        notifyPageChanged(mCurrentPage + 1, rect.bottom);
                                    }
                                }
                                page++;
                                offset = rect.bottom;
                            }
                            //prefetch next
                            mPool.get(page);

                            int progress = getProgress(mCurrentPage);
                            String text = "";
                            if (mShowNumbers) {
                                text += String.valueOf(mCurrentPage + 1);
                                if (progress == -1) {
                                    text += " - ERROR";
                                } else if (progress != 0 && progress != 100) {
                                    text += " - " + progress + "%";
                                }
                            } else {
                                if (progress == -1) {
                                    text += "ERROR";
                                } else if (progress != 0 && progress != 100) {
                                    text += progress + "%";
                                }
                            }
                            canvas.drawText(text, 5, canvas.getHeight() - 10, mPaint);
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

        private void notifyPageChanged(int page, int offsetY) {
            Message msg = new Message();
            msg.what = MSG_PAGE_CHANGED;
            msg.arg1 = page;
            msg.arg2 = offsetY;
            handler.sendMessage(msg);
        }

        private void notifyOverscrollEnd(int size) {
            Message msg = new Message();
            msg.what = MSG_OVERSCROLL_END;
            msg.arg1 = size;
            handler.sendMessage(msg);
        }
    }
}
