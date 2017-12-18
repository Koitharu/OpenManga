package org.nv95.openmanga.sync;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

/**
 * Created by koitharu on 18.12.17.
 */

public class SyncAccountService extends Service {

	@Nullable
	@Override
	public IBinder onBind(Intent intent) {
		SyncAuthenticator authenticator = new SyncAuthenticator(this);
		return authenticator.getIBinder();
	}
}
