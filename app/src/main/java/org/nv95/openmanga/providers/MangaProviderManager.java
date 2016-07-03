package org.nv95.openmanga.providers;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import org.nv95.openmanga.R;
import org.nv95.openmanga.utils.FileLogger;

import java.util.ArrayList;

/**
 * Created by nv95 on 30.09.15.
 */
public class MangaProviderManager {

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
            new ProviderSumm("ReadManga", ReadmangaRuProvider.class, Language.RU),
            new ProviderSumm("MintManga", MintMangaProvider.class, Language.RU),
            new ProviderSumm("Манга-тян", MangachanProvider.class, Language.RU),
            new ProviderSumm("Desu.me", DesuMeProvider.class, Language.RU),
            new ProviderSumm("MangaFox", MangaFoxProvider.class, Language.EN),
            new ProviderSumm("E-Hentai", EHentaiProvider.class, Language.MULTI),
            new ProviderSumm("MangaTown", MangaTownProvider.class, Language.EN),
            new ProviderSumm("MangaReader", MangaReaderProvider.class, Language.EN),
            new ProviderSumm("PuzzManga", PuzzmosProvider.class, Language.TR)
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

    public void setProviderEnabled(String name, boolean enabled) {
        mContext.getSharedPreferences("providers", Context.MODE_PRIVATE).edit().putBoolean(name, enabled).apply();
    }

    public boolean isProviderEnabled(String name) {
        return mContext.getSharedPreferences("providers", Context.MODE_PRIVATE).getBoolean(name, true);
    }

    public ArrayList<ProviderSumm> getEnabledProviders() {
        return mEnabledProviders;
    }

    public ProviderSelectAdapter getAdapter() {
        return new ProviderSelectAdapter();
    }

    public static boolean needConnection(MangaProvider provider) {
        return !(provider instanceof LocalMangaProvider || provider instanceof FavouritesProvider || provider instanceof HistoryProvider);
    }

    private enum Language {EN, RU, TR, MULTI, JP}

    public static class ProviderSumm {
        public String name;
        @NonNull
        public Class<?> aClass;
        public Language lang;

        public ProviderSumm(String name, @NonNull Class<?> aClass, Language lang) {
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

    public class ProviderSelectAdapter extends BaseAdapter {
        private LayoutInflater inflater;
        private String[] summs;

        public ProviderSelectAdapter() {
            inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            summs = mContext.getResources().getStringArray(R.array.provider_summs);
        }

        @Override
        public int getCount() {
            return providers.length;
        }

        @Override
        public ProviderSumm getItem(int position) {
            return providers[position];
        }

        @Override
        public long getItemId(int position) {
            return providers[position].hashCode();
        }

        @SuppressLint("InflateParams")
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.item_adapter_checkable, null);
            }
            ProviderSumm prov = getItem(position);
            ((TextView) convertView.findViewById(android.R.id.text1)).setText(prov.name);
            ((TextView) convertView.findViewById(android.R.id.text2)).setText(summs[prov.lang.ordinal()]);
            ((CheckBox) convertView.findViewById(android.R.id.checkbox)).setChecked(isProviderEnabled(prov.name));
            return convertView;
        }
    }

}
