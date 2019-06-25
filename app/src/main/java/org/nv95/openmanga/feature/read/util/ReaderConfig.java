package org.nv95.openmanga.feature.read.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import org.nv95.openmanga.feature.manga.domain.MangaInfo;
import org.nv95.openmanga.providers.HistoryProvider;

/**
 * Created by nv95 on 18.11.16.
 */

@SuppressWarnings("unused")
public class ReaderConfig {

    public static final int MODE_PAGES = 0;
    public static final int MODE_SCROLL = 1;
    public static final int DIRECTION_DEFAULT = 0;
    public static final int DIRECTION_VERTICAL = 1;
    public static final int DIRECTION_REVERSED = 2;
    public static final int PRELOAD_DISABLED = 0;
    public static final int PRELOAD_WLAN_ONLY = 1;
    public static final int PRELOAD_ALWAYS = 2;
    public static final int SCALE_FIT = 0;
    public static final int SCALE_FIT_W = 1;
    public static final int SCALE_FIT_H = 2;
    public static final int SCALE_FIT_H_REV = 5;
    public static final int SCALE_ZOOM = 3;

    public final boolean keepScreenOn;
    public final boolean scrollByVolumeKeys;
    public final boolean adjustBrightness;
    public final int brightnessValue;
    public final int scrollDirection;
    public final int mode;
    public final int preload;
    public final int scaleMode;
    public final boolean tapNavs;
    public final boolean hideMenuButton;
    public final boolean showNumbers;

    private ReaderConfig(SharedPreferences prefs, boolean isWeb) {
        keepScreenOn = prefs.getBoolean("keep_screen", true);
        scrollByVolumeKeys = prefs.getBoolean("volkeyscroll", false);
        adjustBrightness = prefs.getBoolean("brightness", false);
        hideMenuButton = prefs.getBoolean("hide_menu", false);
        brightnessValue = prefs.getInt("brightness_value", 20);
        tapNavs = prefs.getBoolean("tap_navs", false);
        scrollDirection = isWeb ? DIRECTION_VERTICAL : Integer.parseInt(prefs.getString("direction", "0"));
        mode = Integer.parseInt(prefs.getString("r2_mode", "0"));
        preload = Integer.parseInt(prefs.getString("preload", "1"));
        int scalemode = isWeb ? SCALE_FIT_W : Integer.parseInt(prefs.getString("scalemode", "0"));
        if (scalemode == SCALE_FIT_H && scrollDirection == DIRECTION_REVERSED) {
            scalemode = SCALE_FIT_H_REV;
        }
        scaleMode = scalemode;
        showNumbers = prefs.getBoolean("show_numbers", true);
    }

    public static ReaderConfig load(Context context) {
        return new ReaderConfig(
                PreferenceManager.getDefaultSharedPreferences(context),
                false
        );
    }

    public static ReaderConfig load(Context context, MangaInfo manga) {
        return new ReaderConfig(
                PreferenceManager.getDefaultSharedPreferences(context),
                HistoryProvider.getInstance(context).isWebMode(manga)
        );
    }
}
