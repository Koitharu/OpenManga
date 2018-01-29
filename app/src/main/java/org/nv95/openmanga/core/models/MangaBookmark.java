package org.nv95.openmanga.core.models;

import android.os.Parcel;
import android.os.Parcelable;

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
		manga = in.readParcelable(MangaHeader.class.getClassLoader());
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
		dest.writeParcelable(manga, flags);
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
}
