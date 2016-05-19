package org.nv95.openmanga.utils.imagecontroller;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;

import com.nostra13.universalimageloader.core.process.BitmapProcessor;

/**
 * Created by admin on 19.05.16.
 */
public class ImageShifter implements BitmapProcessor {
    private static final int SPACE = 8;

    @Override
    public Bitmap process(Bitmap bitmap) {
        if (bitmap.getHeight() <= bitmap.getWidth() * 3) {
            return bitmap;
        }
        final Paint paint = new Paint();
        final Bitmap res = Bitmap.createBitmap(bitmap.getWidth() * 2 + SPACE, bitmap.getHeight() / 2, bitmap.getConfig());
        final Canvas canvas = new Canvas(res);
        canvas.drawBitmap(bitmap,
                new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight() / 2),
                new RectF(0, 0, bitmap.getWidth(), bitmap.getHeight() / 2),
                paint);
        canvas.drawBitmap(bitmap,
                new Rect(0, bitmap.getHeight() / 2, bitmap.getWidth(), bitmap.getHeight()),
                new RectF(bitmap.getWidth() + SPACE, 0, bitmap.getWidth() * 2 + SPACE, bitmap.getHeight() / 2),
                paint);
        bitmap.recycle();
        return res;
    }
}
