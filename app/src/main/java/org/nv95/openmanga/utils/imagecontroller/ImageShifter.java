package org.nv95.openmanga.utils.imagecontroller;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;

import com.nostra13.universalimageloader.core.process.BitmapProcessor;

import javax.microedition.khronos.opengles.GL10;

/**
 * Created by admin on 19.05.16.
 */
public class ImageShifter implements BitmapProcessor {
    private static final int SPACE = 8;

    @Override
    public Bitmap process(Bitmap bitmap) {
        if (bitmap.getHeight() <= bitmap.getWidth() * 3) {
            return scaleBitmap(bitmap);
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
        return scaleBitmap(res);
    }

    private Bitmap scaleBitmap (Bitmap bitmap) {
        int width = 0, height = 0;
        float videoAspectRatio = bitmap.getWidth() / (float) bitmap.getHeight();
        if (bitmap.getHeight() > GL10.GL_MAX_TEXTURE_SIZE) {

            height = GL10.GL_MAX_TEXTURE_SIZE;
            if (videoAspectRatio > 1) {
                width = (int) (height / videoAspectRatio);
            } else {
                width = (int) (height * videoAspectRatio);
            }
        }
        if (bitmap.getWidth() > GL10.GL_MAX_TEXTURE_SIZE) {
            width = GL10.GL_MAX_TEXTURE_SIZE;
            if (videoAspectRatio > 1) {
                height = (int) (width / videoAspectRatio);
            } else {
                height = (int) (width * videoAspectRatio);
            }
        }

        if (width == 0 || height == 0)
            return bitmap;

        Bitmap newBitmap = Bitmap.createScaledBitmap(bitmap, width, height, false);
        bitmap.recycle();
        return newBitmap;
    }
}
