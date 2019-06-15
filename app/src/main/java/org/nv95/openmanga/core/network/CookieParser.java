package org.nv95.openmanga.core.network;

import androidx.annotation.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by nv95 on 21.11.16.
 */

public class CookieParser {

    private final HashMap<String,String> mCookies;

    public CookieParser(List<String> cookies) {
        mCookies = new HashMap<>();
        String s;
        String[] sa ,sa1;
        for (String o : cookies) {
            s = o.substring(0, o.indexOf(";"));
            sa1 = s.split("=");
            if (sa1.length == 2) {
                mCookies.put(sa1[0], sa1[1]);
            }
        }
    }

    @Nullable
    public String getValue(String key) {
        return mCookies.get(key);
    }

    @Override
    public String toString() {
        StringBuilder res = new StringBuilder();
        for (Map.Entry<String,String> o : mCookies.entrySet()) {
            res.append(o.getKey()).append("=").append(o.getValue()).append("; ");
        }
        //res.delete(res.length() - 2, res.length() - 1);
        return res.toString();
    }

    public String toString(String... values) {
        StringBuilder res = new StringBuilder();
        for (String key : values) {
            res.append(key).append("=").append(mCookies.get(key)).append("; ");
        }
        return res.substring(0, res.length() - 2);
    }
}
