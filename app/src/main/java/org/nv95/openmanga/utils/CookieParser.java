package org.nv95.openmanga.utils;

import java.util.HashMap;
import java.util.List;

/**
 * Created by nv95 on 21.11.16.
 */

public class CookieParser {

    private final HashMap<String,String> mCookies;

    public CookieParser(List<String> cookies) {
        mCookies = new HashMap<>();
        String[] sa ,sa1;
        for (String o : cookies) {
            sa = o.split(";\\s{0,}");
            for (String o1 : sa) {
                sa1 = o1.split("=");
                if (sa1.length == 2) {
                    mCookies.put(sa1[0], sa1[1]);
                }
            }
        }
    }

    public String toString(String... values) {
        StringBuilder res = new StringBuilder();
        for (String key : values) {
            res.append(key).append("=").append(mCookies.get(key)).append("; ");
        }
        return res.substring(0, res.length() - 2);
    }
}
