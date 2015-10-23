package org.nv95.openmanga;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.os.AsyncTask;
import android.util.LruCache;
import android.widget.ImageView;

import org.nv95.openmanga.components.ErrorReporter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.util.concurrent.Executor;

public class ImageLoadTask  extends AsyncTask<Void, Void, Bitmap> {
    public static final Executor FIXED_EXECUTOR = AsyncTask.THREAD_POOL_EXECUTOR;
    //кэш в памяти
    private static LruCache<String, Bitmap> mMemoryCache = new LruCache<String, Bitmap>((int) (Runtime.getRuntime().maxMemory() / 1024)/8) {
        @Override
        protected int sizeOf(String key, Bitmap bitmap) {
            // The cache size will be measured in kilobytes rather than
            // number of items.
            return bitmap.getByteCount() / 1024;
        }
    };
    //ну тут ясно всё
    public static void addBitmapToMemoryCache(String key, Bitmap bitmap) {
        if (getBitmapFromMemCache(key) == null) {
            mMemoryCache.put(key, bitmap);
        }
    }
    //тут тоже
    public static Bitmap getBitmapFromMemCache(String key) {
        return mMemoryCache.get(key);
    }

    private final String url;
    private final File file;
    private final Context context;
    private final WeakReference<ImageView> imageViewReference;
    private boolean round;

    //делает пикчу круглой
    public static Bitmap createSquaredBitmap(Bitmap bitmap) {
        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(),
                bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawCircle(bitmap.getWidth() / 2, bitmap.getHeight() / 2,
                (bitmap.getWidth()>bitmap.getHeight()?bitmap.getHeight() / 2:bitmap.getWidth() / 2), paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);
        return output;
    }

    public static Bitmap loadBitmap(String url) {
        try {
            return BitmapFactory.decodeStream((InputStream) new URL(url).getContent());
        } catch (Exception e) {
            return null;
        }
    }

    public ImageLoadTask(ImageView imageView, String url, boolean round, Drawable defaultDrawable) {
        this.url = url;
        this.context = imageView.getContext();
        imageViewReference = new WeakReference<>(imageView);
        this.round = round;
        Bitmap bitmap;
        if (!url.startsWith("http") && new File(url).exists()) {
            file = new File(url);
        } else {
            file = new File(context.getExternalCacheDir(), String.valueOf(url.hashCode()));
        }
        imageView.setTag(file.getName());
        if ((bitmap = getBitmapFromMemCache(file.getName())) != null) {
            if (round)
                bitmap = createSquaredBitmap(bitmap);
            imageView.setImageBitmap(bitmap);
            this.cancel(true);
            return;
        }
        if (defaultDrawable != null)
            imageView.setImageDrawable(defaultDrawable);
    }

    //кэширует на карту памяти
    private void cacheBitmap(Bitmap bitmap, File file)
    {
        try {
            OutputStream fOut = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fOut);
            fOut.flush();
            fOut.close();
        }
        catch (Exception e)
        {
            new ErrorReporter(context).report(e);
        }
    }

    @Override
    protected Bitmap doInBackground(Void... voids) {
        Bitmap bitmap;
        if (file.exists()) {
            try {
                bitmap = BitmapFactory.decodeStream(new FileInputStream(file));
            } catch (FileNotFoundException e) {
                bitmap = loadBitmap(url);
                if (bitmap!=null)
                    cacheBitmap(bitmap,file);
            } catch (OutOfMemoryError e1) {
                return null;
            }
        } else {
            bitmap = loadBitmap(url);
            cacheBitmap(bitmap,file);
        }
        if (bitmap!=null)
            addBitmapToMemoryCache(file.getName(),bitmap);
        if (round)
            bitmap = createSquaredBitmap(bitmap);
        return bitmap;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected void onPostExecute(Bitmap bdrawable) {
        super.onPostExecute(bdrawable);
        final ImageView imageView = imageViewReference.get();
        if (bdrawable != null && imageView != null && imageView.getTag().equals(file.getName())) {
            TransitionDrawable drawable = new TransitionDrawable(new Drawable[]{imageView.getDrawable(), new BitmapDrawable(bdrawable)});
            drawable.setCrossFadeEnabled(false);
            drawable.startTransition(100);
            //drawable.setBounds(0,0,imageView.getMeasuredWidth(),imageView.getMeasuredHeight());
            imageView.setImageDrawable(drawable);
        }
    }
}