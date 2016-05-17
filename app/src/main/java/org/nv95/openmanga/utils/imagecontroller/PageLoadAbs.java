package org.nv95.openmanga.utils.imagecontroller;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.view.View;

import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.nostra13.universalimageloader.core.listener.ImageLoadingProgressListener;
import com.nostra13.universalimageloader.core.process.BitmapProcessor;

import org.nv95.openmanga.OpenMangaApplication;
import org.nv95.openmanga.items.MangaPage;
import org.nv95.openmanga.providers.MangaProvider;

import javax.microedition.khronos.opengles.GL10;

public abstract class PageLoadAbs implements ImageLoadingListener, ImageLoadingProgressListener {
    protected final MangaPage page;
    private DisplayImageOptions options;
    private PageImageAvare view;
    private AsyncTask<Void, Void, String> task;

    public PageLoadAbs(MangaPage page, SubsamplingScaleImageView view) {
        this.page = page;
        this.view = new PageImageAvare(view);

        options = OpenMangaApplication.getImageLoaderOptionsBuilder()
                    .imageScaleType(ImageScaleType.NONE_SAFE)
//                    .postProcessor(new BitmapProcessor() {
//                        @Override
//                        public Bitmap process(Bitmap bitmap) {

                // TODO как то придумать как вставлять разрезанную картинку в pager
                // напримаер
        //        Bitmap bm1 = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), (bitmap.getHeight() / 2));
        //        Bitmap bm2 = Bitmap.createBitmap(bitmap, 0, (bitmap.getHeight() / 2), bitmap.getWidth(), (bitmap.getHeight() / 2));


//                            int width = 0, height = 0;
//                            float videoAspectRatio = bitmap.getWidth() / (float)bitmap.getHeight();
//                            if(bitmap.getHeight() > GL10.GL_MAX_TEXTURE_SIZE) {
//                                height = GL10.GL_MAX_TEXTURE_SIZE;
////                                viewWidthToBitmapWidthRatio = bitmap.getWidth() / (float)bitmap.getHeight();
//                                if (videoAspectRatio > 1) {
//                                    width = (int) (height / videoAspectRatio);
//                                } else {
//                                    width = (int) (height * videoAspectRatio);
//                                }
//                            }
//                            if(bitmap.getWidth() > GL10.GL_MAX_TEXTURE_SIZE) {
//                                width = GL10.GL_MAX_TEXTURE_SIZE;
//                                if (videoAspectRatio > 1) {
//                                    height = (int) (width / videoAspectRatio);
//                                } else {
//                                    height = (int) (width * videoAspectRatio);
//                                }
//                            }
//
//                            if(width == 0 || height == 0)
//                                return bitmap;
//
//                            return Bitmap.createScaledBitmap(bitmap, width, height, false);
//                        }
//                    })
                    .build();
    }



    protected abstract void preLoad();

    protected abstract void onLoadingComplete();

    private class PrepareTask extends AsyncTask<Void,Void,String> {

        @Override
        protected String doInBackground(Void... params) {
            try {
                return ((MangaProvider) page.provider.newInstance()).getPageImage(page);
            } catch (Exception e) {
                return page.path;
            }
        }

        @Override
        protected void onPostExecute(String path) {
            super.onPostExecute(path);
            if (path != null) {
                if (path.startsWith("/")) {
                    path = "file://" + path;
                }
                ImageLoader.getInstance().displayImage(path, view, options, PageLoadAbs.this, PageLoadAbs.this);
            } else {
                onLoadingFailed(null, null, null);
            }
        }
    }

    public void load() {
        preLoad();
        task = new PrepareTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public void cancel(){
        if(task!=null)
            task.cancel(true);
        ImageLoader.getInstance().cancelDisplayTask(this.view);
    }

    @Override
    public void onLoadingStarted(String imageUri, View view) {

    }

    @Override
    public void onLoadingFailed(String imageUri, View view, FailReason failReason) {

    }

    @Override
    public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
        onLoadingComplete();
    }

    @Override
    public void onLoadingCancelled(String imageUri, View view) {

    }

    @Override
    public void onProgressUpdate(String imageUri, View view, int current, int total) {

    }
}