package org.nv95.openmanga.providers;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.annotation.WorkerThread;
import android.text.TextUtils;

import org.nv95.openmanga.items.MangaInfo;
import org.nv95.openmanga.utils.AppHelper;
import org.nv95.openmanga.utils.NetworkUtils;
import org.nv95.openmanga.utils.XmlUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.lang.ref.SoftReference;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by unravel22 on 26.02.17.
 */

public class MALProvider {
    
    @Nullable
    private static SoftReference<MALProvider> sInstance = null;
    
    private final String mLogin;
    private final String mPassword;
    
    public static MALProvider getInstance(Context context) {
        MALProvider instance = null;
        if (sInstance != null) {
            instance = sInstance.get();
        }
        if (instance == null) {
            instance = new MALProvider(context);
        }
        return instance;
    }
    
    public MALProvider(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        mLogin = preferences.getString("mal.login", null);
        mPassword = preferences.getString("mal.password", null);
    }
    
    public boolean isAuthorized() {
        return !TextUtils.isEmpty(mLogin) && !TextUtils.isEmpty(mPassword);
    }
    
    @Nullable
    @WorkerThread
    public List<MALManga> findEquals(MangaInfo subject) {
        try {
            String name = AppHelper.isEnglish(subject.name) || TextUtils.isEmpty(subject.subtitle) ? subject.name : subject.subtitle;
            return search(name);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    @WorkerThread
    public List<MALManga> search(String query) throws Exception {
        return convertResults(XmlUtils.convertNodesFromXml(
                NetworkUtils.getRaw("https://myanimelist.net/api/manga/search.xml?q=" + URLEncoder.encode(query, "UTF-8"), mLogin, mPassword)
        ));
    }
    
    private static List<MALManga> convertResults(Document xml) {
        ArrayList<MALManga> result = new ArrayList<>();
        NodeList entries = xml.getElementsByTagName("entry");
        Node o;
        MALManga item;
        for (int i=0; i<entries.getLength();i++) {
            o = entries.item(i);
            if (o instanceof Element) {
                try {
                    item = new MALManga();
                    item.id = Integer.parseInt(XmlUtils.getElementValue((Element) o, "id"));
                    item.title = XmlUtils.getElementValue((Element) o, "title");
                    item.english = XmlUtils.getElementValue((Element) o, "english");
                    item.synopsis = XmlUtils.getElementValue((Element) o, "synopsis");
                    item.score = (int) (Double.parseDouble(XmlUtils.getElementValue((Element) o, "score")) * 100);
                    result.add(item);
                } catch (Exception ignored) {
                }
            }
        }
        return result;
    }
    
    public static boolean checkCredentials(String login, String password) {
        try {
            return !TextUtils.isEmpty(NetworkUtils.getRaw("https://myanimelist.net/api/account/verify_credentials.xml",
                    login, password));
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    public static class MALManga {
        public int id;
        public String title;
        public String english;
        public String synopsis;
        public int score;
    }
    
    @Nullable
    public static Boolean getConfig(Context context, String key, boolean defValue) {
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        if (!prefs.contains("mal.password")) {
            return null;
        }
        return prefs.getBoolean("mal." + key, defValue);
    }
}
