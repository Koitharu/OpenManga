package org.nv95.openmanga;

import android.content.Context;
import android.os.AsyncTask;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;

import org.nv95.openmanga.utils.ErrorReporter;
import org.nv95.openmanga.providers.MangaPage;
import org.nv95.openmanga.providers.MangaProvider;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;

public class PageLoadTask extends AsyncTask<Void,Integer,ImageSource> implements SubsamplingScaleImageView.OnImageEventListener {
    private final WeakReference<SubsamplingScaleImageView> imageViewReference;
    private final WeakReference<ProgressBar> progressBarReference;
    private final WeakReference<TextView> textViewReference;
    private MangaPage page;
    private final Context context;

    public PageLoadTask(Context context, SubsamplingScaleImageView imageView, ProgressBar progressBar, TextView textView, MangaPage page) {
        imageViewReference = new WeakReference<>(imageView);
        progressBarReference = new WeakReference<>(progressBar);
        textViewReference = new WeakReference<>(textView);
        imageView.setParallelLoadingEnabled(true);
        this.page = page;
        this.context = context;

        imageView.setOnImageEventListener(this);
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected void onPostExecute(ImageSource imageSource) {
        super.onPostExecute(imageSource);
        final SubsamplingScaleImageView imageView = imageViewReference.get();
        final TextView textView = textViewReference.get();

        if (imageSource != null && imageView != null) {
            imageView.setImage(imageSource);
            textView.setText(R.string.wait);
        } else {
            if (textView != null)
                textView.setText(R.string.loading_error);
        }
        page.setLoadTask(null);
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);
        TextView textView = textViewReference.get();
        if (textView != null)
            textView.setText(values[0] + "%");
    }

    protected void downloadFile(String source, String destination) {
        InputStream input = null;
        OutputStream output = null;
        HttpURLConnection connection = null;
        try {
            URL url = new URL(source);
            connection = (HttpURLConnection) url.openConnection();
            connection.connect();

            // expect HTTP 200 OK, so we don't mistakenly save error report
            // instead of the file
            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                //
            }

            // this will be useful to display download percentage
            // might be -1: server did not report the length
            int fileLength = connection.getContentLength();

            // download the file
            input = connection.getInputStream();
            output = new FileOutputStream(destination);

            byte data[] = new byte[4096];
            long total = 0;
            int count;
            while ((count = input.read(data)) != -1) {
                // allow canceling with back button
                if (isCancelled()) {
                    input.close();
                    output.close();
                    new File(destination).delete();
                    return;
                }
                total += count;
                // publishing the progress....
                if (fileLength > 0) // only if total length is known
                    publishProgress((int) (total * 100 / fileLength));
                output.write(data, 0, count);
            }
        } catch (Exception e) {
            //
        } finally {
            try {
                if (output != null)
                    output.close();
                if (input != null)
                    input.close();
            } catch (IOException ignored) {
            }

            if (connection != null)
                connection.disconnect();
        }
    }

    @Override
    protected ImageSource doInBackground(Void... params) {
        String path = null;
        publishProgress(0);
        try {
            path = ((MangaProvider)page.getProvider().newInstance()).getPageImage(page);
        } catch (Exception e) {
            path = page.getPath();
        }
        if (path == null) {
            return null;
        }
        File file = null;
        if (!path.startsWith("http")) {
             file = new File(path);
        }
        if (file == null || !file.exists()) {
            file = new File(context.getExternalCacheDir(), String.valueOf(path.hashCode()));
        }
        if (!file.exists()) {
            downloadFile(path, file.getPath());
        } else {
            publishProgress(-1);
        }
        return file.exists() ? ImageSource.uri(file.getPath()).tilingDisabled() : null;
    }

    @Override
    public void onReady() {
        final ProgressBar progressBar = progressBarReference.get();
        if (progressBar != null)
            progressBar.setVisibility(View.GONE);
        final TextView textView = textViewReference.get();
        if (textView != null)
            textView.setVisibility(View.GONE);
    }

    @Override
    public void onImageLoaded() {
        final ProgressBar progressBar = progressBarReference.get();
        if (progressBar != null)
            progressBar.setVisibility(View.GONE);
        final TextView textView = textViewReference.get();
        if (textView != null)
            textView.setVisibility(View.GONE);
    }

    @Override
    public void onPreviewLoadError(Exception e) {
        final ProgressBar progressBar = progressBarReference.get();
        if (progressBar != null)
            progressBar.setVisibility(View.GONE);
        final TextView textView = textViewReference.get();
        if (textView != null) {
            String msg = context.getText(R.string.loading_error) + "\n" + e.getLocalizedMessage();
            textView.setText(msg);
        }
    }

    @Override
    public void onImageLoadError(Exception e) {
        final ProgressBar progressBar = progressBarReference.get();
        if (progressBar != null)
            progressBar.setVisibility(View.GONE);
        final TextView textView = textViewReference.get();
        if (textView != null) {
            String msg = context.getText(R.string.loading_error) + "\n" + e.getLocalizedMessage();
            textView.setText(msg);
        }
        new ErrorReporter(context).report("# PageLoadTask.onImageLoadError\n page.path: " + page.getPath());
    }

    @Override
    public void onTileLoadError(Exception e) {
        final ProgressBar progressBar = progressBarReference.get();
        if (progressBar != null)
            progressBar.setVisibility(View.GONE);
        final TextView textView = textViewReference.get();
        if (textView != null) {
            String msg = context.getText(R.string.loading_error) + "\n" + e.getLocalizedMessage();
            textView.setText(msg);
        }
        new ErrorReporter(context).report("# PageLoadTask.onTileLoadError\n page.path: " + page.getPath());
    }
}