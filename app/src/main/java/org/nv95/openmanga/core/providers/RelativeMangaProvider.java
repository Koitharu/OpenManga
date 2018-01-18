package org.nv95.openmanga.core.providers;

import org.nv95.openmanga.core.models.MangaHeader;

import java.util.ArrayList;

/**
 * Created by koitharu on 17.01.18.
 */

public interface RelativeMangaProvider {

	ArrayList<MangaHeader> getRelativeManga(MangaHeader manga);
}
