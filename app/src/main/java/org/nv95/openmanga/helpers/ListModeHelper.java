package org.nv95.openmanga.helpers;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;

import org.nv95.openmanga.R;

/**
 * Created by nv95 on 09.02.16.
 */
public class ListModeHelper implements SharedPreferences.OnSharedPreferenceChangeListener {
    private static final String PREF_KEY = "view_mode";
    private final Context mContext;
    private final OnListModeListener mListModeListener;

    public ListModeHelper(final Context context, OnListModeListener callback) {
        mListModeListener = callback;
        mContext = context;
    }

    public void showDialog() {
        final int mode = PreferenceManager.getDefaultSharedPreferences(mContext)
                .getInt(PREF_KEY, 0);
        new AlertDialog.Builder(mContext)
                .setSingleChoiceItems(R.array.view_modes, mode, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        PreferenceManager.getDefaultSharedPreferences(mContext)
                                .edit().putInt(PREF_KEY, which).commit();
                        dialog.dismiss();
                    }
                })
                .create().show();
    }

    public void applyCurrent() {
        onSharedPreferenceChanged(PreferenceManager.getDefaultSharedPreferences(mContext), PREF_KEY);
    }

    public void enable() {
        PreferenceManager.getDefaultSharedPreferences(mContext)
                .registerOnSharedPreferenceChangeListener(this);
    }

    public void disable() {
        PreferenceManager.getDefaultSharedPreferences(mContext)
                .unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (PREF_KEY.equals(key)) {
            final int mode = sharedPreferences.getInt(PREF_KEY, 0);
            mListModeListener.onListModeChanged(mode != 0, mode - 1);
        }
    }

    public interface OnListModeListener {
        void onListModeChanged(boolean grid, int sizeMode);
    }
}
