package org.nv95.openmanga.providers;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import org.nv95.openmanga.R;
import org.nv95.openmanga.utils.ErrorReporter;

import java.util.ArrayList;

/**
 * Created by nv95 on 30.09.15.
 */
public class MangaProviderManager {
    public static final int FUTURE_MULTIPAGE = 0;
    public static final int FEAUTURE_SEARCH = 1;
    public static final int FEAUTURE_REMOVE = 2;
    public static final int FEAUTURE_SORT = 3;
    public static final int FEAUTURE_GENRES = 4;
    private Context context;
    ArrayList<ProviderSumm> providers;
    public static String[] allProviders = {"ReadManga","MintManga", "E-Hentai", "MangaTown", "MangaReader"}; //
    private static final Class<?> mangaProviders[] = {
            ReadmangaRuProvider.class,
            AdultmangaRuProvider.class,
            EHentaiProvider.class,
            MangaTownProvider.class,
            MangaReaderProvider.class
    };

    public class ProviderSumm {
        String name;
        Class<?> aClass;

        public ProviderSumm(String name, Class<?> aClass) {
            this.name = name;
            this.aClass = aClass;
        }
    }

    public MangaProviderManager(Context context) {
        this.context = context;
        providers = new ArrayList<>();
        update();
    }

    public void update() {
        providers.clear();
        SharedPreferences prefs = context.getSharedPreferences("providers", Context.MODE_PRIVATE);
        for (int i=0; i<mangaProviders.length; i++) {
            if (prefs.getBoolean(allProviders[i], true)) {
                providers.add(new ProviderSumm(allProviders[i], mangaProviders[i]));
            }
        }
    }

    public MangaProvider getMangaProvider(int index) {
        try {
            return (MangaProvider) providers.get(index).aClass.newInstance();
        } catch (Exception e) {
            ErrorReporter.getInstance().report(e);
            return null;
        }
    }

    public String[] getNames() {
        String[] res = new String[providers.size()];
        for (int i=0; i<res.length; i++) {
            res[i] = providers.get(i).name;
        }
        return res;
    }

    public void setProviderEnabled(String name, boolean enabled) {
        context.getSharedPreferences("providers", Context.MODE_PRIVATE).edit().putBoolean(name, enabled).apply();
    }

    public boolean isProviderEnabled(String name) {
        return context.getSharedPreferences("providers", Context.MODE_PRIVATE).getBoolean(name, true);
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

    public ProviderSelectAdapter getAdapter() {
        return new ProviderSelectAdapter();
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
            return allProviders.length;
        }

        @Override
        public String getItem(int position) {
            return allProviders[position];
        }

        @Override
        public long getItemId(int position) {
            return allProviders[position].hashCode();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null)
                convertView = inflater.inflate(R.layout.item_adapter_checkable, null);
            ((TextView) convertView.findViewById(android.R.id.text1)).setText(allProviders[position]);
            ((TextView) convertView.findViewById(android.R.id.text2)).setText(summs[position]);
            ((CheckBox) convertView.findViewById(android.R.id.checkbox)).setChecked(isProviderEnabled(allProviders[position]));
            return convertView;
        }
    }

}
