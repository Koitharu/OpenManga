package org.nv95.openmanga.components.reader.webtoon;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.WorkerThread;
import android.util.AttributeSet;

import org.nv95.openmanga.components.reader.MangaReader;
import org.nv95.openmanga.components.reader.OnOverScrollListener;
import org.nv95.openmanga.components.reader.PageLoadListener;
import org.nv95.openmanga.components.reader.PageLoader;
import org.nv95.openmanga.components.reader.PageWrapper;
import org.nv95.openmanga.components.reader.recyclerpager.RecyclerViewPager;
import org.nv95.openmanga.items.MangaPage;
import org.nv95.openmanga.utils.InternalLinkMovement;

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
        mHeights = new TreeMap<>();
        mHandler = new Handler(this);
        mPageChangeListeners = new Vector<>();
        mCurrentPage = 0;
        mScrollBounds = new Rect();
    }

    @WorkerThread
    @Override
    protected void onSurfaceDraw(Canvas canvas, int dX, int dY, float zoom) {
        canvas.drawColor(Color.DKGRAY);
        final Rect viewport = canvas.getClipBounds();
        if (mPool != null) {
            mOffsetY -= dY;
            mOffsetX -= dX;
            int offsetY = mOffsetY;
            int page = mCurrentPage;
            //draw previous pages
            while (offsetY > 0) {
                PageImage image = mPool.get(page - 1);
                if (image == null) break;
                if (!image.isPreScaled()) {
                    image.preScale(viewport.width() / (float) image.getOriginalWidth());
                    notifyPageHeight(page, image.getPreScaledHeight());
                }
                offsetY -= image.getPreScaledHeight() * zoom;
                mOffsetY = offsetY;
                page--;
                notifyPageChanged(mCurrentPage - 1);
            }
            //draw current page and next
            while (offsetY < canvas.getHeight()) {
                PageImage image = mPool.get(page);
                if (image == null) break;
                if (!image.isPreScaled()) {
                    image.preScale(viewport.width() / (float) image.getOriginalWidth());
                    notifyPageHeight(page, image.getPreScaledHeight());
                }
                Rect rect = image.draw(canvas, mPaint, mOffsetX, offsetY, viewport, zoom);
                if (rect.bottom <= 0) {
                    mOffsetY = rect.bottom;
                    notifyPageChanged(mCurrentPage + 1);
                }
                page++;
                offsetY = rect.bottom;
            }
            //prefetch next
            mPool.prefetch(page);

            /*int progress = getProgress(mCurrentPage);
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
            canvas.drawText(text, 5, canvas.getHeight() - 10, mPaint);*/
        }
    }

    private void recomputeWidth() {
        int count = mHeights.size();
        if (count == 0) {
            mFullHeight = 0;
            return;
        }
        int sum = 0;
        for (int o : mHeights.values()) {
            sum += o;
        }
        if (count < getItemCount()) {
            sum += sum / count;
        }
        /*int avgHeight = sum / count;
        for (int i = getItemCount();i>=count;i--) {
            sum += avgHeight;
        }*/
        mFullHeight = sum;
        recomputeScrollRange();
    }

    @Override
    protected void onViewportChanged(int oldHeight, int oldWidth, int newHeight, int newWidth) {
        if (mPool != null) {
            mPool.resetPreScale();
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
        mScrollBounds.bottom = mScrollBounds.top + (int)(mFullHeight * getScaleFactor()) - getViewportHeight();
        mScrollBounds.right = mScrollBounds.left + (int)(getViewportWidth() * getScaleFactor()) - getViewportWidth();
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
        mOffsetY = 0;
        forceRedraw();
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
        getLoader().setPages(mangaPages);
    }

    @Override
    public void finish() {
        getLoader().cancelAll();
        mPool.recycle();
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
