package org.nv95.openmanga.providers;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.ArrayList;

/**
 * Created by nv95 on 30.09.15.
 */
public class MangaProviderManager {
    public static final int FUTURE_MULTIPAGE = 0;
    public static final int FEAUTURE_SEARCH = 1;
    public static final int FEAUTURE_REMOVE = 2;
    private Context context;
    ArrayList<ProviderSumm> providers;
    public static String[] allProviders = {"ReadManga", "AdultManga", "E-Hentai", "MangaTown", "MangaReader"};
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

}
