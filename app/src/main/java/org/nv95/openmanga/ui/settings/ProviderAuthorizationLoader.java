package org.nv95.openmanga.ui.settings;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.support.annotation.Nullable;

import org.nv95.openmanga.content.providers.MangaProvider;

/**
 * Created by koitharu on 12.01.18.
 */

public final class ProviderAuthorizationLoader extends AsyncTaskLoader<String> {

	private final String mProvider;
	private final String mLogin;
	private final String mPassword;

	public ProviderAuthorizationLoader(Context context, String provider, String login, String password) {
		super(context);
		mProvider = provider;
		mLogin = login;
		mPassword = password;
	}

	@Nullable
	@Override
	public String loadInBackground() {
		try {
			final MangaProvider provider = MangaProvider.getProvider(getContext(), mProvider);
			return provider.authorize(mLogin, mPassword);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
}
