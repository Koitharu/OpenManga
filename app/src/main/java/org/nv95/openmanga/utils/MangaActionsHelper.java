package org.nv95.openmanga.utils;

import android.content.Context;
import android.support.annotation.Nullable;

import org.nv95.openmanga.items.MangaInfo;
import org.nv95.openmanga.providers.FavouritesProvider;
import org.nv95.openmanga.providers.HistoryProvider;
import org.nv95.openmanga.providers.LocalMangaProvider;
import org.nv95.openmanga.providers.MangaProvider;

/**
 * Created by nv95 on 26.01.16.
 */
public class MangaActionsHelper {
    private final MangaInfo mMangaInfo;
    @Nullable
    private final MangaProvider mProvider;

    public MangaActionsHelper(Context context, MangaInfo mangaInfo) {
        MangaProvider provider;
        this.mMangaInfo = mangaInfo;
        if (mangaInfo.provider.equals(LocalMangaProvider.class)) {
            provider = LocalMangaProvider.getInstacne(context);
        } else if (mangaInfo.provider.equals(FavouritesProvider.class)) {
            provider = FavouritesProvider.getInstacne(context);
        } else if (mangaInfo.provider.equals(HistoryProvider.class)) {
            provider = HistoryProvider.getInstacne(context);
        } else {
            try {
                provider = (MangaProvider) mangaInfo.provider.newInstance();
            } catch (Exception e) {
                provider = null;
            }
        }
        mProvider = provider;
    }

    public boolean checkFeature(int feature) {
        return mProvider != null && mProvider.hasFeature(feature);
    }

    public boolean remove() {
        return  mProvider != null && mProvider.remove(new long[]{mMangaInfo.hashCode()});
    }
}
