package org.nv95.openmanga.providers.staff;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import androidx.annotation.Nullable;

import org.nv95.openmanga.providers.EHentaiProvider;
import org.nv95.openmanga.providers.FavouritesProvider;
import org.nv95.openmanga.providers.HistoryProvider;
import org.nv95.openmanga.providers.LocalMangaProvider;
import org.nv95.openmanga.providers.MangaProvider;
import org.nv95.openmanga.providers.MintMangaProvider;
import org.nv95.openmanga.providers.ReadmangaRuProvider;
import org.nv95.openmanga.providers.RecommendationsProvider;
import org.nv95.openmanga.utils.FileLogger;

import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import okhttp3.Request;
import timber.log.Timber;

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
    @SuppressWarnings("ConstantConditions")
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
                    return Providers.getById(index).aClass.getDeclaredConstructor(Context.class).newInstance(mContext);
            }
        } catch (Exception e) {
            FileLogger.getInstance().report(e);
            return null;
        }
    }

    @Nullable
    public static MangaProvider instanceProvider(Context context, String className) {
        try {
            return instanceProvider(context, (Class<? extends MangaProvider>) Class.forName(className));
        } catch (Exception e) {
            return null;
        }
    }

    @Nullable
    public static MangaProvider instanceProvider(Context context, Class<? extends MangaProvider> aClass) {
        try {
            Method m = aClass.getMethod("getInstance", Context.class);
            return (MangaProvider) m.invoke(null, context);
        } catch (Exception ignored) {
        }
        try {
            return aClass.getDeclaredConstructor(Context.class).newInstance(context);
        } catch (Exception ignored) {
        }
        return null;
    }

    public static boolean needConnectionFor(MangaProvider provider) {
        return !(provider instanceof LocalMangaProvider || provider instanceof FavouritesProvider || provider instanceof HistoryProvider);
    }

    public static boolean checkConnection(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm.getActiveNetworkInfo();
        return ni != null && ni.isAvailable() && ni.isConnected();
    }

    public static boolean isWlan(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm.getActiveNetworkInfo();
        return ni != null && ni.getType() == ConnectivityManager.TYPE_WIFI;
    }

    public MangaProvider instanceProvider(Class<? extends MangaProvider> aClass) {
        return instanceProvider(mContext, aClass);
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
		Timber.d(ids);
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


    public List<ProviderSummary> getDisabledOrderedProviders() {
        return mProviders.subList(getProvidersCount(), mProviders.size());
    }

    public boolean hasDisabledProviders() {
        int count = mContext.getSharedPreferences("providers", Context.MODE_PRIVATE).getInt("count", Providers.getCount());
        return count < mProviders.size();
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

    public static void prepareRequest(String url, Request.Builder request, @Nullable Class<? extends MangaProvider> provider) {
        if (url.contains("exhentai.org") && EHentaiProvider.isAuthorized()) {
            request.addHeader("Cookie", EHentaiProvider.getCookie());
        } else if (ReadmangaRuProvider.class.equals(provider)) {
            request.addHeader("Referer", "http://readmanga.me");
        } else if (MintMangaProvider.class.equals(provider)) {
            request.addHeader("Referer", "http://mintmanga.com/");
        }
    }

    @Deprecated
    public static void prepareConnection(HttpURLConnection connection, @Nullable Class<? extends MangaProvider> provider) {
        URL url = connection.getURL();
        switch (url.getHost().toLowerCase()) {
            case "exhentai.org":
                if (EHentaiProvider.isAuthorized()) {
                    connection.addRequestProperty("Cookie", EHentaiProvider.getCookie());
                }
                break;
        }
        if (ReadmangaRuProvider.class.equals(provider)) {
            connection.addRequestProperty("Referer", "http://readmanga.me");
        } else if (MintMangaProvider.class.equals(provider)) {
            connection.addRequestProperty("Referer", "http://mintmanga.com/");
        }
    }
}
