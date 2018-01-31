package org.nv95.openmanga.core.models;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import java.io.File;

/**
 * Created by koitharu on 23.01.18.
 */

public final class SavedManga extends MangaHeader implements Parcelable {

	public final long createdAt;
	public final String localPath;
	public final String description;
	public final String author;

	public SavedManga(String name, String summary, String genres, String url, String thumbnail, String provider, int status, short rating, long createdAt, String localPath, String description, String author) {
		super(name, summary, genres, url, thumbnail, provider, status, rating);
		this.createdAt = createdAt;
		this.localPath = localPath;
		this.description = description;
		this.author = author;
	}

	public SavedManga(long id, String name, String summary, String genres, String url, String thumbnail, String provider, int status, short rating, long createdAt, String localPath, String description, String author) {
		super(id, name, summary, genres, url, thumbnail, provider, status, rating);
		this.createdAt = createdAt;
		this.localPath = localPath;
		this.description = description;
		this.author = author;
	}

	public static final Creator<SavedManga> CREATOR = new Creator<SavedManga>() {
		@Override
		public SavedManga createFromParcel(Parcel in) {
			return new SavedManga(in);
		}

		@Override
		public SavedManga[] newArray(int size) {
			return new SavedManga[size];
		}
	};

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel parcel, int i) {
		super.writeToParcel(parcel, i);
		parcel.writeLong(createdAt);
		parcel.writeString(localPath);
		parcel.writeString(description);
		parcel.writeString(author);
	}

	protected SavedManga(Parcel in) {
		super(in);
		createdAt = in.readLong();
		localPath = in.readString();
		description = in.readString();
		author = in.readString();
	}

	@NonNull
	public static SavedManga from(@NonNull MangaDetails other, @NonNull File localPath) {
		return new SavedManga(
				other.id,
				other.name,
				other.summary,
				other.genres,
				other.url,
				other.thumbnail,
				other.provider,
				other.status,
				other.rating,
				System.currentTimeMillis(),
				localPath.getPath(),
				other.description,
				other.author
		);
	}
}
