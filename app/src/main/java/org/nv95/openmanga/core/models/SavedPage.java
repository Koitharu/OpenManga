package org.nv95.openmanga.core.models;

import android.os.Parcel;
import android.support.annotation.NonNull;

/**
 * Created by koitharu on 25.01.18.
 */

public class SavedPage extends MangaPage {

	public final long chapterId;
	public final int number;

	public SavedPage(String url, String provider, long chapterId, int number) {
		super(url, provider);
		this.chapterId = chapterId;
		this.number = number;
	}

	public SavedPage(long id, String url, String provider, long chapterId, int number) {
		super(id, url, provider);
		this.chapterId = chapterId;
		this.number = number;
	}

	public static final Creator<SavedPage> CREATOR = new Creator<SavedPage>() {
		@Override
		public SavedPage createFromParcel(Parcel in) {
			return new SavedPage(in);
		}

		@Override
		public SavedPage[] newArray(int size) {
			return new SavedPage[size];
		}
	};

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		super.writeToParcel(dest, flags);
		dest.writeLong(chapterId);
		dest.writeInt(number);
	}

	protected SavedPage(Parcel in) {
		super(in);
		chapterId = in.readLong();
		number = in.readInt();
	}

	@NonNull
	public static SavedPage from(MangaPage page, long chapterId, int number) {
		return new SavedPage(
				page.id,
				page.url,
				page.provider,
				chapterId,
				number
		);
	}
}
