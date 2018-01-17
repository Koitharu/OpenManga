package org.nv95.openmanga.core.models;

import android.os.Parcel;

/**
 * Created by koitharu on 21.12.17.
 */

public final class MangaDetails extends MangaHeader {

	public final String description;
	public final String cover;
	public final String author;
	public final MangaChaptersList chapters;

	public MangaDetails(String name, String summary, String genres, String url, String thumbnail, String provider, int status, byte rating, String description, String cover, String author, MangaChaptersList chapters) {
		super(name, summary, genres, url, thumbnail, provider, status, rating);
		this.description = description;
		this.cover = cover;
		this.author = author;
		this.chapters = chapters;
	}

	public MangaDetails(long id, String name, String summary, String genres, String url, String thumbnail, String provider, int status, byte rating, String description, String cover, String author, MangaChaptersList chapters) {
		super(id, name, summary, genres, url, thumbnail, provider, status, rating);
		this.description = description;
		this.cover = cover;
		this.author = author;
		this.chapters = chapters;
	}

	public MangaDetails(MangaHeader header, String description, String cover, String author) {
		super(header.id, header.name, header.summary, header.genres, header.url, header.thumbnail, header.provider, header.status, header.rating);
		this.description = description;
		this.cover = cover;
		this.author = author;
		this.chapters = new MangaChaptersList();
	}

	protected MangaDetails(Parcel in) {
		super(in);
		description = in.readString();
		cover = in.readString();
		author = in.readString();
		chapters = new MangaChaptersList();
		in.readTypedList(chapters, MangaChapter.CREATOR);
	}

	public static final Creator<MangaDetails> CREATOR = new Creator<MangaDetails>() {
		@Override
		public MangaDetails createFromParcel(Parcel in) {
			return new MangaDetails(in);
		}

		@Override
		public MangaDetails[] newArray(int size) {
			return new MangaDetails[size];
		}
	};

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel parcel, int i) {
		super.writeToParcel(parcel, i);
		parcel.writeString(description);
		parcel.writeString(cover);
		parcel.writeString(author);
		parcel.writeTypedList(chapters);
	}
}
