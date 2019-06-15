package org.nv95.openmanga.helpers;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.preference.PreferenceManager;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;
import android.text.TextUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.nv95.openmanga.BuildConfig;
import org.nv95.openmanga.items.RESTResponse;
import org.nv95.openmanga.items.SyncDevice;
import org.nv95.openmanga.providers.FavouritesProvider;
import org.nv95.openmanga.providers.HistoryProvider;
import org.nv95.openmanga.services.SyncService;
import org.nv95.openmanga.utils.AppHelper;
import org.nv95.openmanga.core.network.NetworkUtils;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

/**
 * Created by admin on 10.07.17.
 */

public class SyncHelper {

    private static WeakReference<SyncHelper> instanceRef = new WeakReference<>(null);

    public static SyncHelper get(Context context) {
        SyncHelper instance = instanceRef.get();
        if (instance == null) {
            instance = new SyncHelper(context);
            instanceRef = new WeakReference<>(instance);
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
        setToken(token, null);
    }

    private void setToken(String token, String username) {
        mToken = token;
        SharedPreferences.Editor editor = mPreferences.edit();
        if (token != null) {
            editor.putString("sync.token", token).remove("sync.last_favourites").remove("sync.last_history");
            if (username != null) {
                editor.putString("sync.username", username);
            }
        } else {
            editor.remove("sync.token").remove("sync.username").remove("sync.last_favourites").remove("sync.last_history");
        }
        editor.apply();
    }

    @WorkerThread
    public RESTResponse register(String login, String password) {
        RESTResponse response = NetworkUtils.restQuery(
                BuildConfig.SYNC_URL + "/user",
                null,
                NetworkUtils.HTTP_PUT,
                "login", login,
                "password", password,
                "device",
                AppHelper.getDeviceSummary()
        );
        if (response.isSuccess()) {
            try {
                setToken(response.getData().getString("token"), login);
            } catch (JSONException e) {
                e.printStackTrace();
                return RESTResponse.fromThrowable(e);
            }
        }
        return response;
    }

    @WorkerThread
    public RESTResponse authorize(String login, String password) {
        RESTResponse response = NetworkUtils.restQuery(
                BuildConfig.SYNC_URL + "/user",
                null,
                NetworkUtils.HTTP_POST,
                "login", login,
                "password", password,
                "device",
                AppHelper.getDeviceSummary()
        );
        if (response.isSuccess()) {
            try {
                setToken(response.getData().getString("token"), login);
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
        try {
            HistoryProvider provider = HistoryProvider.getInstance(mContext);
            long lastSync = getLastHistorySync();
            JSONArray updated = provider.dumps(lastSync);
            if (updated == null) {
                return RESTResponse.fromThrowable(new NullPointerException());
            }
            JSONArray deleted = dumpsDeleted("history");
            if (deleted == null) {
                return RESTResponse.fromThrowable(new NullPointerException("deleted is null"));
            }
            RESTResponse resp = NetworkUtils.restQuery(
                    BuildConfig.SYNC_URL + "/history",
                    mToken,
                    NetworkUtils.HTTP_POST,
                    "timestamp",
                    String.valueOf(lastSync),
                    "updated",
                    updated.toString(),
                    "deleted",
                    deleted.toString()
            );
            if (!resp.isSuccess()) {
                if (resp.getResponseCode() == RESTResponse.RC_INVALID_TOKEN) {
                    setToken(null);
                }
                return resp;
            }
            if (!provider.inject(resp.getData().getJSONArray("updated"))) {
                return RESTResponse.fromThrowable(new SQLException("Cannot write data"));
            }
            deleted = resp.getData().getJSONArray("deleted");
            for (int i=0;i<deleted.length(); i++) {
                JSONObject o = deleted.getJSONObject(i);
                provider.remove(new long[]{o.getLong("manga_id")});
            }
            clearDeleted("history");
            return resp;
        } catch (Exception e) {
            e.printStackTrace();
            return RESTResponse.fromThrowable(e);
        }
    }

    @WorkerThread
    public RESTResponse syncFavourites() {
        try {
            FavouritesProvider provider = FavouritesProvider.getInstance(mContext);
            long lastSync = getLastFavouritesSync();
            JSONArray updated = provider.dumps(lastSync);
            if (updated == null) {
                return RESTResponse.fromThrowable(new NullPointerException("updated is null"));
            }
            JSONArray deleted = dumpsDeleted("favourites");
            if (deleted == null) {
                return RESTResponse.fromThrowable(new NullPointerException("deleted is null"));
            }
            RESTResponse resp = NetworkUtils.restQuery(
                    BuildConfig.SYNC_URL + "/favourites",
                    mToken,
                    NetworkUtils.HTTP_POST,
                    "timestamp",
                    String.valueOf(lastSync),
                    "updated",
                    updated.toString(),
                    "deleted",
                    deleted.toString()
            );
            if (!resp.isSuccess()) {
                if (resp.getResponseCode() == RESTResponse.RC_INVALID_TOKEN) {
                    setToken(null);
                }
                return resp;
            }
            if (!provider.inject(resp.getData().getJSONArray("updated"))) {
                return RESTResponse.fromThrowable(new SQLException("Cannot write data"));
            }
            deleted = resp.getData().getJSONArray("deleted");
            for (int i=0;i<deleted.length(); i++) {
                JSONObject o = deleted.getJSONObject(i);
                provider.remove(new long[]{o.getLong("manga_id")});
            }
            clearDeleted("favourites");
            return resp;
        } catch (Exception e) {
            e.printStackTrace();
            return RESTResponse.fromThrowable(e);
        }
    }

    @Nullable
    private JSONArray dumpsDeleted(String subject) {
        StorageHelper storageHelper = null;
        SQLiteDatabase database = null;
        Cursor c = null;
        try {
            JSONArray array = new JSONArray();
            storageHelper = new StorageHelper(mContext);
            database = storageHelper.getReadableDatabase();
            c = database.query(
                    "sync_delete", new String[]{"manga_id", "timestamp"},
                    "subject = ?", new String[]{subject}, null, null, null
            );
            if (c.moveToFirst()) {
                do {
                    JSONObject jobj = new JSONObject();
                    jobj.put("manga_id", c.getInt(0));
                    jobj.put("timestamp", c.getLong(1));
                    array.put(jobj);
                } while (c.moveToNext());
            }
            return array;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            if (storageHelper != null) {
                if (database != null) {
                    database.close();
                    if (c != null) {
                        c.close();
                    }
                }
                storageHelper.close();
            }
        }
    }

    private void clearDeleted(String subject) {
        StorageHelper storageHelper = null;
        SQLiteDatabase database = null;
        try {
            storageHelper = new StorageHelper(mContext);
            database = storageHelper.getWritableDatabase();
            database.delete("sync_delete", "subject = ?", new String[]{subject});
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (storageHelper != null) {
                if (database != null) {
                    database.close();
                }
                storageHelper.close();
            }
        }
    }

    public void setDeleted(@Nullable SQLiteDatabase writableDatabase, String subject, long id) {
        switch (subject) {
            case "history":
                if (!isHistorySyncEnabled()) return;
                break;
            case "favourites":
                if (!isFavouritesSyncEnabled()) return;
                break;
            default:
                return;
        }
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

    public ArrayList<SyncDevice> getUserDevices(boolean includeSelf) throws Exception {
        ArrayList<SyncDevice> list = new ArrayList<>();
        RESTResponse resp = NetworkUtils.restQuery(
                BuildConfig.SYNC_URL + "/user",
                mToken,
                NetworkUtils.HTTP_GET,
                "self",
                includeSelf ? "1" : "0"
        );
        if (!resp.isSuccess()) {
            if (resp.getResponseCode() == RESTResponse.RC_INVALID_TOKEN) {
                setToken(null);
                noauthBroadcast();
            }
            return null;
        }
        JSONArray devices = resp.getData().getJSONArray("devices");
        int len = devices.length();
        for (int i = 0; i < len; i++) {
            JSONObject o = devices.getJSONObject(i);
            list.add(new SyncDevice(
                    o.getInt("id"),
                    o.getString("device"),
                    o.getLong("created_at")
            ));
        }
        return list;
    }

    public RESTResponse detachDevice(int id) {
        return NetworkUtils.restQuery(
                BuildConfig.SYNC_URL + "/user",
                mToken,
                NetworkUtils.HTTP_DELETE,
                "id",
                String.valueOf(id)
        );
    }

    public RESTResponse logout() {
        RESTResponse resp = NetworkUtils.restQuery(
                BuildConfig.SYNC_URL + "/user",
                mToken,
                NetworkUtils.HTTP_DELETE,
                "self",
                "1"
        );
        if (resp.isSuccess()) {
            setToken(null);
        }
        return resp;
    }

    private void noauthBroadcast() {
        Intent intent = new Intent();
        intent.setAction(SyncService.SYNC_EVENT);
        intent.putExtra("what", SyncService.MSG_UNAUTHORIZED);
        mContext.sendBroadcast(intent);
    }
}
