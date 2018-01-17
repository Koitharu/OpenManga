package org.nv95.openmanga.discover;

import android.graphics.drawable.Drawable;

import org.nv95.openmanga.core.models.ProviderHeader;

/**
 * Created by koitharu on 26.12.17.
 */

public final class ProviderHeaderDetailed extends ProviderHeader {

	public final String summary;
	public final Drawable icon;

	public ProviderHeaderDetailed(String cname, String dname, String summary, Drawable icon) {
		super(cname, dname);
		this.summary = summary;
		this.icon = icon;
	}
}
