package org.nv95.openmanga.content;

import android.support.annotation.NonNull;

/**
 * Created by koitharu on 26.12.17.
 */

public class ProviderHeader {

	@NonNull
	public final String cName;
	@NonNull
	public final String dName;

	public ProviderHeader(@NonNull String cName, @NonNull String dName) {
		this.cName = cName;
		this.dName = dName;
	}

	@Override
	public boolean equals(Object obj) {
		return obj != null && obj instanceof ProviderHeader && ((ProviderHeader) obj).cName.equals(cName);
	}

	@Override
	public int hashCode() {
		return cName.hashCode();
	}
}
