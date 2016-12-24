package org.nv95.openmanga.helpers;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;

import org.nv95.openmanga.R;

/**
 * Created by nv95 on 09.02.16.
 */
public class ListModeHelper implements SharedPreferences.OnSharedPreferenceChangeListener {

    private static final String PREF_KEY = "view_mode";

    private static final int LISTMODE_LIST = 0;
    private static final int LISTMODE_GRID_SMALL = 1;
    private static final int LISTMODE_GRID_MEDIUM = 2;
    private static final int LISTMODE_GRID_LARGE = 3;

    private static final int[] ITEMS_IDS = new int[] {
            R.id.listmode_list,
            R.id.listmode_grid_small,
            R.id.listmode_grid_medium,
            R.id.listmode_grid_large
    };

    private final SharedPreferences mPreferences;
    private final OnListModeListener mListModeListener;

    public ListModeHelper(final Context context, OnListModeListener callback) {
        mListModeListener = callback;
        mPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem item = menu.findItem(R.id.action_listmode);
        if (item == null) {
            return false;
        }
        Menu submenu = item.getSubMenu();
        if (submenu == null) {
            return false;
        }
        int mode = mPreferences.getInt(PREF_KEY, 0);
        submenu.findItem(ITEMS_IDS[mode]).setChecked(true);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.listmode_list:
                mPreferences.edit().putInt(PREF_KEY, LISTMODE_LIST).apply();
                return true;
            case R.id.listmode_grid_small:
                mPreferences.edit().putInt(PREF_KEY, LISTMODE_GRID_SMALL).apply();
                return true;
            case R.id.listmode_grid_medium:
                mPreferences.edit().putInt(PREF_KEY, LISTMODE_GRID_MEDIUM).apply();
                return true;
            case R.id.listmode_grid_large:
                mPreferences.edit().putInt(PREF_KEY, LISTMODE_GRID_LARGE).apply();
                return true;
            default:
                return false;
        }
    }

    public void applyCurrent() {
        onSharedPreferenceChanged(mPreferences, PREF_KEY);
    }

    public void enable() {
        mPreferences.registerOnSharedPreferenceChangeListener(this);
    }

    public void disable() {
        mPreferences.unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (PREF_KEY.equals(key)) {
            final int mode = sharedPreferences.getInt(PREF_KEY, 0);
            mListModeListener.onListModeChanged(mode != LISTMODE_LIST, mode - 1);
        }
    }

    public interface OnListModeListener {
        void onListModeChanged(boolean grid, int sizeMode);
    }
}
