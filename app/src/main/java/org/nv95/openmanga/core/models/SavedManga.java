package org.nv95.openmanga.core.models;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by koitharu on 23.01.18.
 */

public final class SavedManga extends MangaHeader implements Parcelable {

	public final long createdAt;
	public final String localPath;

	public SavedManga(String name, String summary, String genres, String url, String thumbnail, String provider, int status, short rating, long createdAt, String localPath) {
		super(name, summary, genres, url, thumbnail, provider, status, rating);
		this.createdAt = createdAt;
		this.localPath = localPath;
	}

	public SavedManga(long id, String name, String summary, String genres, String url, String thumbnail, String provider, int status, short rating, long createdAt, String localPath) {
		super(id, name, summary, genres, url, thumbnail, provider, status, rating);
		this.createdAt = createdAt;
		this.localPath = localPath;
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
	}

	protected SavedManga(Parcel in) {
		super(in);
		createdAt = in.readLong();
		localPath = in.readString();
	}
}
