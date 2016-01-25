package org.nv95.openmanga.lists;

import org.nv95.openmanga.items.MangaInfo;

/**
 * Created by nv95 on 30.09.15.
 */
public class MangaList extends PagedList<MangaInfo> {
  public static MangaList Empty() {
    return new MangaList();
  }

}
