package org.nv95.openmanga.providers.staff;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.Nullable;
import android.util.Log;

import org.nv95.openmanga.providers.FavouritesProvider;
import org.nv95.openmanga.providers.HistoryProvider;
import org.nv95.openmanga.providers.LocalMangaProvider;
import org.nv95.openmanga.providers.MangaProvider;
import org.nv95.openmanga.providers.RecommendationsProvider;
import org.nv95.openmanga.utils.FileLogger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by nv95 on 30.09.15.
 */
public class MangaProviderManager {

    public static final int PROVIDER_LOCAL = -4;
    public static final int PROVIDER_RECOMMENDATIONS = -3;
    public static final int PROVIDER_FAVOURITES = -2;
    public static final int PROVIDER_HISTORY = -1;

    private final Context mContext;
    private ArrayList<ProviderSummary> mProviders;

    public MangaProviderManager(Context context) {
        mContext = context;
        update();
    }

    public void update() {
        mProviders = new ArrayList<>();
        String ids =  mContext.getSharedPreferences("providers", Context.MODE_PRIVATE).getString("ordered", null);
        if (ids == null) {
            Collections.addAll(mProviders, Providers.getAll());
        } else {
            String[] ss = ids.split("\\|");
            int i;
            int count = Providers.getCount();
            for (String o : ss) {
                i = Integer.parseInt(o);
                if (i < count) {
                    mProviders.add(Providers.getById(i));
                }
            }
            for (i=ss.length;i<count;i++) {
                mProviders.add(Providers.getById(i));
            }
        }
    }

    /**
     *
     * @param index < 0 for standard providers, otherwise - id
     * @return provider class instance
     */
    public MangaProvider getProviderById(int index) {
        try {
            switch (index) {
                case PROVIDER_LOCAL:
                    return LocalMangaProvider.getInstance(mContext);
                case PROVIDER_FAVOURITES:
                    return FavouritesProvider.getInstance(mContext);
                case PROVIDER_HISTORY:
                    return HistoryProvider.getInstance(mContext);
                case PROVIDER_RECOMMENDATIONS:
                    return RecommendationsProvider.getInstance(mContext);
                default:
                    return Providers.getById(index).aClass.newInstance();
            }
        } catch (Exception e) {
            FileLogger.getInstance().report(e);
            return null;
        }
    }

    @Nullable
    public static MangaProvider instanceNewProvider(String className) {
        try {
            Class aClass = Class.forName(className);
            return (MangaProvider) aClass.newInstance();
        } catch (Exception e) {
            FileLogger.getInstance().report(e);
            return null;
        }
    }

    public static boolean needConnectionFor(MangaProvider provider) {
        return !(provider instanceof LocalMangaProvider || provider instanceof FavouritesProvider || provider instanceof HistoryProvider);
    }

    public static boolean checkConnection(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm.getActiveNetworkInfo();
        return ni != null && ni.isAvailable() && ni.isConnected();
    }

    public MangaProvider instanceProvider(Class<? extends MangaProvider> aClass) {
        return instanceProvider(mContext, aClass);
    }

    @Nullable
    public static MangaProvider instanceProvider(Context context, Class<? extends MangaProvider> aClass) {
        if (aClass.equals(LocalMangaProvider.class)) {
            return LocalMangaProvider.getInstance(context);
        } else if (aClass.equals(RecommendationsProvider.class)) {
            return RecommendationsProvider.getInstance(context);
        } else if (aClass.equals(FavouritesProvider.class)) {
            return FavouritesProvider.getInstance(context);
        } else if (aClass.equals(HistoryProvider.class)) {
            return HistoryProvider.getInstance(context);
        } else {
            try {
                return aClass.newInstance();
            } catch (Exception e) {
                FileLogger.getInstance().report(e);
            }
        }
        return null;
    }

    public static void saveSortOrder(Context context, MangaProvider provider, int sort) {
        context.getSharedPreferences("sort", Context.MODE_PRIVATE)
                .edit()
                .putInt(provider.getName(), sort)
                .apply();
    }

    public static int restoreSortOrder(Context context, MangaProvider provider) {
        return context.getSharedPreferences("sort", Context.MODE_PRIVATE).getInt(provider.getName(), 0);
    }

    //-------------------------------------------

    public void updateOrder(List<ProviderSummary> providers) {
        String ids = "";
        for (ProviderSummary o : providers) {
            ids = ids + o.id + "|";
        }
        Log.d("SORT", ids);
        mContext.getSharedPreferences("providers", Context.MODE_PRIVATE)
                .edit()
                .putString("ordered", ids.substring(0, ids.length() - 1))
                .apply();
    }

    public List<ProviderSummary> getOrderedProviders() {
        return mProviders;
    }

    public List<ProviderSummary> getEnabledOrderedProviders() {
        return mProviders.subList(0, getProvidersCount());
    }

    public int getProvidersCount() {
        return Math.min(
                mContext.getSharedPreferences("providers", Context.MODE_PRIVATE).getInt("count", Providers.getCount()),
                mProviders.size()
        );
    }

    public void setProvidersCount(int count) {
        mContext.getSharedPreferences("providers", Context.MODE_PRIVATE)
                .edit()
                .putInt("count", count)
                .apply();
    }
}
