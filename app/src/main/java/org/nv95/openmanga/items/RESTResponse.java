package org.nv95.openmanga.items;

import androidx.annotation.Nullable;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by admin on 11.07.17.
 */

public class RESTResponse {

    public static final int RC_OK = 200;
    public static final int RC_SERVER_ERROR = 500;
    public static final int RC_CLIENT_ERROR = 0;
    public static final int RC_INVALID_TOKEN = 403;

    private String state;
    @Nullable
    private
    String message;
    private JSONObject data;
    private int responseCode;

    private RESTResponse() {
    }

    public RESTResponse(JSONObject data) {
        this(data, 200);
    }

    public RESTResponse(JSONObject data, int responseCode) {
        this.responseCode = responseCode;
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

    public int getResponseCode() {
        return responseCode;
    }

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
        resp.responseCode = 0;
        return resp;
    }
}