package org.nv95.openmanga.utils;

import android.os.AsyncTask;

import org.nv95.openmanga.feature.main.domain.MangaListLoader;
import org.nv95.openmanga.feature.manga.domain.MangaInfo;
import org.nv95.openmanga.lists.MangaList;
import org.nv95.openmanga.providers.MangaProvider;

import java.lang.ref.WeakReference;
import java.util.Iterator;

/**
 * Created by admin on 14.07.17.
 */

public class DeltaUpdater {

    private MangaListLoader mLoader;

    public DeltaUpdater(MangaListLoader loader) {
        mLoader = loader;
    }

    public void update(MangaProvider provider) {
        if (provider.isMultiPage()) {
            throw new RuntimeException("Provider must not be multipaged");
        }
        new ContentLoadTask(mLoader).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, provider);
    }

    private static class ContentLoadTask extends AsyncTask<MangaProvider, Void, MangaList> {

        private final WeakReference<MangaListLoader> mLoaderRef;

        ContentLoadTask(MangaListLoader loader) {
            mLoaderRef = new WeakReference<>(loader);
        }

        @Override
        protected MangaList doInBackground(MangaProvider... mangaProviders) {
            try {
                return mangaProviders[0].getList(0, 0, 0);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(MangaList newList) {
            super.onPostExecute(newList);
            if (newList == null) {
                return;
            }
            MangaListLoader loader = mLoaderRef.get();
            if (loader == null) {
                return;
            }
            MangaList oldList = loader.getList();
            for (MangaInfo newItem: newList) {
                if (!oldList.contains(newItem)) {
                    loader.addItem(newItem);
                }
            }
            Iterator<MangaInfo> it = oldList.iterator();
            while (it.hasNext()) {
                MangaInfo oldItem = it.next();
                if (!newList.contains(oldItem)) {
                    loader.notifyRemoved(oldList.indexOf(oldItem.id));
                    it.remove();
                }
            }
        }
    }
}
