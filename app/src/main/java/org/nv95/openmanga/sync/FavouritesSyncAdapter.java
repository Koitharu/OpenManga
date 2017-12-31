package org.nv95.openmanga.sync;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AuthenticatorException;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentValues;
import android.content.Context;
import android.content.SyncResult;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;

import org.json.JSONArray;
import org.json.JSONObject;
import org.nv95.openmanga.content.RESTResponse;
import org.nv95.openmanga.utils.TextUtils;

import java.io.IOException;

/**
 * Created by koitharu on 18.12.17.
 */

public class FavouritesSyncAdapter extends AbstractThreadedSyncAdapter {

	private static final String KEY_LAST_SYNC = "lastsync.favourites";
	private static final String[] PROJECTION = new String[] {
			"id", "name", "subtitle", "summary", "provider", "preview", "path", "timestamp", "rating"
	};
	private static final String[] PROJECTION_DELETED = new String[]{"manga_id", "timestamp"};

	private static final Uri URI = Uri.parse("content://" + FavouritesContentProvider.AUTHORITY + "/favourites");
	private static final Uri URI_DELETED = Uri.parse("content://" + FavouritesContentProvider.AUTHORITY + "/deleted");

	public FavouritesSyncAdapter(Context context, boolean autoInitialize) {
		super(context, autoInitialize);
	}

	public FavouritesSyncAdapter(Context context, boolean autoInitialize, boolean allowParallelSyncs) {
		super(context, autoInitialize, allowParallelSyncs);
	}

	@Override
	public void onPerformSync(Account account, Bundle bundle, String s, ContentProviderClient provider, SyncResult syncResult) {
		Cursor cursor = null;
		try {
			//prepare
			final AccountManager accountManager = AccountManager.get(getContext());
			final String token = accountManager.blockingGetAuthToken(account, SyncAuthenticator.TOKEN_DEFAULT, true);
			final SyncClient client = new SyncClient(token);
			String t = accountManager.getUserData(account, KEY_LAST_SYNC);
			long lastSync = 0;
			if (t != null) {
				try {
					lastSync = Long.parseLong(t);
				} catch (NumberFormatException ne) {
					ne.printStackTrace();
				}
			}
			//get new/updated
			cursor = provider.query(URI, PROJECTION, "timestamp > ?", new String[]{String.valueOf(lastSync)}, null);
			if (cursor == null) return;
			JSONArray updated = new JSONArray();
			if (cursor.moveToFirst()) {
				do {
					JSONObject jobj = new JSONObject();
					JSONObject manga = new JSONObject();
					manga.put("id", cursor.getInt(0));
					manga.put("name", cursor.getString(1));
					manga.put("subtitle", TextUtils.notNull(cursor.getString(2)));
					manga.put("summary", TextUtils.notNull(cursor.getString(3)));
					manga.put("provider", cursor.getString(4));
					manga.put("preview", cursor.getString(5));
					manga.put("path", cursor.getString(6));
					manga.put("rating", cursor.getInt(8));
					jobj.put("manga", manga);
					jobj.put("timestamp", cursor.getLong(7));
					updated.put(jobj);
				} while (cursor.moveToNext());
			}
			cursor.close();
			//get deleted
			JSONArray deleted = new JSONArray();
			cursor = provider.query(URI_DELETED, PROJECTION_DELETED, null, null, null);
			if (cursor != null && cursor.moveToFirst()) {
				do {
					JSONObject jobj = new JSONObject();
					jobj.put("mangaId", cursor.getInt(0));
					jobj.put("timestamp", cursor.getLong(1));
					deleted.put(jobj);
				} while (cursor.moveToNext());
			}
			//send to server
			RESTResponse response = client.pushFavourites(updated, deleted, lastSync);
			if (!response.isSuccess()) {
				syncResult.stats.numIoExceptions++;
				return;
			}
			//insert new/update
			updated = response.getData().getJSONArray("updated");
			int len = updated.length();
			for (int i=0;i<len;i++) {
				JSONObject jobj = updated.getJSONObject(i);
				JSONObject manga = jobj.getJSONObject("manga");
				ContentValues cv = new ContentValues();
				int id = manga.getInt("id");
				cv.put("id", id);
				cv.put("name", manga.getString("name"));
				cv.put("subtitle", manga.getString("subtitle"));
				cv.put("summary", manga.getString("summary"));
				cv.put("provider", manga.getString("provider"));
				cv.put("preview", manga.getString("preview"));
				cv.put("path", manga.getString("path"));
				cv.put("timestamp", jobj.getLong("timestamp"));
				cv.put("rating", manga.getLong("rating"));
				if (provider.update(URI, cv, "id=?", new String[]{String.valueOf(id)}) <= 0) {
					provider.insert(URI, cv);
					syncResult.stats.numInserts++;
				} else {
					syncResult.stats.numUpdates++;
				}
			}
			//remove deleted
			deleted = response.getData().getJSONArray("deleted");
			for (int i=0;i<deleted.length(); i++) {
				JSONObject o = deleted.getJSONObject(i);
				provider.delete(
						Uri.parse("content://" + HistoryContentProvider.AUTHORITY + "/favourites/" + o.getLong("manga_id")),
						null,
						null
				);
			}
			provider.delete(URI_DELETED, null, null);
			accountManager.setUserData(account, KEY_LAST_SYNC, String.valueOf(System.currentTimeMillis()));
		} catch (IOException e) {
			syncResult.stats.numIoExceptions++;
			e.printStackTrace();
		} catch (SyncClient.InvalidTokenException e) {
			syncResult.stats.numAuthExceptions++;
		} catch (AuthenticatorException e) {
			syncResult.stats.numAuthExceptions++;
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (cursor != null) cursor.close();
		}
	}
}
