package org.nv95.openmanga.sync;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * Created by koitharu on 18.12.17.
 */

public class HistorySyncService extends Service {

	private static HistorySyncAdapter sHistorySyncAdapter = null;
	private static final Object sSyncAdapterLock = new Object();

	@Override
	public void onCreate() {

		synchronized (sSyncAdapterLock) {
			if (sHistorySyncAdapter == null) {
				sHistorySyncAdapter = new HistorySyncAdapter(getApplicationContext(), true);
			}
		}
	}

	@Override
	public IBinder onBind(Intent intent) {
		return sHistorySyncAdapter.getSyncAdapterBinder();
	}

}
