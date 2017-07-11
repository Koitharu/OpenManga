package org.nv95.openmanga.helpers;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.annotation.WorkerThread;
import android.text.TextUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.nv95.openmanga.BuildConfig;
import org.nv95.openmanga.items.RESTResponse;
import org.nv95.openmanga.utils.AppHelper;
import org.nv95.openmanga.utils.NetworkUtils;

import java.lang.ref.WeakReference;

/**
 * Created by admin on 10.07.17.
 */

public class SyncHelper {

    private static WeakReference<SyncHelper> instanceRef = new WeakReference<>(null);

    public static SyncHelper get(Context context) {
        SyncHelper instance = instanceRef.get();
        if (instance == null) {
            instance = new SyncHelper(context);
            instanceRef = new WeakReference<SyncHelper>(instance);
        }
        return instance;
    }

    private String mToken;
    private final Context mContext;
    private final SharedPreferences mPreferences;

    private SyncHelper(Context context) {
        mContext = context;
        mPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        mToken = mPreferences.getString("sync.token", null);
    }

    public boolean isHistorySyncEnabled() {
        return mPreferences.getBoolean("sync.history", true);
    }

    public boolean isFavouritesSyncEnabled() {
        return mPreferences.getBoolean("sync.favourites", true);
    }

    public long getLastHistorySync() {
        return mPreferences.getLong("sync.last_history", 0);
    }

    public long getLastFavouritesSync() {
        return mPreferences.getLong("sync.last_favourites", 0);
    }

    public void setHistorySynced() {
        mPreferences.edit()
                .putLong("sync.last_history", System.currentTimeMillis())
                .apply();
    }

    public void setFavouritesSynced() {
        mPreferences.edit()
                .putLong("sync.last_favourites", System.currentTimeMillis())
                .apply();
    }

    private void setToken(String token) {
        mToken = token;
        SharedPreferences.Editor editor = mPreferences.edit();
        if (token != null) {
            editor.putString("sync.token", token);
        } else {
            editor.remove("sync.token");
        }
        editor.apply();
    }

    @WorkerThread
    public RESTResponse register(String login, String password) {
        RESTResponse response = new RESTResponse(NetworkUtils.restQuery(
                BuildConfig.SYNC_URL + "/user",
                null,
                NetworkUtils.HTTP_PUT,
                "login", login,
                "password", password,
                "device",
                AppHelper.getDeviceSummary()
        ));
        if (response.isSuccess()) {
            try {
                setToken(response.getData().getString("token"));
            } catch (JSONException e) {
                e.printStackTrace();
                return RESTResponse.fromThrowable(e);
            }
        }
        return response;
    }

    @WorkerThread
    public RESTResponse authorize(String login, String password) {
        RESTResponse response = new RESTResponse(NetworkUtils.restQuery(
                BuildConfig.SYNC_URL + "/user",
                null,
                NetworkUtils.HTTP_POST,
                "login", login,
                "password", password,
                "device",
                AppHelper.getDeviceSummary()
        ));
        if (response.isSuccess()) {
            try {
                setToken(response.getData().getString("token"));
            } catch (JSONException e) {
                e.printStackTrace();
                return RESTResponse.fromThrowable(e);
            }
        }
        return response;
    }

    public boolean isAuthorized() {
        return !TextUtils.isEmpty(mToken);
    }

    @WorkerThread
    public RESTResponse syncHistory() {
        StorageHelper storageHelper = null;
        try {
            storageHelper = new StorageHelper(mContext);
            long lastSync = getLastHistorySync();

            RESTResponse resp = new RESTResponse(NetworkUtils.restQuery(BuildConfig.SYNC_URL + "/history", mToken, NetworkUtils.HTTP_GET, "timestamp", String.valueOf(lastSync)));
            if (!resp.isSuccess()) {
                return resp;
            }
            JSONArray data = storageHelper.extractTableData("history", lastSync == 0 ? null : "timestamp > " + lastSync);
            storageHelper.insertTableData("history", resp.getData().getJSONArray("data"));
            return new RESTResponse(NetworkUtils.restQuery(BuildConfig.SYNC_URL + "/history", mToken, NetworkUtils.HTTP_POST, "data", data.toString()));
        } catch (Exception e) {
            e.printStackTrace();
            return RESTResponse.fromThrowable(e);
        } finally {
            if (storageHelper != null) {
                storageHelper.close();
            }
        }
    }

    public void remove(@Nullable SQLiteDatabase writableDatabase, String subject, long id) {
        SQLiteDatabase db = writableDatabase;
        if (db == null) {
            db = new StorageHelper(mContext).getWritableDatabase();
        }
        ContentValues cv = new ContentValues();
        cv.put("subject", subject);
        cv.put("manga_id", id);
        cv.put("timestamp", System.currentTimeMillis());
        db.insert("sync_delete", null, cv);
        if (writableDatabase == null) {
            db.close();
        }
    }

}
