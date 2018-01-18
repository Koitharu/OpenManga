package org.nv95.openmanga.core.models;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;

import org.nv95.openmanga.R;

/**
 * Created by koitharu on 26.12.17.
 */

public class Category {

	public final int id;
	public final String name;
	public final long createdAt;

	public Category(int id, String name, long createdAt) {
		this.id = id;
		this.name = name;
		this.createdAt = createdAt;
	}

	public Category(String name, long createdAt) {
		this.id = name.hashCode();
		this.name = name;
		this.createdAt = createdAt;
	}

	@NonNull
	public static Category createDefault(Context context) {
		return new Category(context.getString(R.string.action_favourites), System.currentTimeMillis());
	}

	@NonNull
	public Bundle toBundle() {
		final Bundle bundle = new Bundle(3);
		bundle.putInt("id", id);
		bundle.putString("name", name);
		bundle.putLong("created_at", createdAt);
		return bundle;
	}

	@NonNull
	public static Category fromBundle(Bundle bundle) {
		return new Category(
				bundle.getInt("id"),
				bundle.getString("name"),
				bundle.getLong("created_at")
		);
	}
}
