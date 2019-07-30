package org.nv95.openmanga.feature.newchapter.view

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.*
import org.nv95.openmanga.core.sources.ConnectionSource
import org.nv95.openmanga.feature.manga.domain.MangaInfo
import org.nv95.openmanga.lists.MangaList
import org.nv95.openmanga.providers.FavouritesProvider
import org.nv95.openmanga.providers.NewChaptersProvider
import kotlin.coroutines.CoroutineContext


class NewChapterViewModel(
		private val favouritesProvider: FavouritesProvider,
		private val newChaptersProvider: NewChaptersProvider,
		private val connectionSource: ConnectionSource,
		private val mainContext: CoroutineContext
) : ViewModel() {

	/**
	 * Can attach listener for change data
	 */
	val mangaList = MutableLiveData<List<MangaInfo>>()

	/**
	 * For fist open page
	 * If db data not exists, then load from network new ones
	 *
	 * Or display from db
	 */
	fun firstLoad() {
		CoroutineScope(mainContext + Dispatchers.IO).launch {

			val updates = newChaptersProvider.lastUpdates

			if (updates.isEmpty()) {
				load()
			} else {
				displayItems(updates)
			}
		}
	}

	/**
	 * Async check new data only if connection exists
	 * if not, display saved data in db
	 * Using for pull refresh
	 *
	 * Update ui list
	 */
	fun load() {

		CoroutineScope(mainContext + Dispatchers.IO).launch {

			if (connectionSource.isConnectionAvailable()) {
				newChaptersProvider.checkForNewChapters()
			}

			displayItems(newChaptersProvider.lastUpdates)
		}
	}

	/**
	 * Mark as viewed in db and reload list of newed manga
	 *
	 * Update ui list
	 */
	fun markAsViewed(mangaId: Int) {
		CoroutineScope(mainContext + Dispatchers.IO).launch {

			newChaptersProvider.markAsViewed(mangaId)
			val items = mangaList.value?.filter { it.id != mangaId }.orEmpty()
			// set value in main thread
			mangaList.postValue(items)

		}
	}

	/**
	 * Mark all new manga as viewed in db and clear the list
	 *
	 * Update ui list
	 */
	fun markAllAsViewed() {
		newChaptersProvider.markAllAsViewed()
		mangaList.value = MangaList()
	}

	private suspend fun displayItems(updates: Map<Int, Int>) {
		val mangas = favouritesProvider.getList(0, 0, 0)
		withContext(mainContext) {
			mangaList.value = convertWithNewCount(mangas, updates)
		}
	}

	private fun convertWithNewCount(mangas: MangaList, updates: Map<Int, Int>): MangaList {
		return mangas.mapNotNullTo(MangaList()) { mangaInfo ->
			updates[mangaInfo.hashCode()]?.takeIf { it > 0 }?.let { newChapters ->
				mangaInfo.apply {
					extra = "+$newChapters"
				}
			}
		}
	}

}