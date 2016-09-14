package org.nv95.openmanga.providers.staff;

import org.nv95.openmanga.providers.DesuMeProvider;
import org.nv95.openmanga.providers.EHentaiProvider;
import org.nv95.openmanga.providers.HentaiBoxProvider;
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
            new ProviderSummary(0, "ReadManga", ReadmangaRuProvider.class, Languages.RU),
            new ProviderSummary(1, "MintManga", MintMangaProvider.class, Languages.RU),
            new ProviderSummary(2, "Манга-тян", MangachanProvider.class, Languages.RU),
            new ProviderSummary(3, "Desu.me", DesuMeProvider.class, Languages.RU),
            new ProviderSummary(4, "SelfManga", SelfmangaRuProvider.class, Languages.RU),
            new ProviderSummary(5, "MangaFox", MangaFoxProvider.class, Languages.EN),
            new ProviderSummary(6, "MangaTown", MangaTownProvider.class, Languages.EN),
            new ProviderSummary(7, "MangaReader", MangaReaderProvider.class, Languages.EN),
            new ProviderSummary(8, "E-Hentai", EHentaiProvider.class, Languages.MULTI),
            new ProviderSummary(9, "PuzzManga", PuzzmosProvider.class, Languages.TR),
            new ProviderSummary(10, "Яой-тян", YaoiChanProvider.class, Languages.RU),
            new ProviderSummary(11, "HentaiBox", HentaiBoxProvider.class, Languages.MULTI)
    };

    public static ProviderSummary[] getAll() {
        return mAllProviders;
    }

    public static ProviderSummary getById(int id) {
        return mAllProviders[id];
    }

    public static int getCount() {
        return mAllProviders.length;
    }
}
