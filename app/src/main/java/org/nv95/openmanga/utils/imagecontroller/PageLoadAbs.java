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

import org.nv95.openmanga.OpenMangaApplication;
import org.nv95.openmanga.R;
import org.nv95.openmanga.items.MangaPage;
import org.nv95.openmanga.providers.MangaProvider;

public abstract class PageLoadAbs implements ImageLoadingListener, ImageLoadingProgressListener {

    protected final MangaPage mPage;
    private static DisplayImageOptions mOptions = null;
    private PageImageAvare mView;
    private AsyncTask<Void, Void, String> mTask;

    public PageLoadAbs(MangaPage page, SubsamplingScaleImageView view) {
        mPage = page;
        this.mView = new PageImageAvare(view);
        if (mOptions == null) {
            mOptions = OpenMangaApplication.getImageLoaderOptionsBuilder()
                    .imageScaleType(ImageScaleType.NONE)
                    .postProcessor(ImageShifter.getInstance()
                            .setSpace(view.getContext().getResources()
                                    .getDimensionPixelSize(R.dimen.padding8)))
                    .build();
        }
    }

    protected abstract void preLoad();

    protected abstract void onLoadingComplete();

    private class PrepareTask extends AsyncTask<Void,Void,String> {

        @Override
        protected String doInBackground(Void... params) {
            try {
                return ((MangaProvider) mPage.provider.newInstance()).getPageImage(mPage);
            } catch (Exception e) {
                return mPage.path;
            }
        }

        @Override
        protected void onPostExecute(String path) {
            super.onPostExecute(path);
            if (path != null) {
                if (path.startsWith("/")) {
                    path = "file://" + path;
                }
                ImageLoader.getInstance().displayImage(path, mView, mOptions, PageLoadAbs.this, PageLoadAbs.this);
            } else {
                onLoadingFailed(null, null, null);
            }
        }
    }

    public void load() {
        preLoad();
        mTask = new PrepareTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public void cancel(){
        if(mTask !=null)
            mTask.cancel(true);
        ImageLoader.getInstance().cancelDisplayTask(this.mView);
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