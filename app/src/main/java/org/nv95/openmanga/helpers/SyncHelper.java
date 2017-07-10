package org.nv95.openmanga.helpers;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.annotation.WorkerThread;
import android.text.TextUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.nv95.openmanga.BuildConfig;
import org.nv95.openmanga.utils.AppHelper;
import org.nv95.openmanga.utils.NetworkUtils;

import java.lang.ref.WeakReference;
import java.util.Date;

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

    @Nullable
    public Date getLastSync() {
        long ts = mPreferences.getLong("sync.last_sync", 0);
        return ts == 0 ? null : new Date(ts);
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
    public RESTResponse postHistory() {
        StorageHelper storageHelper = null;
        try {
            storageHelper = new StorageHelper(mContext);
            Date lastSync = getLastSync();
            JSONArray data = storageHelper.extractTableData("history", lastSync == null ? null : "timestamp > " + lastSync.getTime());

            return new RESTResponse(NetworkUtils.restQuery("/history", NetworkUtils.HTTP_POST, "data", data.toString()));
        } catch (Exception e) {
            e.printStackTrace();
            return RESTResponse.fromThrowable(e);
        } finally {
            if (storageHelper != null) {
                storageHelper.close();
            }
        }
    }

    public static class RESTResponse {

        private RESTResponse() {
        }

        RESTResponse(JSONObject data) {
            this.data = data;
            try {
                this.state = data.getString("state");
                this.message = data.has("message") ? data.getString("message") : null;
            } catch (JSONException e) {
                e.printStackTrace();
                this.state = "fail";
                this.message = e.getMessage();
            }
        }

        String state;
        @Nullable
        String message;
        JSONObject data;

        public boolean isSuccess() {
            return "success".equals(state);
        }

        public String getMessage() {
            return message == null ? "Internal error" : message;
        }

        public JSONObject getData() {
            return data;
        }

        public static RESTResponse fromThrowable(Throwable e) {
            RESTResponse resp = new RESTResponse();
            resp.state = "fail";
            resp.message = e.getMessage();
            resp.data = new JSONObject();
            return resp;
        }
    }

}
