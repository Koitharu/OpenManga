package org.nv95.openmanga.components;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import com.google.android.material.snackbar.Snackbar;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.nv95.openmanga.R;
import org.nv95.openmanga.utils.LayoutUtils;

/**
 * Created by admin on 25.07.17.
 */

public class OnboardSnackbar implements View.OnClickListener {

    private final Snackbar mSnackbar;
    private final View mView;
    @Nullable
    private View.OnClickListener mActionClickListener;


    private OnboardSnackbar(Snackbar snackbar) {
        mSnackbar = snackbar;
        Snackbar.SnackbarLayout layout = (Snackbar.SnackbarLayout) snackbar.getView();
        mView = LayoutInflater.from(layout.getContext())
                .inflate(R.layout.snackbar_onboard, null);
        layout.addView(mView, 0);
        layout.setBackgroundColor(LayoutUtils.isAppThemeDark(snackbar.getContext()) ? Color.BLACK : Color.WHITE);
        mView.findViewById(android.R.id.button1).setOnClickListener(this);
        mView.findViewById(android.R.id.button2).setOnClickListener(this);
        ((TextView)mView.findViewById(android.R.id.text1)).setTextColor(LayoutUtils.isAppThemeDark(layout.getContext()) ? Color.WHITE : Color.BLACK);
    }

    public OnboardSnackbar setText(@StringRes int resId) {
        ((TextView)mView.findViewById(android.R.id.text1)).setText(resId);
        return this;
    }

    public OnboardSnackbar setAction(@StringRes int resId, View.OnClickListener onClickListener) {
        Button b = mView.findViewById(android.R.id.button1);
        b.setText(resId);
        mActionClickListener = onClickListener;
        return this;
    }

    public OnboardSnackbar setDiscardText(@StringRes int resId) {
        Button b = mView.findViewById(android.R.id.button2);
        b.setText(resId);
        return this;
    }

    public void show() {
        mSnackbar.show();
    }

    public static OnboardSnackbar make(@NonNull View view, @StringRes int resId, int duration) {
        return new OnboardSnackbar(Snackbar.make(view, "", duration)).setText(resId);
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == android.R.id.button1 && mActionClickListener != null) {
            mActionClickListener.onClick(view);
        }
        mSnackbar.dismiss();
    }

    public static boolean askOnce(View view, @StringRes int text, @StringRes int discardText,
                                  @StringRes int acceptText, View.OnClickListener onClickListener) {
        if (view != null && view.getVisibility() == View.VISIBLE
                && !view.getContext().getSharedPreferences("tips", Context.MODE_PRIVATE).getBoolean("s__" + text, false)) {

            OnboardSnackbar.make(view, text, Snackbar.LENGTH_INDEFINITE)
                    .setAction(acceptText, onClickListener)
                    .setDiscardText(discardText)
                    .show();

            SharedPreferences prefs = view.getContext().getSharedPreferences("tips", Context.MODE_PRIVATE);
            prefs.edit().putBoolean("s__" + text, true).apply();
            return true;
        } else {
            return false;
        }
    }
}
