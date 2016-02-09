package org.nv95.openmanga;

import android.content.Context;
import android.content.DialogInterface;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;

/**
 * Created by nv95 on 09.02.16.
 */
public class ListModeDialog {
    private final AlertDialog mDialog;
    private OnListModeListener mListModeListener;

    public ListModeDialog(final Context context) {
        int mode = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext())
                .getInt("view_mode", 0);
        mDialog = new AlertDialog.Builder(context)
                .setSingleChoiceItems(R.array.view_modes, mode, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        setViewMode(which);
                        PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext())
                                .edit().putInt("view_mode", which).apply();
                        dialog.dismiss();
                    }
                })
                .create();
    }

    public void show(OnListModeListener callback) {
        mListModeListener = callback;
        mDialog.show();
    }

    private void setViewMode(int which) {
        if (mListModeListener != null) {
            mListModeListener.onListModeChanged(which != 0, which - 1);
        }
    }

    public interface OnListModeListener {
        void onListModeChanged(boolean grid, int sizeMode);
    }
}
