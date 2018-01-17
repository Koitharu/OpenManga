package org.nv95.openmanga.sync;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

/**
 * Created by koitharu on 18.12.17.
 */

public class AccountService extends Service {

	private SyncAuthenticator mAuthenticator;
	
	@Override
	public void onCreate() {
		// Create a new authenticator object
		mAuthenticator = new SyncAuthenticator(this);
	}


	@Nullable
	@Override
	public IBinder onBind(Intent intent) {
		return mAuthenticator.getIBinder();
	}
}
