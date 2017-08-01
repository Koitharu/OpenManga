package org.nv95.openmanga.components.reader.webtoon;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.annotation.NonNull;

/**
 * Created by admin on 01.08.17.
 */

public class PageImage {

    @NonNull
    private final Bitmap mBitmap;
    private final Rect mRect;

    public PageImage(@NonNull Bitmap bitmap) {
        mBitmap = bitmap;
        mRect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
    }

    public int getHeight() {
        return mRect.height();
    }

    public int getWidth() {
        return mRect.width();
    }

    public void scale(float factor) {
        mRect.right = (int) (mRect.left + (mBitmap.getWidth() * factor));
        mRect.bottom = (int) (mRect.top + (mBitmap.getHeight() * factor));
    }

    public Rect draw(Canvas canvas, Paint paint, int offsetX, int offsetY) {
        mRect.left += offsetX;
        mRect.right += offsetX;
        mRect.top += offsetY;
        mRect.bottom += offsetY;

        canvas.drawBitmap(mBitmap, bitmapRect(mBitmap), mRect, paint);
        return mRect;
    }

    @NonNull
    private static Rect bitmapRect(Bitmap b) {
        return new Rect(0,0,b.getWidth(), b.getHeight());
    }
}
