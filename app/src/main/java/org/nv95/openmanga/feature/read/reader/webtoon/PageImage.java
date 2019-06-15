package org.nv95.openmanga.feature.read.reader.webtoon;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import androidx.annotation.NonNull;

import org.nv95.openmanga.utils.DecartUtils;

/**
 * Created by admin on 01.08.17.
 */

public class PageImage {

    @NonNull
    private final Bitmap mBitmap;

    public PageImage(@NonNull Bitmap bitmap) {
        mBitmap = bitmap;
    }

    public int getHeight() {
        return mBitmap.getHeight();
    }

    public int getWidth() {
        return mBitmap.getWidth();
    }

    public Rect draw(Canvas canvas, Paint paint, int offsetX, int offsetY, Rect viewport, float scale) {
        Rect outRect = new Rect(
                offsetX,
                offsetY,
                ((int) (offsetX + (getWidth() * scale))),
                (int) (offsetY + (getHeight() * scale))
        );
        DecartUtils.trimRect(outRect, viewport);
        if (!isRecycled()) {
            Rect inRect = new Rect(outRect);
            DecartUtils.translateRect(inRect, -offsetX, -offsetY);
            if (scale != 1f) {
                DecartUtils.scaleRect(inRect, 1f / scale);
            }
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
}
