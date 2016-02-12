package org.nv95.openmanga.helpers;

import android.os.Handler;
import android.support.annotation.StringRes;
import android.support.design.widget.Snackbar;
import android.view.View;

import org.nv95.openmanga.R;

/**
 * Created by nv95 on 12.02.16.
 */
public abstract class UndoHelper implements View.OnClickListener, Runnable {
    private final boolean mAsync;

    public UndoHelper(boolean async) {
        mAsync = async;
    }

    public void snackbar(View view, @StringRes int message, @Snackbar.Duration int duration) {
        Snackbar.make(view, message,duration)
                .setAction(R.string.undo, this)
                .setCallback(new ActionCallback())
                .show();
    }

    @Override
    public void onClick(View v) {
        onUndo();
    }

    private class ActionCallback extends Snackbar.Callback {
        @Override
        public void onDismissed(Snackbar snackbar, int event) {
            super.onDismissed(snackbar, event);
            if (event != DISMISS_EVENT_ACTION) {
                if (mAsync) {
                    new Handler().post(UndoHelper.this);
                } else {
                    UndoHelper.this.run();
                }
            }
        }
    }

    public abstract void onUndo();
}
