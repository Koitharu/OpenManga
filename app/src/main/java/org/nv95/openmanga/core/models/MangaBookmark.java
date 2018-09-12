package org.nv95.openmanga.core.models;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.Nullable;

/**
 * Created by koitharu on 21.12.17.
 */

public final class MangaBookmark implements Parcelable, UniqueObject {

	public final long id;
	public final MangaHeader manga;
	public final long chapterId;
	public final long pageId;
	public final long createdAt;

	public MangaBookmark(long id, MangaHeader manga, long chapterId, long pageId, long createdAt) {
		this.id = id;
		this.manga = manga;
		this.chapterId = chapterId;
		this.pageId = pageId;
		this.createdAt = createdAt;
	}

	public MangaBookmark(MangaHeader manga, long chapterId, long pageId, long createdAt) {
		this.id = pageId + 1; //magic
		this.manga = manga;
		this.chapterId = chapterId;
		this.pageId = pageId;
		this.createdAt = createdAt;
	}

	protected MangaBookmark(Parcel in) {
		id = in.readLong();
		manga = new MangaHeader(in);
		chapterId = in.readLong();
		pageId = in.readLong();
		createdAt = in.readLong();
	}

	public static final Creator<MangaBookmark> CREATOR = new Creator<MangaBookmark>() {
		@Override
		public MangaBookmark createFromParcel(Parcel in) {
			return new MangaBookmark(in);
		}

		@Override
		public MangaBookmark[] newArray(int size) {
			return new MangaBookmark[size];
		}
	};

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeLong(id);
		manga.writeToParcel(dest, flags);
		dest.writeLong(chapterId);
		dest.writeLong(pageId);
		dest.writeLong(createdAt);
	}

	@Override
	public long getId() {
		return id;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		MangaBookmark bookmark = (MangaBookmark) o;

		return id == bookmark.id;
	}

	@Override
	public int hashCode() {
		return (int) (id ^ (id >>> 32));
	}

	public Bundle toBundle() {
		final Bundle bundle = new Bundle(5);
		bundle.putLong("id", id);
		bundle.putBundle("manga", manga.toBundle());
		bundle.putLong("chapter_id", chapterId);
		bundle.putLong("page_id", pageId);
		bundle.putLong("created_at", createdAt);
		return bundle;
	}

	@Nullable
	public static MangaBookmark from(Bundle bundle) {
		if (bundle.containsKey("bookmark")) {
			return bundle.getParcelable("bookmark");
		} else return new MangaBookmark(
				bundle.getLong("id"),
				MangaHeader.from(bundle.getBundle("manga")),
				bundle.getLong("chapter_id"),
				bundle.getLong("page_id"),
				bundle.getLong("created_at")
		);
	}
}
