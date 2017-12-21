package org.nv95.openmanga.content;

/**
 * Created by koitharu on 21.12.17.
 */

public class MangaChapter {

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
}
