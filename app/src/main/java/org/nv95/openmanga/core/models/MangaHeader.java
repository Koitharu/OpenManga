package org.nv95.openmanga.core.models;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by koitharu on 21.12.17.
 */

public class MangaHeader implements Parcelable {

	public final long id;
	public final String name;
	public final String summary;
	public final String genres;
	public final String url;
	public final String thumbnail;
	public final String provider;
	public final int status;
	public final short rating; //0..100

	public MangaHeader(String name, String summary, String genres, String url, String thumbnail, String provider, int status, short rating) {
		this.name = name;
		this.summary = summary;
		this.genres = genres;
		this.url = url;
		this.thumbnail = thumbnail;
		this.provider = provider;
		this.status = status;
		this.rating = rating;
		this.id = provider.hashCode() + url.hashCode();
	}

	public MangaHeader(long id, String name, String summary, String genres, String url, String thumbnail, String provider, int status, short rating) {
		this.id = id;
		this.name = name;
		this.summary = summary;
		this.genres = genres;
		this.url = url;
		this.thumbnail = thumbnail;
		this.provider = provider;
		this.status = status;
		this.rating = rating;
	}


	protected MangaHeader(Parcel in) {
		id = in.readLong();
		name = in.readString();
		summary = in.readString();
		genres = in.readString();
		url = in.readString();
		thumbnail = in.readString();
		provider = in.readString();
		status = in.readInt();
		rating = (short) in.readInt();
	}

	public static final Creator<MangaHeader> CREATOR = new Creator<MangaHeader>() {
		@Override
		public MangaHeader createFromParcel(Parcel in) {
			return new MangaHeader(in);
		}

		@Override
		public MangaHeader[] newArray(int size) {
			return new MangaHeader[size];
		}
	};

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel parcel, int i) {
		parcel.writeLong(id);
		parcel.writeString(name);
		parcel.writeString(summary);
		parcel.writeString(genres);
		parcel.writeString(url);
		parcel.writeString(thumbnail);
		parcel.writeString(provider);
		parcel.writeInt(status);
		parcel.writeInt((int) rating);
	}

	public static MangaHeader from(MangaHeader other) {
		return new MangaHeader(
				other.id,
				other.name,
				other.summary,
				other.genres,
				other.url,
				other.thumbnail,
				other.provider,
				other.status,
				other.rating
		);
	}
}
