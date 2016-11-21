package org.nv95.openmanga.providers.staff;

import org.nv95.openmanga.R;
import org.nv95.openmanga.providers.DesuMeProvider;
import org.nv95.openmanga.providers.EHentaiProvider;
import org.nv95.openmanga.providers.HentaiBoxProvider;
import org.nv95.openmanga.providers.HentaichanProvider;
import org.nv95.openmanga.providers.MangaFoxProvider;
import org.nv95.openmanga.providers.MangaReaderProvider;
import org.nv95.openmanga.providers.MangaTownProvider;
import org.nv95.openmanga.providers.MangachanProvider;
import org.nv95.openmanga.providers.MintMangaProvider;
import org.nv95.openmanga.providers.PuzzmosProvider;
import org.nv95.openmanga.providers.ReadmangaRuProvider;
import org.nv95.openmanga.providers.SelfmangaRuProvider;
import org.nv95.openmanga.providers.YaoiChanProvider;

/**
 * Created by nv95 on 27.07.16.
 */

public class Providers {

    private static final ProviderSummary[] mAllProviders = {
            new ProviderSummary(0, "ReadManga", ReadmangaRuProvider.class, Languages.RU, R.xml.pref_readmanga),
            new ProviderSummary(1, "MintManga", MintMangaProvider.class, Languages.RU, R.xml.pref_readmanga),
            new ProviderSummary(2, "Манга-тян", MangachanProvider.class, Languages.RU, 0),
            new ProviderSummary(3, "Desu.me", DesuMeProvider.class, Languages.RU, 0),
            new ProviderSummary(4, "SelfManga", SelfmangaRuProvider.class, Languages.RU, 0),
            new ProviderSummary(5, "MangaFox", MangaFoxProvider.class, Languages.EN, 0),
            new ProviderSummary(6, "MangaTown", MangaTownProvider.class, Languages.EN, 0),
            new ProviderSummary(7, "MangaReader", MangaReaderProvider.class, Languages.EN, 0),
            new ProviderSummary(8, "E-Hentai", EHentaiProvider.class, Languages.MULTI, R.xml.pref_ehentai),
            new ProviderSummary(9, "PuzzManga", PuzzmosProvider.class, Languages.TR, 0),
            new ProviderSummary(10, "Яой-тян", YaoiChanProvider.class, Languages.RU, 0),
            new ProviderSummary(11, "HentaiBox", HentaiBoxProvider.class, Languages.MULTI, 0),
            new ProviderSummary(12, "Хентай-тян", HentaichanProvider.class, Languages.RU, 0)
    };

    public static ProviderSummary[] getAll() {
        return mAllProviders;
    }

    public static ProviderSummary getById(int id) {
        return id < 0 || id >= mAllProviders.length ? null : mAllProviders[id];
    }

    public static int getCount() {
        return mAllProviders.length;
    }
}
