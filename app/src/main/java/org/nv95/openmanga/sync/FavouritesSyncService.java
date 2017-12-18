package org.nv95.openmanga.sync;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * Created by koitharu on 18.12.17.
 */

public class FavouritesSyncService extends Service {

	private static FavouritesSyncAdapter sFavouritesSyncAdapter = null;
	private static final Object sSyncAdapterLock = new Object();

	@Override
	public void onCreate() {

		synchronized (sSyncAdapterLock) {
			if (sFavouritesSyncAdapter == null) {
				sFavouritesSyncAdapter = new FavouritesSyncAdapter(getApplicationContext(), true);
			}
		}
	}

	@Override
	public IBinder onBind(Intent intent) {
		return sFavouritesSyncAdapter.getSyncAdapterBinder();
	}

}
