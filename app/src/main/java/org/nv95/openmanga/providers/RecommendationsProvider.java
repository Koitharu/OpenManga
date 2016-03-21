package org.nv95.openmanga.providers;

import android.content.Context;
import android.support.annotation.NonNull;

import org.nv95.openmanga.R;
import org.nv95.openmanga.items.MangaInfo;
import org.nv95.openmanga.items.MangaPage;
import org.nv95.openmanga.items.MangaSummary;
import org.nv95.openmanga.lists.MangaList;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

/**
 * Created by nv95 on 21.03.16.
 */
public class RecommendationsProvider extends MangaProvider {
    private static boolean features[] = {true, false, false, false, false};
    @NonNull
    private static WeakReference<RecommendationsProvider> instanceReference = new WeakReference<>(null);
    private final MangaProviderManager mProviderManager;
    private final Context mContext;

    public RecommendationsProvider(Context context) {
        mContext = context;
        mProviderManager = new MangaProviderManager(context);
    }

    public static RecommendationsProvider getInstacne(Context context) {
        RecommendationsProvider instance = instanceReference.get();
        if (instance == null) {
            instance = new RecommendationsProvider(context);
            instanceReference = new WeakReference<>(instance);
        }
        return instance;
    }

    @Override
    public MangaList getList(int page, int sort, int genre) throws Exception {
        final ArrayList<MangaProviderManager.ProviderSumm> providers = mProviderManager.getEnabledProviders();
        final MangaList mangas = new MangaList();
        final Random random = new Random();
        final int groupCount = Math.min(providers.size(), 4);
        final int groupSize = 20 / groupCount;
        MangaProvider provider;
        MangaList tempList;
        for (int i=0; i<groupCount; i++) {
            try {
                provider = providers.get(i).instance();
                tempList = provider.getList(random.nextInt(10), 0, 0);
                Collections.shuffle(tempList);
                tempList = tempList.subList(groupSize);
                mangas.addAll(tempList);
            } catch (Exception e) {
                continue;
            }
        }
        Collections.shuffle(mangas);
        return mangas;
    }

    @Override
    public MangaSummary getDetailedInfo(MangaInfo mangaInfo) {
        return null;
    }

    @Override
    public ArrayList<MangaPage> getPages(String readLink) {
        return null;
    }

    @Override
    public String getPageImage(MangaPage mangaPage) {
        return null;
    }

    @Override
    public String getName() {
        return mContext.getString(R.string.action_recommendations);
    }

    @Override
    public boolean hasFeature(int feature) {
        return features[feature];
    }
}
