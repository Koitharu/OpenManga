package org.nv95.openmanga.content.shelf;

import java.util.UUID;

/**
 * Created by koitharu on 26.12.17.
 */

public class Category {

	public final long id;
	public final String name;
	public final long createdAt;

	public Category(long id, String name, long createdAt) {
		this.id = id;
		this.name = name;
		this.createdAt = createdAt;
	}

	public Category(String name, long createdAt) {
		this.id = UUID.randomUUID().getMostSignificantBits();
		this.name = name;
		this.createdAt = createdAt;
	}
}
