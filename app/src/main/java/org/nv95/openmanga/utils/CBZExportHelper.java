package org.nv95.openmanga.utils;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;

import org.nv95.openmanga.R;
import org.nv95.openmanga.items.MangaInfo;

import java.io.File;

/**
 * Created by nv95 on 28.01.16.
 */
public class CBZExportHelper {
    private final Context mContext;

    public CBZExportHelper(Context context) {
        mContext = context;
    }

    public void export(MangaInfo manga) {
        new ExportTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, manga);
    }

    private class ExportTask extends AsyncTask<MangaInfo, Integer, File> {
        private final ProgressDialog mProgressDialog;

        public ExportTask() {
            super();
            mProgressDialog = new ProgressDialog(mContext);
            mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            mProgressDialog.setMessage(mContext.getString(R.string.exporting));
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mProgressDialog.setIndeterminate(true);
            mProgressDialog.show();
        }

        @Override
        protected File doInBackground(MangaInfo... params) {
            //reading full data

            //creating dirs and copying files


            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected void onPostExecute(File file) {
            super.onPostExecute(file);
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
        }
    }
}
