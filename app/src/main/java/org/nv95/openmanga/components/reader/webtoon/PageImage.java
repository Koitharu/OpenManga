package org.nv95.openmanga.components.reader.webtoon;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.annotation.NonNull;

import org.nv95.openmanga.utils.DecartUtils;

/**
 * Created by admin on 01.08.17.
 */

public class PageImage {

    @NonNull
    private final Bitmap mBitmap;
    private int mPreScaledHeight;
    private float mPreScale;

    public PageImage(@NonNull Bitmap bitmap) {
        mBitmap = bitmap;
        mPreScaledHeight = 0;
        mPreScale = 0;
    }

    public void preScale(float scale) {
        mPreScale = scale;
        mPreScaledHeight = (int) Math.ceil(getOriginalHeight() * scale);
    }

    public boolean isPreScaled() {
        return mPreScale != 0;
    }

    public int getOriginalHeight() {
        return mBitmap.getHeight();
    }

    public int getOriginalWidth() {
        return mBitmap.getWidth();
    }

    public Rect draw(Canvas canvas, Paint paint, int offsetX, int offsetY, Rect viewport, float scale) {
        if (mPreScale != 0) {
            scale *= mPreScale;
        }
        Rect outRect = new Rect(
                offsetX,
                offsetY,
                ((int) (offsetX + (getOriginalWidth() * scale))),
                (int) (offsetY + (getOriginalHeight() * scale))
        );
        DecartUtils.trimRect(outRect, viewport);
        if (!isRecycled()) {
            Rect inRect = new Rect(outRect);
            DecartUtils.translateRect(inRect, -offsetX, -offsetY);
            DecartUtils.scaleRect(inRect, 1f / scale);
            canvas.drawBitmap(mBitmap, inRect, outRect, paint);
        }
        return outRect;
    }

    public boolean isRecycled() {
        return mBitmap.isRecycled();
    }

    public void recycle() {
        mBitmap.recycle();
    }

    public int getPreScaledHeight() {
        return mPreScaledHeight;
    }

    public void resetPreScale() {
        mPreScale = 0;
        mPreScaledHeight = 0;
    }

    public int bytesSize() {
        return mBitmap.getByteCount();
    }
}
