package org.nv95.openmanga.helpers;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AlertDialog;

import org.nv95.openmanga.R;
import org.nv95.openmanga.feature.preview.dialog.ChaptersSelectDialog;
import org.nv95.openmanga.items.MangaChapter;
import org.nv95.openmanga.feature.manga.domain.MangaInfo;
import org.nv95.openmanga.items.MangaSummary;
import org.nv95.openmanga.providers.LocalMangaProvider;
import org.nv95.openmanga.providers.MangaProvider;
import org.nv95.openmanga.providers.staff.MangaProviderManager;
import org.nv95.openmanga.feature.download.service.SaveService;

/**
 * Created by admin on 21.07.17.
 */

public class MangaSaveHelper {

    private final Context mContext;

    public MangaSaveHelper(Context context) {
        this.mContext = context;
    }

    public void confirmSave(MangaSummary manga) {
        confirmSave(manga, R.string.chapters_to_save);
    }

    public void confirmSave(MangaInfo[] mangas) {
        new GetDetailsTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,
                mangas);
    }

    public void confirmSave(MangaSummary manga, @StringRes int dialogTitle) {
        new ChaptersSelectDialog(mContext).showSave(manga, dialogTitle);
    }

    public void save(MangaSummary manga) {
        mContext.startService(new Intent(mContext, SaveService.class)
                .putExtra("action", SaveService.ACTION_ADD)
                .putExtras(manga.toBundle()));
    }

    public void save(MangaSummary manga, MangaChapter chapter) {
        save(manga, chapter, chapter);
    }

    public void save(MangaSummary manga, MangaChapter first, MangaChapter last) {
        MangaSummary copy = new MangaSummary(manga);
        copy.chapters.clear();
        int firstPos = manga.chapters.indexOf(first);
        int lastPos = manga.chapters.lastIndexOf(last);
        copy.chapters.addAll(manga.chapters.subList(firstPos, lastPos + 1));
        save(copy);
    }

    public void cancelAll() {
        mContext.startService(new Intent(mContext, SaveService.class)
                .putExtra("action", SaveService.ACTION_CANCEL_ALL));
    }

    public void saveLast(MangaSummary manga, int count) {
        MangaSummary copy = new MangaSummary(manga);
        copy.chapters.clear();
        int lastPos = manga.chapters.size() - 1;
        copy.chapters.addAll(manga.chapters.subList(lastPos - count, lastPos));
        save(copy);
    }

    @SuppressLint("StaticFieldLeak")
    private class GetDetailsTask extends AsyncTask<MangaInfo, Integer, MangaSummary[]>
            implements DialogInterface.OnClickListener {

        private final ProgressDialog mProgressDialog;

        GetDetailsTask() {
            super();
            mProgressDialog = new ProgressDialog(mContext);
            mProgressDialog.setMessage(mContext.getString(R.string.loading));
            mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            mProgressDialog.setIndeterminate(true);
            mProgressDialog.setCancelable(false);
            mProgressDialog.setButton(DialogInterface.BUTTON_NEGATIVE,
                    mContext.getString(android.R.string.cancel), this);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mProgressDialog.show();
        }

        @Override
        protected MangaSummary[] doInBackground(MangaInfo... params) {
            MangaSummary[] summaries = new MangaSummary[params.length];
            MangaProvider provider;
            publishProgress(0, params.length);
            for (int i = 0; i < params.length && !isCancelled(); i++) {
                if (params[i] == null || params[i].provider == LocalMangaProvider.class) {
                    summaries[i] = null;
                } else try {
                    provider = MangaProviderManager.instanceProvider(mContext, params[i].provider);
                    if (provider instanceof LocalMangaProvider) {
                        summaries[i] = null;
                    } else {
                        summaries[i] = provider.getDetailedInfo(params[i]);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    summaries[i] = null;
                }
                publishProgress(i, params.length);
            }
            return summaries;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            mProgressDialog.setIndeterminate(false);
            mProgressDialog.setMax(values[1]);
            mProgressDialog.setProgress(values[0]);
        }

        @Override
        protected void onPostExecute(final MangaSummary[] mangaSummaries) {
            super.onPostExecute(mangaSummaries);
            int mangas = 0, chapters = 0;
            for (MangaSummary o : mangaSummaries) {
                if (o != null) {
                    mangas++;
                    chapters += o.chapters.size();
                }
            }
            mProgressDialog.dismiss();
            new AlertDialog.Builder(mContext)
                    .setMessage(mContext.getString(R.string.multiple_save_confirm, mangas, chapters))
                    .setNegativeButton(android.R.string.cancel, null)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            for (MangaSummary o : mangaSummaries) {
                                if (o != null) {
                                    save(o);
                                }
                            }
                        }
                    })
                    .create().show();
        }

        @Override
        public void onClick(DialogInterface dialog, int which) {
            if (which == DialogInterface.BUTTON_NEGATIVE) {
                mProgressDialog.setIndeterminate(true);
                mProgressDialog.setMessage(mContext.getString(R.string.cancelling));
                this.cancel(false);
            }
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            mProgressDialog.dismiss();
        }
    }
}
