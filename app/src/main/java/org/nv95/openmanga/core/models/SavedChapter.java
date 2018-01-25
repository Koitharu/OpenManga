package org.nv95.openmanga.core.models;

import android.os.Parcel;
import android.support.annotation.NonNull;

/**
 * Created by koitharu on 25.01.18.
 */

public class SavedChapter extends MangaChapter {

	public final long mangaId;

	public SavedChapter(String name, int number, String url, String provider, long mangaId) {
		super(name, number, url, provider);
		this.mangaId = mangaId;
	}

	public SavedChapter(long id, String name, int number, String url, String provider, long mangaId) {
		super(id, name, number, url, provider);
		this.mangaId = mangaId;
	}

	public static final Creator<SavedChapter> CREATOR = new Creator<SavedChapter>() {
		@Override
		public SavedChapter createFromParcel(Parcel in) {
			return new SavedChapter(in);
		}

		@Override
		public SavedChapter[] newArray(int size) {
			return new SavedChapter[size];
		}
	};

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		super.writeToParcel(dest, flags);
		dest.writeLong(mangaId);
	}

	protected SavedChapter(Parcel in) {
		super(in);
		mangaId = in.readLong();
	}

	@NonNull
	public static SavedChapter from(MangaChapter chapter, long mangaId) {
		return new SavedChapter(
				chapter.id,
				chapter.name,
				chapter.number,
				chapter.url,
				chapter.provider,
				mangaId
		);
	}
}
