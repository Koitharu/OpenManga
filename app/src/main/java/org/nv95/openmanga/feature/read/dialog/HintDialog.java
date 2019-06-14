package org.nv95.openmanga.feature.read.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import androidx.annotation.StringRes;

/**
 * Created by nv95 on 05.12.16.
 */

public class HintDialog {

    private final Dialog mDialog;

    private HintDialog(Context context, @StringRes int text) {
        mDialog = new AlertDialog.Builder(context)
                .setMessage(text)
                .setCancelable(true)
                .setPositiveButton(android.R.string.ok, null)
                .create();
    }

    private void show() {
        mDialog.show();
    }

    public static boolean showOnce(Context context, @StringRes int hint) {
        SharedPreferences prefs = context.getSharedPreferences("tips", Context.MODE_PRIVATE);
        final String key = "tip_" + hint;
        if (prefs.getBoolean(key, false)) {
            return false;
        }
        new HintDialog(context, hint).show();
        prefs.edit()
                .putBoolean(key, true)
                .apply();
        return true;
    }
}
