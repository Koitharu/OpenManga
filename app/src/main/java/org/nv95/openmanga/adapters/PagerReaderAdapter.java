package org.nv95.openmanga.adapters;

import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.Nullable;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;

import org.nv95.openmanga.R;
import org.nv95.openmanga.items.MangaPage;
import org.nv95.openmanga.providers.MangaProvider;
import org.nv95.openmanga.utils.ErrorReporter;
import org.nv95.openmanga.utils.SerialExecutor;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

/**
 * Created by nv95 on 30.09.15.
 */
public class PagerReaderAdapter extends PagerAdapter implements View.OnClickListener {
    private final LayoutInflater inflater;
    private final ArrayList<MangaPage> pages;
    private final SerialExecutor executor = new SerialExecutor();

    public PagerReaderAdapter(Context context, ArrayList<MangaPage> mangaPages) {
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        pages = mangaPages;
    }

    @Override
    public void onClick(View v) {
        Object tag = v.getTag();
        if (tag == null || !(tag instanceof ViewHolder)) {
            return;
        }
        ViewHolder holder = (ViewHolder) tag;
        MangaPage page = pages.get(holder.position);
        holder.loadTask = new PageLoadTask(inflater.getContext(), holder, page);
        holder.loadTask.executeOnExecutor(executor);
    }

    @Override
    public int getCount() {
        return pages.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view.equals(object);
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        View view = inflater.inflate(R.layout.item_page, null);
        MangaPage page = getItem(position);
        ViewHolder holder = new ViewHolder();
        holder.position = position;
        holder.progressBar = (ProgressBar) view.findViewById(R.id.progressBar);
        holder.ssiv = (SubsamplingScaleImageView) view.findViewById(R.id.ssiv);
        holder.ssiv.setMinimumScaleType(SubsamplingScaleImageView.SCALE_TYPE_CENTER_CROP);
        holder.buttonRetry = (Button) view.findViewById(R.id.button_retry);
        holder.buttonRetry.setTag(holder);
        holder.buttonRetry.setOnClickListener(this);
        holder.textView = (TextView) view.findViewById(R.id.textView_progress);
        holder.loadTask = new PageLoadTask(inflater.getContext(), holder, page);
        holder.loadTask.executeOnExecutor(executor);
        container.addView(view, 0);
        return view;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        if (!(object instanceof View)) {
            return;
        }
        View view = (View) object;
        ViewHolder holder = (ViewHolder) view.getTag();
        if (holder != null && holder.loadTask != null && holder.loadTask.getStatus() == AsyncTask.Status.RUNNING) {
            holder.loadTask.cancel(false);
        }
        container.removeView(view);
    }

    public MangaPage getItem(int position) {
        return pages.get(position);
    }

    private static class ViewHolder {
        ProgressBar progressBar;
        SubsamplingScaleImageView ssiv;
        TextView textView;
        Button buttonRetry;
        @Nullable
        PageLoadTask loadTask;
        int position;
    }

    public static class PageLoadTask extends AsyncTask<Void, Integer, File> implements SubsamplingScaleImageView.OnImageEventListener {
        private final ViewHolder viewHolder;
        private final File cacheDir;
        private MangaPage page;

        public PageLoadTask(Context context, ViewHolder viewHolder, MangaPage page) {
            this.viewHolder = viewHolder;
            this.page = page;
            cacheDir = context.getExternalCacheDir();

            viewHolder.ssiv.setParallelLoadingEnabled(true);
            viewHolder.ssiv.setOnImageEventListener(this);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            viewHolder.progressBar.setVisibility(View.VISIBLE);
            viewHolder.textView.setText(R.string.loading);
            viewHolder.textView.setVisibility(View.VISIBLE);
            viewHolder.buttonRetry.setVisibility(View.GONE);
        }

        @Override
        protected void onPostExecute(File file) {
            super.onPostExecute(file);
            if (file != null && file.exists()) {
                ImageSource source = ImageSource.uri(file.getPath()).tilingEnabled();
                viewHolder.ssiv.setImage(source);
                viewHolder.textView.setText(R.string.wait);
            } else {
                viewHolder.progressBar.setVisibility(View.GONE);
                viewHolder.textView.setVisibility(View.GONE);
                viewHolder.buttonRetry.setVisibility(View.VISIBLE);
            }
            viewHolder.loadTask = null;
        }

        @Override
        protected void onCancelled(File file) {
            super.onCancelled(file);
            if (file != null && file.exists()) {
                file.delete();
            }
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            viewHolder.textView.setText(values[0] + "%");
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
                new File(destination).delete();
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
        protected File doInBackground(Void... params) {
            String path;
            publishProgress(0);
            try {
                path = ((MangaProvider) page.getProvider().newInstance()).getPageImage(page);
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
                file = new File(cacheDir, String.valueOf(path.hashCode()));
            }
            if (!file.exists() && !isCancelled()) {
                downloadFile(path, file.getPath());
            } else {
                publishProgress(-1);
            }
            return file;
        }

        @Override
        public void onReady() {
            viewHolder.progressBar.setVisibility(View.GONE);
            viewHolder.textView.setVisibility(View.GONE);
        }

        @Override
        public void onImageLoaded() {
            viewHolder.progressBar.setVisibility(View.GONE);
            viewHolder.textView.setVisibility(View.GONE);
        }

        @Override
        public void onPreviewLoadError(Exception e) {
            viewHolder.progressBar.setVisibility(View.GONE);
            viewHolder.textView.setVisibility(View.GONE);
            viewHolder.buttonRetry.setVisibility(View.VISIBLE);

        }

        @Override
        public void onImageLoadError(Exception e) {
            viewHolder.progressBar.setVisibility(View.GONE);
            viewHolder.textView.setVisibility(View.GONE);
            viewHolder.buttonRetry.setVisibility(View.VISIBLE);
            ErrorReporter.getInstance().report("# PageLoadTask.onImageLoadError\n page.path: " + page.getPath());
        }

        @Override
        public void onTileLoadError(Exception e) {
            viewHolder.progressBar.setVisibility(View.GONE);
            viewHolder.textView.setVisibility(View.GONE);
            viewHolder.buttonRetry.setVisibility(View.VISIBLE);
            ErrorReporter.getInstance().report("# PageLoadTask.onTileLoadError\n page.path: " + page.getPath());
        }
    }
}
