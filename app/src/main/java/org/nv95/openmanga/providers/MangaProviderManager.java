package org.nv95.openmanga.providers;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.nv95.openmanga.utils.FileLogger;

import java.util.ArrayList;
import java.util.Locale;

/**
 * Created by nv95 on 30.09.15.
 */
public class MangaProviderManager {

    public static final int CATEGORY_LOCAL = 0;
    public static final int CATEGORY_FAVOURITES = 2;
    public static final int CATEGORY_HISTORY = 3;

    public static final int PROVIDER_LOCAL = -4;
    public static final int PROVIDER_RECOMMENDATIONS = -3;
    public static final int PROVIDER_FAVOURITES = -2;
    public static final int PROVIDER_HISTORY = -1;

    public static final int FUTURE_MULTIPAGE = 0;
    public static final int FEAUTURE_SEARCH = 1;
    public static final int FEAUTURE_REMOVE = 2;
    public static final int FEAUTURE_SORT = 3;
    public static final int FEAUTURE_GENRES = 4;

    public static final ProviderSumm[] providers = {
            new ProviderSumm("ReadManga", ReadmangaRuProvider.class, Languages.RU),
            new ProviderSumm("MintManga", MintMangaProvider.class, Languages.RU),
            new ProviderSumm("Манга-тян", MangachanProvider.class, Languages.RU),
            new ProviderSumm("Desu.me", DesuMeProvider.class, Languages.RU),
            new ProviderSumm("MangaFox", MangaFoxProvider.class, Languages.EN),
            new ProviderSumm("MangaTown", MangaTownProvider.class, Languages.EN),
            new ProviderSumm("MangaReader", MangaReaderProvider.class, Languages.EN),
            new ProviderSumm("E-Hentai", EHentaiProvider.class, Languages.MULTI),
            new ProviderSumm("PuzzManga", PuzzmosProvider.class, Languages.TR)
    };

    private final Context mContext;
    private final ArrayList<ProviderSumm> mEnabledProviders;

    public MangaProviderManager(Context context) {
        mContext = context;
        mEnabledProviders = new ArrayList<>();
        update();
    }

    public static void setSort(Context context, MangaProvider provider, int sort) {
        context.getSharedPreferences("sort", Context.MODE_PRIVATE)
                .edit()
                .putInt(provider.getName(), sort)
                .apply();
    }

    public static int getSort(Context context, MangaProvider provider) {
        return context.getSharedPreferences("sort", Context.MODE_PRIVATE).getInt(provider.getName(), 0);
    }

    public void update() {
        mEnabledProviders.clear();
        SharedPreferences prefs = mContext.getSharedPreferences("providers", Context.MODE_PRIVATE);
        for (ProviderSumm o : providers) {
            if (prefs.getBoolean(o.name, true)) {
                mEnabledProviders.add(o);
            }
        }
    }

    public int indexOf(ProviderSumm providerSumm) {
        for (int i = 0; i < mEnabledProviders.size(); i++) {
            if (mEnabledProviders.get(i).equals(providerSumm)) {
                return i;
            }
        }
        return -1;
    }

    public MangaProvider getMangaProvider(int index) {
        try {
            switch (index) {
                case PROVIDER_LOCAL:
                    return LocalMangaProvider.getInstacne(mContext);
                case PROVIDER_FAVOURITES:
                    return FavouritesProvider.getInstacne(mContext);
                case PROVIDER_HISTORY:
                    return HistoryProvider.getInstacne(mContext);
                case PROVIDER_RECOMMENDATIONS:
                    return RecommendationsProvider.getInstacne(mContext);
                default:
                    return (MangaProvider) mEnabledProviders.get(index).aClass.newInstance();
            }
        } catch (Exception e) {
            FileLogger.getInstance().report(e);
            return null;
        }
    }

    public String[] getNames() {
        String[] res = new String[mEnabledProviders.size()];
        for (int i = 0; i < res.length; i++) {
            res[i] = mEnabledProviders.get(i).name;
        }
        return res;
    }

    @Nullable
    public static MangaProvider createProvider(String className) {
        try {
            Class aClass = Class.forName(className);
            return (MangaProvider) aClass.newInstance();
        } catch (Exception e) {
            FileLogger.getInstance().report(e);
            return null;
        }
    }

    public static void configure(Context context, int language) {
        SharedPreferences.Editor editor = context.getSharedPreferences("providers", Context.MODE_PRIVATE)
                .edit();
        for (ProviderSumm o : providers) {
            editor.putBoolean(o.name, o.lang == language || o.lang == Languages.MULTI);
        }
        editor.apply();
    }

    public void setProviderEnabled(String name, boolean enabled) {
        mContext.getSharedPreferences("providers", Context.MODE_PRIVATE).edit().putBoolean(name, enabled).apply();
    }

    public boolean isProviderEnabled(String name) {
        return mContext.getSharedPreferences("providers", Context.MODE_PRIVATE).getBoolean(name, true);
    }

    public ArrayList<ProviderSumm> getEnabledProviders() {
        return mEnabledProviders;
    }

    public static boolean needConnection(MangaProvider provider) {
        return !(provider instanceof LocalMangaProvider || provider instanceof FavouritesProvider || provider instanceof HistoryProvider);
    }

    public static boolean checkConnection(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm.getActiveNetworkInfo();
        return ni != null && ni.isAvailable() && ni.isConnected();
    }

    public static class ProviderSumm {
        public String name;
        @NonNull
        public Class<?> aClass;
        public int lang;

        public ProviderSumm(String name, @NonNull Class<?> aClass, int lang) {
            this.name = name;
            this.aClass = aClass;
            this.lang = lang;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            ProviderSumm that = (ProviderSumm) o;

            return aClass.equals(that.aClass);

        }

        @Override
        public int hashCode() {
            return aClass.hashCode();
        }

        @Nullable
        public MangaProvider instance() {
            try {
                return (MangaProvider) aClass.newInstance();
            } catch (Exception e) {
                FileLogger.getInstance().report(e);
                return null;
            }
        }
    }

    public static class Languages {

        public static final int EN = 0;
        public static final int RU = 1;
        public static final int JP = 2;
        public static final int TR = 3;
        public static final int MULTI = 4;

        public static int fromLocale(Locale locale) {
            switch (locale.getLanguage()) {
                case "ru":
                case "uk":
                case "be":
                case "sk":
                case "sl":
                case "sr":
                    return RU;
                case "tr":
                    return TR;
                default:
                    return EN;
            }
        }
    }
}
