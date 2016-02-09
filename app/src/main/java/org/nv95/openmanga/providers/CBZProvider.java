package org.nv95.openmanga.providers;

import org.nv95.openmanga.items.MangaInfo;
import org.nv95.openmanga.items.MangaPage;
import org.nv95.openmanga.items.MangaSummary;
import org.nv95.openmanga.lists.MangaList;

import java.io.IOException;
import java.util.ArrayList;
import java.util.zip.ZipFile;

/**
 * Created by nv95 on 05.02.16.
 */
public class CBZProvider extends MangaProvider {

    @Override
    public MangaList getList(int page, int sort, int genre) throws Exception {
        return null;
    }

    @Override
    public MangaSummary getDetailedInfo(MangaInfo mangaInfo) {
        return null;
    }

    @Override
    public ArrayList<MangaPage> getPages(String readLink) {
        try {
            ZipFile zipFile = new ZipFile(readLink);

            zipFile.close();
            return null;
        } catch (IOException e) {
            return null;
        }
    }

    @Override
    public String getPageImage(MangaPage mangaPage) {
        return null;
    }

    @Override
    public String getName() {
        return "";
    }

    @Override
    public boolean hasFeature(int feature) {
        return false;
    }
}
