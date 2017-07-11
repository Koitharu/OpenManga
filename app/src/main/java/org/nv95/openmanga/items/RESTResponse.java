package org.nv95.openmanga.items;

import android.support.annotation.Nullable;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by admin on 11.07.17.
 */

public class RESTResponse {

    private RESTResponse() {
    }

    public RESTResponse(JSONObject data) {
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

    private String state;
    @Nullable
    private
    String message;
    private JSONObject data;

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