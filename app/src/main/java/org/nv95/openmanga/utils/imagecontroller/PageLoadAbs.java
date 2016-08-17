package org.nv95.openmanga.utils.imagecontroller;

import android.os.AsyncTask;
import android.support.annotation.Nullable;

import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;
import com.nostra13.universalimageloader.cache.disc.DiskCache;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.utils.DiskCacheUtils;
import com.nostra13.universalimageloader.utils.IoUtils;

import org.nv95.openmanga.items.MangaPage;

import java.io.File;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public abstract class PageLoadAbs implements SubsamplingScaleImageView.OnImageEventListener, FileConverter.ConvertCallback {

    private final MangaPage mPage;
    private final SubsamplingScaleImageView mView;
    @Nullable
    private String mFileName;
    private AsyncTask<Void,Integer,String> mTask;

    public PageLoadAbs(MangaPage page, SubsamplingScaleImageView view, boolean tiling) {
        mPage = page;
        mView = view;
        mView.setOnImageEventListener(this);
    }

    protected abstract void preLoad();

    protected abstract void onLoadingComplete();

    @Override
    public final void onReady() {

    }

    @Override
    public final void onImageLoaded() {
        onLoadingComplete();
    }

    @Override
    public final void onPreviewLoadError(Exception e) {

    }

    @Override
    public final void onImageLoadError(Exception e) {
        if (mFileName != null) {
            onProgressUpdate(-1, -1);
            FileConverter.getInstance().convertAsync(mFileName, this);
        } else {
            onLoadingFailed(e);
        }
    }

    @Override
    public final void onTileLoadError(Exception e) {

    }

    @Override
    public void onConvertDone(boolean success) {
        if (success) {
            mView.setImage(ImageSource.uri(mFileName).tilingEnabled());
            mFileName = null;
        } else {
            onLoadingFailed(new RuntimeException("Convert failed"));
        }
    }

    private class LoadTask extends AsyncTask<Void,Integer,String> {

        @Override
        protected String doInBackground(Void... params) {
            try {
                if (mPage.path.startsWith("/")) {
                    return mPage.path;
                }
                String url =  mPage.provider.newInstance().getPageImage(mPage);
                DiskCache cache = ImageLoader.getInstance().getDiskCache();
                File file = DiskCacheUtils.findInCache(url, cache);
                if (file != null) {
                    return file.getAbsolutePath();
                }
                HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
                connection.connect();
                InputStream is = connection.getInputStream();

                cache.save(url, is, new IoUtils.CopyListener() {
                    @Override
                    public boolean onBytesCopied(int current, int total) {
                        if (total > 0) {
                            publishProgress(current, total);
                        }
                        return !isCancelled();
                    }
                });
                file = DiskCacheUtils.findInCache(url, cache);
                if (file != null) {
                    return file.getAbsolutePath();
                } else {
                    return null;
                }
            } catch (Exception e) {
                return null;
            }
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            PageLoadAbs.this.onProgressUpdate(values[0], values[1]);
        }

        @Override
        protected void onPostExecute(String path) {
            super.onPostExecute(path);
            if (path != null) {
                mFileName = path;
                mView.setImage(ImageSource.uri(path).tilingEnabled());
            } else {
                onLoadingFailed(null);
            }
        }
    }

    public void load() {
        mFileName = null;
        preLoad();
        mTask = new LoadTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public void cancel(){
        if(mTask !=null)
            mTask.cancel(true);
    }

    public void onLoadingFailed(Exception e) {}

    public void onProgressUpdate(int current, int total) {}
}