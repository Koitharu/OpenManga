package org.nv95.openmanga.providers;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.util.Set;

/**
 * Created by nv95 on 30.09.15.
 */
public class MangaProviderManager {
    public static final int FUTURE_MULTIPAGE = 0;
    public static final int FEAUTURE_SEARCH = 1;
    public static final int FEAUTURE_REMOVE = 2;
    private Context context;
    Set<String> providers;
    public static String[] allProviders = {"ReadManga", "AdultManga", "E-Hentai"};

    public class ProviderSumm {
        String name;
        Class<?> aClass;
    }

    public MangaProviderManager(Context context) {
        this.context = context;
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        providers = sharedPrefs.getStringSet("providers", null);
    }

    private static final Class<?> mangaProviders[] = {
            ReadmangaRuProvider.class
            , AdultmangaRuProvider.class,
            EHentaiProvider.class
        };

    public MangaProvider getMangaProvider(int index) {
        try {
            return (MangaProvider) mangaProviders[index].newInstance();
        } catch (Exception e) {
            return null;
        }
    }

    public String[] getNames() {
        //return providers.toArray(new String[providers.size()]);
        return allProviders;
    }

}
