package org.nv95.openmanga.feature.read.reader.webtoon;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Message;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;
import android.util.AttributeSet;
import android.util.SparseIntArray;
import android.view.MotionEvent;

import org.nv95.openmanga.feature.read.reader.MangaReader;
import org.nv95.openmanga.feature.read.reader.OnOverScrollListener;
import org.nv95.openmanga.feature.read.reader.OverscrollDetector;
import org.nv95.openmanga.feature.read.reader.PageLoadListener;
import org.nv95.openmanga.feature.read.reader.PageLoader;
import org.nv95.openmanga.feature.read.reader.PageWrapper;
import org.nv95.openmanga.feature.read.reader.recyclerpager.RecyclerViewPager;
import org.nv95.openmanga.items.MangaPage;
import org.nv95.openmanga.utils.DecartUtils;
import org.nv95.openmanga.utils.InternalLinkMovement;
import org.nv95.openmanga.utils.LayoutUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
import java.util.Vector;

/**
 * Created by admin on 14.08.17.
 */

public class WebtoonReader extends DrawableView implements MangaReader, PageLoadListener,
        ChangesListener, Handler.Callback {

    private static final int MSG_IMGSIZE = 1;
    private static final int MSG_PAGE_CHANGED = 2;

    private ImagesPool mPool;
    private final Paint mPaint;
    private final TreeMap<Integer, Integer> mHeights;
    private int mFullHeight = 0;
    private final Handler mHandler;
    private int mCurrentPage;
    private final Vector<RecyclerViewPager.OnPageChangedListener> mPageChangeListeners;
    private volatile int mOffsetX = 0, mOffsetY = 0;
    private final Rect mScrollBounds;
    private int mTopPage, mTopPageOffset;
    private volatile boolean mShowNumbers;
    private final SparseIntArray mProgressMap;
    @Nullable
    private OverscrollDetector mOverscrollDetector;

    public WebtoonReader(Context context) {
        this(context, null, 0);
    }

    public WebtoonReader(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public WebtoonReader(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setHorizontalScrollBarEnabled(false);
        setVerticalScrollBarEnabled(true);
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setColor(Color.DKGRAY);
        mPaint.setSubpixelText(true);
        mPaint.setTextSize(LayoutUtils.DpToPx(getResources(), 18));
        mHeights = new TreeMap<>();
        mProgressMap = new SparseIntArray();
        mHandler = new Handler(this);
        mPageChangeListeners = new Vector<>();
        mCurrentPage = 0;
        mTopPage = mTopPageOffset = 0;
        mScrollBounds = new Rect();
        mOverscrollDetector = null;
    }

    @WorkerThread
    @Override
    protected void onSurfaceDraw(Canvas canvas, int dX, int dY, float zoom) {
        canvas.drawColor(Color.LTGRAY);
        final Rect viewport = canvas.getClipBounds();
        if (mPool != null) {
            mOffsetY -= dY;
            mOffsetX -= dX;
            int offsetY = mOffsetY;
            int page = mCurrentPage;
            //draw previous pages
            if (offsetY > 0) {
                while (offsetY > 0) {
                    PageImage image = mPool.get(page - 1);
                    if (image == null) break;
                    notifyPageHeight(page, image.getHeight());
                    offsetY -= image.getHeight() * zoom;
                    mOffsetY = offsetY;
                    page--;
                    notifyPageChanged(mCurrentPage - 1);
                }
                mPool.prefetch(page-1);
            }
            //draw current page and next
            while (offsetY < canvas.getHeight()) {
                PageImage image = mPool.get(page);
                if (image == null) break;
                notifyPageHeight(page, image.getHeight());
                Rect rect = image.draw(canvas, mPaint, mOffsetX, offsetY, viewport, zoom);
                if (rect.bottom <= 0) {
                    mOffsetY = rect.bottom;
                    notifyPageChanged(mCurrentPage + 1);
                    mPool.prefetch(page + 2);
                }
                page++;
                offsetY = rect.bottom;
            }
            //prefetch next
            //mPool.prefetch(page);

            int progress = mProgressMap.get(mCurrentPage, 0);
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

    private void recomputeWidth() {
        int count = mHeights.size();
        if (count == 0) {
            mFullHeight = getViewportHeight();
            mTopPageOffset = 0;
            return;
        }
        final int total = getItemCount();
        int sum = 0;

        for (int o : mHeights.values()) {
            sum += o;
        }
        int avgHeight = sum / count;
        sum += avgHeight * (total - count);

        mTopPageOffset = 0;
        for (int i = 0; i < mTopPage; i++) {
            Integer o = mHeights.get(i);
            mTopPageOffset += o == null ? avgHeight : o;
        }
        mFullHeight = sum;
        recomputeScrollRange();
    }

    @Override
    protected void onViewportChanged(int oldHeight, int oldWidth, int newHeight, int newWidth) {
        if (mPool != null) {
            mPool.setBaseWidth(newWidth);
        }
        if (oldHeight != 0) {
            mFullHeight = mFullHeight / oldHeight * newHeight;
            for (Integer i : mHeights.keySet()) {
                mHeights.put(i, mHeights.get(i) / oldHeight * newHeight);
            }
        } else {
            mHeights.clear();
        }
    }

    @Override
    protected void onZoomChanged() {
        recomputeScrollRange();
    }

    private void recomputeScrollRange() {
        mScrollBounds.top = 0;
        mScrollBounds.left = 0;
        mScrollBounds.bottom = mFullHeight;
        mScrollBounds.right = getViewportWidth();
        DecartUtils.scaleRect(mScrollBounds, getScaleFactor());
        DecartUtils.translateRect(mScrollBounds, 0, -mTopPageOffset);
        mScrollBounds.bottom -= getViewportHeight();
        mScrollBounds.right -= getViewportWidth();
    }

    @Override
    protected void onIdle() {
        //recomputeWidth();
    }

    @Override
    protected Rect getScrollBounds() {
        return mScrollBounds;
    }

    @Override
    public void applyConfig(boolean vertical, boolean reverse, boolean sticky, boolean showNumbers) {
        mShowNumbers = showNumbers;
    }

    @Override
    public boolean scrollToNext(boolean animate) {
        return smoothScrollBy(0, (int) (getViewportHeight() * 0.9f));
    }

    @Override
    public boolean scrollToPrevious(boolean animate) {
        return smoothScrollBy(0, (int) -(getViewportHeight() * 0.9f));
    }

    @Override
    public int getCurrentPosition() {
        return mCurrentPage;
    }

    @Override
    public void scrollToPosition(int position) {
        int oldPage = mCurrentPage;
        mCurrentPage = position;
        mOffsetY = 0;
        mScrollState.set(mScrollState.get().offsetY(0));
        mTopPage = position;
        recomputeWidth();
        forceRedraw();
        for (RecyclerViewPager.OnPageChangedListener o : mPageChangeListeners) {
            o.OnPageChanged(oldPage, mCurrentPage);
        }
    }

    @Override
    public void setTapNavs(boolean val) {

    }

    @Override
    public void addOnPageChangedListener(RecyclerViewPager.OnPageChangedListener listener) {
        mPageChangeListeners.add(listener);
    }

    @Override
    public void setOnOverScrollListener(OnOverScrollListener listener) {
        mOverscrollDetector = new OverscrollDetector(listener);
        mOverscrollDetector.setDirections(true, false);
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
        recomputeWidth();
        forceRedraw();
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
        mPool.recycle();
        mHeights.clear();
        mProgressMap.clear();
        getLoader().setPages(mangaPages);
        scrollToPosition(0);
    }

    @Override
    public void finish() {
        getLoader().cancelAll();
        mPool.recycle();
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mOverscrollDetector != null) {
            mOverscrollDetector.onTouch(this, event);
        }
        return super.onTouchEvent(event);
    }

    protected Rect computeScrollRange(float scale) {
        Rect range = new Rect(mScrollBounds);
        range.bottom = range.top + (int)(mFullHeight * scale) - getViewportHeight();
        range.right = range.left + (int)(getViewportWidth() * scale) - getViewportWidth();
        return range;
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

    @Override
    public void onLoadingStarted(PageWrapper page, boolean shadow) {

    }

    @Override
    public void onProgressUpdated(PageWrapper page, boolean shadow, int percent) {
        mProgressMap.put(page.position, percent);
        notifyDataSetChanged();
    }

    @Override
    public void onLoadingComplete(PageWrapper page, boolean shadow) {
        mProgressMap.put(page.position, 100);
        notifyDataSetChanged();
    }

    @Override
    public void onLoadingFail(PageWrapper page, boolean shadow) {
        mProgressMap.put(page.position, -1);
        notifyDataSetChanged();
    }

    @Override
    public void onLoadingCancelled(PageWrapper page, boolean shadow) {
        mProgressMap.put(page.position, 0);
        notifyDataSetChanged();
    }

    @WorkerThread
    private void notifyPageHeight(int page, int height) {
        Message msg = new Message();
        msg.what = MSG_IMGSIZE;
        msg.arg1 = page;
        msg.arg2 = height;
        mHandler.sendMessage(msg);
    }

    @WorkerThread
    private void notifyPageChanged(int page) {
        Message msg = new Message();
        msg.what = MSG_PAGE_CHANGED;
        msg.arg1 = page;
        mHandler.sendMessage(msg);
    }

    @Override
    public boolean handleMessage(Message message) {
        switch (message.what) {
            case MSG_IMGSIZE:
                if (!mHeights.containsKey(message.arg1)) {
                    mHeights.put(message.arg1, message.arg2);
                    recomputeWidth();
                }
                return true;
            case MSG_PAGE_CHANGED:
                int oldPage = mCurrentPage;
                mCurrentPage = message.arg1;
                for (RecyclerViewPager.OnPageChangedListener o : mPageChangeListeners) {
                    o.OnPageChanged(oldPage, mCurrentPage);
                }
                return true;
         default:
            return false;
        }
    }
}
