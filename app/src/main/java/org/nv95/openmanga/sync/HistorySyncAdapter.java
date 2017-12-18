package org.nv95.openmanga.sync;

import android.accounts.Account;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.Context;
import android.content.SyncResult;
import android.database.SQLException;
import android.os.Bundle;

import org.json.JSONArray;
import org.json.JSONObject;
import org.nv95.openmanga.BuildConfig;
import org.nv95.openmanga.items.RESTResponse;
import org.nv95.openmanga.providers.HistoryProvider;
import org.nv95.openmanga.utils.NetworkUtils;

/**
 * Created by koitharu on 18.12.17.
 */

public class HistorySyncAdapter extends AbstractThreadedSyncAdapter {

	public HistorySyncAdapter(Context context, boolean autoInitialize) {
		super(context, autoInitialize);
	}

	public HistorySyncAdapter(Context context, boolean autoInitialize, boolean allowParallelSyncs) {
		super(context, autoInitialize, allowParallelSyncs);
	}

	@Override
	public void onPerformSync(Account account, Bundle bundle, String s, ContentProviderClient contentProviderClient, SyncResult syncResult) {

	}
}
