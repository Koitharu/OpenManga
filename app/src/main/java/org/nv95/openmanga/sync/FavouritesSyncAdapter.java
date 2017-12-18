package org.nv95.openmanga.sync;

import android.accounts.Account;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.Context;
import android.content.SyncResult;
import android.os.Bundle;

/**
 * Created by koitharu on 18.12.17.
 */

public class FavouritesSyncAdapter extends AbstractThreadedSyncAdapter {

	public FavouritesSyncAdapter(Context context, boolean autoInitialize) {
		super(context, autoInitialize);
	}

	public FavouritesSyncAdapter(Context context, boolean autoInitialize, boolean allowParallelSyncs) {
		super(context, autoInitialize, allowParallelSyncs);
	}

	@Override
	public void onPerformSync(Account account, Bundle bundle, String s, ContentProviderClient contentProviderClient, SyncResult syncResult) {

	}
}
