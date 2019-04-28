package org.nv95.openmanga.lists;

import android.os.Parcel;
import android.os.Parcelable;
import androidx.annotation.NonNull;
import android.text.TextUtils;

/**
 * Created by nv95 on 22.06.16.
 */

public class Genres implements Parcelable {

    private final String[] mData;

    public Genres(@NonNull String data) {
        mData = data.split("[,]?\\s");
    }

    private Genres(Parcel in) {
        mData = in.createStringArray();
    }

    public static final Creator<Genres> CREATOR = new Creator<Genres>() {
        @Override
        public Genres createFromParcel(Parcel in) {
            return new Genres(in);
        }

        @Override
        public Genres[] newArray(int size) {
            return new Genres[size];
        }
    };

    @Override
    public String toString() {
        return TextUtils.join(" ", mData);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeStringArray(mData);
    }
}
