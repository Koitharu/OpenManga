package org.nv95.openmanga.utils.diffutil

import org.nv95.openmanga.feature.manga.domain.MangaInfo

class MangaInfoDiffUtill(
		oldItems: List<MangaInfo>,
		newItems: List<MangaInfo>
) : BaseDiffUtils(oldItems, newItems) {

	override fun areItemsTheSame(item1: Any, item2: Any): Boolean {
		return item1 is MangaInfo && item2 is MangaInfo
				&& item1.id == item2.id
	}

	override fun areContentsTheSame(item1: Any, item2: Any): Boolean {
		return item1 is MangaInfo && item2 is MangaInfo
				&& item1.id == item2.id
				&& item1.extra == item2.extra
				&& item1.preview == item2.preview
				&& item1.status == item2.status
	}

}