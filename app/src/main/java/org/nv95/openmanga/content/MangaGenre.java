package org.nv95.openmanga.content;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.StringRes;

/**
 * Created by koitharu on 21.12.17.
 */

public final class MangaGenre implements Parcelable {

	@StringRes
	public final int nameId;
	public final String value;

	public MangaGenre(int nameId, String value) {
		this.nameId = nameId;
		this.value = value;
	}

	private MangaGenre(Parcel in) {
		nameId = in.readInt();
		value = in.readString();
	}

	public static final Creator<MangaGenre> CREATOR = new Creator<MangaGenre>() {
		@Override
		public MangaGenre createFromParcel(Parcel in) {
			return new MangaGenre(in);
		}

		@Override
		public MangaGenre[] newArray(int size) {
			return new MangaGenre[size];
		}
	};

	@Override
	public String toString() {
		return value;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel parcel, int i) {
		parcel.writeInt(nameId);
		parcel.writeString(value);
	}
}
