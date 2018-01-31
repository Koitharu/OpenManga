package org.nv95.openmanga.core.models;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * Created by koitharu on 21.12.17.
 */

public class MangaHeader implements Parcelable, UniqueObject {

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

	@NonNull
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

	@Nullable
	public static MangaHeader from(Bundle bundle) {
		if (bundle.containsKey("manga")) {
			return bundle.getParcelable("manga");
		} else return new MangaHeader(
				bundle.getLong("id"),
				bundle.getString("name"),
				bundle.getString("summary"),
				bundle.getString("genres"),
				bundle.getString("url"),
				bundle.getString("thumbnail"),
				bundle.getString("provider"),
				bundle.getInt("status"),
				(short) bundle.getInt("rating")
		);
	}

	@NonNull
	public Bundle toBundle() {
		final Bundle bundle = new Bundle();
		bundle.putLong("id", id);
		bundle.putString("name", name);
		bundle.putString("summary", summary);
		bundle.putString("genres", genres);
		bundle.putString("url", url);
		bundle.putString("thumbnail", thumbnail);
		bundle.putString("provider", provider);
		bundle.putInt("status", status);
		bundle.putInt("rating", rating);
		return bundle;
	}

	@Override
	public long getId() {
		return id;
	}
}
