package org.nv95.openmanga.utils.imagecontroller;

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;

import com.nostra13.universalimageloader.core.process.BitmapProcessor;

import javax.microedition.khronos.opengles.GL10;

/**
 * Created by admin on 19.05.16.
 */
public class ImageShifter implements BitmapProcessor, SharedPreferences.OnSharedPreferenceChangeListener {

    private static final ImageShifter instance = new ImageShifter();

    private int mSpace = 100;
    //private boolean mShift;
    private boolean mRtl;

    public static ImageShifter getInstance() {
        return instance;
    }

    private ImageShifter() {
        //mShift = true;
        mRtl = false;
    }

    public ImageShifter setSpace(int spacePx) {
        mSpace = spacePx;
        return this;
    }

    @Override
    public Bitmap process(Bitmap bitmap) {
        if (bitmap.getHeight() < GL10.GL_MAX_TEXTURE_SIZE || bitmap.getHeight() <= bitmap.getWidth() * 2.4) {
            return scaleBitmap(bitmap);
        }
        final int count = Math.max((int) Math.sqrt(bitmap.getHeight() / bitmap.getWidth()) - 1, 2);
        final int sectHeight = bitmap.getHeight() / count;
        final Paint paint = new Paint();
        final Paint line = new Paint();
        paint.setAntiAlias(true);
        line.setColor(Color.GRAY);
        line.setStrokeWidth(mSpace/2.f);
        final Bitmap res = Bitmap.createBitmap((bitmap.getWidth() + mSpace) * count - mSpace, sectHeight, bitmap.getConfig());
        final Canvas canvas = new Canvas(res);
        for (int i=0;i<count;i++) {

            RectF dist = !mRtl ?                                                                   /*destination*/
                    new RectF((bitmap.getWidth() + mSpace) * i, 0, (bitmap.getWidth() + mSpace) * i + bitmap.getWidth(), sectHeight) :
                    new RectF((bitmap.getWidth() + mSpace) * (count - i - 1), 0, (bitmap.getWidth() + mSpace) * (count - i) - mSpace, sectHeight);

            canvas.drawBitmap(bitmap,
                    new Rect(0, sectHeight * i, bitmap.getWidth(), sectHeight * (i+1)),     /*source*/
                    dist,
                    paint);
            if (i < (count - 1)) {
                canvas.drawLine(dist.right + (mSpace / 2), 0, dist.right + (mSpace / 2), sectHeight, line);
            }
        }
        bitmap.recycle();
        return scaleBitmap(res);
    }

    private Bitmap scaleBitmap (Bitmap bitmap) {
        int width = 0, height = 0;
        float imageAspectRatio = bitmap.getWidth() / (float) bitmap.getHeight();
        int maxSize = GL10.GL_MAX_TEXTURE_SIZE;
        if (bitmap.getHeight() > maxSize) {

            height = maxSize;
            if (imageAspectRatio > 1) {
                width = (int) (height / imageAspectRatio);
            } else {
                width = (int) (height * imageAspectRatio);
            }
        }
        if (bitmap.getWidth() > maxSize) {
            width = maxSize;
            if (imageAspectRatio > 1) {
                height = (int) (width / imageAspectRatio);
            } else {
                height = (int) (width * imageAspectRatio);
            }
        }

        if (width == 0 || height == 0)
            return bitmap;

        Bitmap newBitmap = Bitmap.createScaledBitmap(bitmap, width, height, false);
        bitmap.recycle();
        return newBitmap;
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        switch (key) {
            /*case "shifts":
                mShift = sharedPreferences.getBoolean("shifts", true);
                break;*/
            case "direction":
                mRtl = "2".equals(sharedPreferences.getString("direction", "0"));
        }
    }
}
