package org.nv95.openmanga.content;

/**
 * Created by koitharu on 21.12.17.
 */

public class MangaPage {

	public final long id;
	public final String url;
	public final String provider;

	public MangaPage(String url, String provider) {
		this.url = url;
		this.provider = provider;
		this.id = provider.hashCode() + url.hashCode();
	}

	public MangaPage(long id, String url, String provider) {
		this.id = id;
		this.url = url;
		this.provider = provider;
	}
}
