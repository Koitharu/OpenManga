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
    private final Context context;
    private final ArrayList<ProviderSumm> enabledProviders;

    public MangaProviderManager(Context context) {
        this.context = context;
        enabledProviders = new ArrayList<>();
        update();
    }

    public static void SetSort(Context context, MangaProvider provider, int sort) {
        context.getSharedPreferences("sort", Context.MODE_PRIVATE)
                .edit()
                .putInt(provider.getName(), sort)
                .commit();
    }

    public static int GetSort(Context context, MangaProvider provider) {
        return context.getSharedPreferences("sort", Context.MODE_PRIVATE).getInt(provider.getName(), 0);
    }

    public void update() {
        enabledProviders.clear();
        SharedPreferences prefs = context.getSharedPreferences("providers", Context.MODE_PRIVATE);
        for (ProviderSumm o : providers) {
            if (prefs.getBoolean(o.name, true)) {
                enabledProviders.add(o);
            }
        }
    }

    public int indexOf(ProviderSumm providerSumm) {
        for (int i = 0; i < enabledProviders.size(); i++) {
            if (enabledProviders.get(i).equals(providerSumm)) {
                return i;
            }
        }
        return -1;
    }

    public MangaProvider getMangaProvider(int index) {
        try {
            switch (index) {
                case PROVIDER_LOCAL:
                    return LocalMangaProvider.getInstacne(context);
                case PROVIDER_FAVOURITES:
                    return FavouritesProvider.getInstacne(context);
                case PROVIDER_HISTORY:
                    return HistoryProvider.getInstacne(context);
                case PROVIDER_RECOMMENDATIONS:
                    return RecommendationsProvider.getInstacne(context);
                default:
                    return (MangaProvider) enabledProviders.get(index).aClass.newInstance();
            }
        } catch (Exception e) {
            FileLogger.getInstance().report(e);
            return null;
        }
    }

    public String[] getNames() {
        String[] res = new String[enabledProviders.size()];
        for (int i = 0; i < res.length; i++) {
            res[i] = enabledProviders.get(i).name;
        }
        return res;
    }

    public void setProviderEnabled(String name, boolean enabled) {
        context.getSharedPreferences("providers", Context.MODE_PRIVATE).edit().putBoolean(name, enabled).apply();
    }

    public boolean isProviderEnabled(String name) {
        return context.getSharedPreferences("providers", Context.MODE_PRIVATE).getBoolean(name, true);
    }

    public ArrayList<ProviderSumm> getEnabledProviders() {
        return enabledProviders;
    }

    public ProviderSelectAdapter getAdapter() {
        return new ProviderSelectAdapter();
    }

    public enum Language {EN, RU, TR, MULTI}

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
            inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            summs = context.getResources().getStringArray(R.array.provider_summs);
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
