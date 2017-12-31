package org.nv95.openmanga.sync;

import android.os.Build;
import android.support.annotation.Nullable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.nv95.openmanga.BuildConfig;
import org.nv95.openmanga.content.RESTResponse;
import org.nv95.openmanga.utils.network.NetworkUtils;

import java.util.ArrayList;

/**
 * Created by koitharu on 19.12.17.
 */

public class SyncClient {

	private final String mToken;

	public SyncClient(String token) {
		mToken = token;
	}

	public RESTResponse detachDevice(int id) {
		return NetworkUtils.restQuery(
				BuildConfig.SYNC_URL + "/user",
				mToken,
				"DELETE",
				"id",
				String.valueOf(id)
		);
	}

	public ArrayList<SyncDevice> getAttachedDevices() throws JSONException, InvalidTokenException {
		ArrayList<SyncDevice> list = new ArrayList<>();
		RESTResponse resp = NetworkUtils.restQuery(
				BuildConfig.SYNC_URL + "/user",
				mToken,
				"GET",
				"self",
				"0"
		);
		if (!resp.isSuccess()) {
			if (resp.getResponseCode() == RESTResponse.RC_INVALID_TOKEN) {
				throw new InvalidTokenException();
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

	public RESTResponse pushHistory(JSONArray updated, JSONArray deleted, long lastSync) throws InvalidTokenException {
		RESTResponse resp = NetworkUtils.restQuery(
				BuildConfig.SYNC_URL + "/history",
				mToken,
				"POST",
				"timestamp",
				String.valueOf(lastSync),
				"updated",
				updated.toString(),
				"deleted",
				deleted.toString()
		);
		if (!resp.isSuccess()) {
			if (resp.getResponseCode() == RESTResponse.RC_INVALID_TOKEN) {
				throw new InvalidTokenException();
			}
		}
		return resp;
	}

	public RESTResponse pushFavourites(JSONArray updated, JSONArray deleted, long lastSync) throws InvalidTokenException {
		RESTResponse resp = NetworkUtils.restQuery(
				BuildConfig.SYNC_URL + "/favourites",
				mToken,
				"POST",
				"timestamp",
				String.valueOf(lastSync),
				"updated",
				updated.toString(),
				"deleted",
				deleted.toString()
		);
		if (!resp.isSuccess()) {
			if (resp.getResponseCode() == RESTResponse.RC_INVALID_TOKEN) {
				throw new InvalidTokenException();
			}
		}
		return resp;
	}

	@Nullable
	public static String authenticate(String login, String password) {
		RESTResponse response = NetworkUtils.restQuery(
				BuildConfig.SYNC_URL + "/user",
				null,
				"POST",
				"login", login,
				"password", password,
				"device",
				getDeviceSummary()
		);
		if (response.isSuccess()) {
			try {
				return response.getData().getString("token");
			} catch (JSONException e) {
				e.printStackTrace();
				return null;
			}
		}
		return null;
	}

	public class InvalidTokenException extends IllegalArgumentException {
	}

	public static String getDeviceSummary() {
		return Build.MANUFACTURER +
				' ' +
				Build.MODEL +
				" (Android " +
				Build.VERSION.RELEASE +
				")";
	}
}
