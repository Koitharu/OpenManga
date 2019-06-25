package org.nv95.openmanga.utils;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;

import org.nv95.openmanga.R;
import org.nv95.openmanga.core.network.NetworkUtils;
import org.nv95.openmanga.feature.read.ReadActivity2;
import org.nv95.openmanga.feature.manga.domain.MangaInfo;
import org.nv95.openmanga.items.MangaSummary;
import org.nv95.openmanga.providers.HistoryProvider;
import org.nv95.openmanga.providers.LocalMangaProvider;
import org.nv95.openmanga.providers.MangaProvider;
import org.nv95.openmanga.providers.staff.MangaProviderManager;

/**
 * Created by admin on 18.07.17.
 */

public class QuickReadTask extends AsyncTask<MangaInfo, Void, Bundle> implements DialogInterface.OnCancelListener {

    private final ProgressDialog mProgressDialog;

    public QuickReadTask(Context context) {
        mProgressDialog = new ProgressDialog(context);
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.setMessage(context.getString(R.string.loading));
        mProgressDialog.setOnCancelListener(this);
        mProgressDialog.setCancelable(true);
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        mProgressDialog.show();
    }

    @Override
    protected Bundle doInBackground(MangaInfo... mangaInfos) {
        try {
            MangaInfo manga = mangaInfos[0];
            MangaProvider provider;
            if (manga.provider.equals(LocalMangaProvider.class)) {
                provider = LocalMangaProvider.getInstance(mProgressDialog.getContext());
            } else {
                if (!NetworkUtils.checkConnection(mProgressDialog.getContext())) {
                    provider = LocalMangaProvider.getInstance(mProgressDialog.getContext());
                    manga = ((LocalMangaProvider)provider).getLocalManga(manga);
                    if (manga.provider != LocalMangaProvider.class) {
                        return null;
                    }
                } else {
                    provider = MangaProviderManager.instanceProvider(mProgressDialog.getContext(), manga.provider);
                }
            }
            MangaSummary summary = provider.getDetailedInfo(manga);
            if (summary.chapters.isEmpty()) {
                return null;
            }
            if (isCancelled()) {
                return null;
            }
            Bundle bundle = new Bundle();
            bundle.putAll(summary.toBundle());
            HistoryProvider.HistorySummary hs = HistoryProvider.getInstance(mProgressDialog.getContext()).get(manga);
            if (hs != null) {
                int index = summary.chapters.indexByNumber(hs.getChapter());
                if (index != -1) {
                    bundle.putInt("chapter", index);
                    bundle.putInt("page", hs.getPage());
                }
            }
            return bundle;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    protected void onPostExecute(Bundle bundle) {
        super.onPostExecute(bundle);
        if (bundle != null) {
            Context c = mProgressDialog.getContext();
            c.startActivity(new Intent(c, ReadActivity2.class).putExtras(bundle));
        }
        mProgressDialog.dismiss();
    }

    @Override
    public void onCancel(DialogInterface dialogInterface) {
        this.cancel(false);
    }
}
