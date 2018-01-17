package org.nv95.openmanga.core.models;

import android.content.Context;

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

	public static Category createDefault(Context context) {
		return new Category(context.getString(R.string.action_favourites), System.currentTimeMillis());
	}
}
