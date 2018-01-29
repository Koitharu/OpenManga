package org.nv95.openmanga.core.models;

import android.os.Parcel;

import org.nv95.openmanga.core.RecommendationsCategory;

/**
 * Created by koitharu on 29.01.18.
 */

public final class MangaRecommendation extends MangaHeader {

	@RecommendationsCategory
	public final int category;

	public MangaRecommendation(long id, String name, String summary, String genres, String url, String thumbnail, String provider, int status, short rating, int category) {
		super(id, name, summary, genres, url, thumbnail, provider, status, rating);
		this.category = category;
	}

	protected MangaRecommendation(Parcel in) {
		super(in);
		category = in.readInt();
	}

	public MangaRecommendation(MangaHeader mangaHeader, @RecommendationsCategory int category) {
		this(
				mangaHeader.id,
				mangaHeader.name,
				mangaHeader.summary,
				mangaHeader.genres,
				mangaHeader.url,
				mangaHeader.thumbnail,
				mangaHeader.provider,
				mangaHeader.status,
				mangaHeader.rating,
				category
		);
	}

	@Override
	public void writeToParcel(Parcel parcel, int i) {
		super.writeToParcel(parcel, i);
		parcel.writeInt(category);
	}
}
