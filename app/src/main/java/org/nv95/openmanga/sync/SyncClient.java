package org.nv95.openmanga.sync;

import android.support.annotation.Nullable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.nv95.openmanga.BuildConfig;
import org.nv95.openmanga.items.RESTResponse;
import org.nv95.openmanga.items.SyncDevice;
import org.nv95.openmanga.utils.AppHelper;
import org.nv95.openmanga.utils.NetworkUtils;

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
				NetworkUtils.HTTP_DELETE,
				"id",
				String.valueOf(id)
		);
	}

	public ArrayList<SyncDevice> getAttachedDevices() throws JSONException, InvalidTokenException {
		ArrayList<SyncDevice> list = new ArrayList<>();
		RESTResponse resp = NetworkUtils.restQuery(
				BuildConfig.SYNC_URL + "/user",
				mToken,
				NetworkUtils.HTTP_GET,
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

	@Nullable
	public static String authenticate(String login, String password) {
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
}
