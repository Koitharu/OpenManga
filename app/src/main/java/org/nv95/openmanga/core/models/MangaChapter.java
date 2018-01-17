package org.nv95.openmanga.core.models;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by koitharu on 21.12.17.
 */

public class MangaChapter implements Parcelable {

	public final long id;
	public final String name;
	public final int number;
	public final String url;
	public final String provider;

	public MangaChapter(String name, int number, String url, String provider) {
		this.name = name;
		this.number = number;
		this.url = url;
		this.provider = provider;
		this.id = provider.hashCode() + url.hashCode();
	}

	public MangaChapter(long id, String name, int number, String url, String provider) {
		this.id = id;
		this.name = name;
		this.number = number;
		this.url = url;
		this.provider = provider;
	}

	protected MangaChapter(Parcel in) {
		id = in.readLong();
		name = in.readString();
		number = in.readInt();
		url = in.readString();
		provider = in.readString();
	}

	public static final Creator<MangaChapter> CREATOR = new Creator<MangaChapter>() {
		@Override
		public MangaChapter createFromParcel(Parcel in) {
			return new MangaChapter(in);
		}

		@Override
		public MangaChapter[] newArray(int size) {
			return new MangaChapter[size];
		}
	};

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeLong(id);
		dest.writeString(name);
		dest.writeInt(number);
		dest.writeString(url);
		dest.writeString(provider);
	}

	public Bundle toBundle() {
		Bundle bundle = new Bundle(1);
		bundle.putParcelable("_chapter", this);
		return bundle;
	}

	public static MangaChapter from(Bundle bundle) {
		return bundle.getParcelable("_chapter");
	}
}
