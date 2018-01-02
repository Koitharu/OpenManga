package org.nv95.openmanga.content;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;

import org.nv95.openmanga.ui.mangalist.MangaListActivity;

/**
 * Created by koitharu on 21.12.17.
 */

public final class MangaGenre implements Parcelable {

	@StringRes
	public final int nameId;
	@NonNull
	public final String value;

	public MangaGenre(int nameId, @NonNull String value) {
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

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		MangaGenre that = (MangaGenre) o;

		return nameId == that.nameId && value.equals(that.value);
	}

	@Override
	public int hashCode() {
		return 31 * nameId + value.hashCode();
	}

	@NonNull
	public static String joinNames(@NonNull Context context, @NonNull MangaGenre[] genres, @NonNull String delimiter) {
		final StringBuilder builder = new StringBuilder();
		boolean nonFirst = false;
		for (MangaGenre o: genres) {
			if (nonFirst) {
				builder.append(delimiter);
			} else {
				nonFirst = true;
			}
			builder.append(context.getString(o.nameId));
		}
		return builder.toString();
	}
}
