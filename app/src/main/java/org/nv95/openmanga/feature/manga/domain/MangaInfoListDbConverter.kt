package org.nv95.openmanga.feature.manga.domain

import android.database.Cursor
import org.koin.core.KoinComponent
import org.koin.core.get
import org.nv95.openmanga.feature.manga.domain.MangaInfo.STATUS_UNKNOWN
import org.nv95.openmanga.lists.MangaList
import org.nv95.openmanga.providers.LocalMangaProvider
import org.nv95.openmanga.providers.MangaProvider
import org.nv95.openmanga.providers.NewChaptersProvider


class MangaInfoListDbConverter : KoinComponent {

    fun convert(cursor: Cursor): MangaList {

        val updates = NewChaptersProvider.getInstance(get()).lastUpdates

        val list = MangaList()

        if (cursor.moveToFirst()) {
            do {
                val manga = MangaInfo()
                manga.id = cursor.getInt(0)
                manga.name = cursor.getString(1)
                manga.subtitle = cursor.getString(2)
                manga.genres = cursor.getString(3)
                manga.preview = cursor.getString(4)
                manga.path = cursor.getString(5)
                try {
                    manga.provider = Class.forName(cursor.getString(6)) as Class<out MangaProvider>
                } catch (e: ClassNotFoundException) {
                    manga.provider = LocalMangaProvider::class.java
                }

                manga.status = STATUS_UNKNOWN
                manga.rating = cursor.getInt(7).toByte()
                manga.extra = if (updates.containsKey(manga.id))
                    "+" + updates.get(manga.id)!!
                else
                    null
                list += manga
            } while (cursor.moveToNext())
        }

        return list
    }
}