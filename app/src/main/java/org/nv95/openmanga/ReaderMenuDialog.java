package org.nv95.openmanga;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.AppCompatSpinner;
import android.support.v7.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

/**
 * Created by nv95 on 12.02.16.
 */
public class ReaderMenuDialog implements View.OnClickListener {
    private final Dialog mDialog;
    private final TextView mTextViewTitle;
    private final TextView mTextViewSubtitle;
    private final Button mButtonFav;
    private final Button mButtonSave;
    private final Button mButtonShare;
    private final Button mButtonOpts;
    private final Button mButtonImg;
    private final Button mButtonNav;
    private final View mOptionsBlock;
    private final View mTitleBlock;
    private final ProgressBar mProgressBar;

    private final SwitchCompat mSwitchBrightness;
    private final SwitchCompat mSwitchKeepscreen;
    private final SwitchCompat mSwitchScrollvolume;
    private final AppCompatSpinner mSpinnerDirection;
    @Nullable
    private View.OnClickListener mCallback;

    @SuppressLint("InflateParams")
    public ReaderMenuDialog(Context context) {
        final View view = LayoutInflater.from(context)
                .inflate(R.layout.dialog_reader, null);
        mTextViewTitle = (TextView) view.findViewById(R.id.textView_title);
        mTextViewSubtitle = (TextView) view.findViewById(R.id.textView_subtitle);
        mButtonFav = (Button) view.findViewById(R.id.button_fav);
        mButtonFav.setOnClickListener(this);
        mButtonSave = (Button) view.findViewById(R.id.button_save);
        mButtonSave.setOnClickListener(this);
        mButtonShare = (Button) view.findViewById(R.id.button_share);
        mButtonShare.setOnClickListener(this);
        mButtonOpts = (Button) view.findViewById(R.id.button_opt);
        mButtonOpts.setOnClickListener(this);
        mButtonImg = (Button) view.findViewById(R.id.button_img);
        mButtonImg.setOnClickListener(this);
        mButtonNav = (Button) view.findViewById(R.id.imageButton_goto);
        mButtonNav.setOnClickListener(this);
        mProgressBar = (ProgressBar) view.findViewById(R.id.progressBar);
        mTitleBlock = view.findViewById(R.id.block_title);
        mTitleBlock.setOnClickListener(this);
        //preferences
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        mSwitchScrollvolume = (SwitchCompat) view.findViewById(R.id.switch_volkeyscroll);
        mSwitchScrollvolume.setChecked(prefs.getBoolean("volkeyscroll", false));
        mSwitchKeepscreen = (SwitchCompat) view.findViewById(R.id.switch_keepscreen);
        mSwitchKeepscreen.setChecked(prefs.getBoolean("keep_screen", true));
        mSwitchBrightness = (SwitchCompat) view.findViewById(R.id.switch_brightness);
        mSwitchBrightness.setChecked(prefs.getBoolean("brightness", false));
        mSpinnerDirection = (AppCompatSpinner) view.findViewById(R.id.spinner_direction);
        mSpinnerDirection.setSelection(Integer.parseInt(prefs.getString("direction","0")));
        mOptionsBlock = view.findViewById(R.id.block_options);
        view.findViewById(R.id.button_positive).setOnClickListener(this);
        mDialog = new AlertDialog.Builder(context)
                .setView(view)
                .setCancelable(true)
                .create();
    }

    public ReaderMenuDialog callback(View.OnClickListener clickListener) {
        mCallback = clickListener;
        return this;
    }

    public ReaderMenuDialog chapter(String name) {
        mTextViewSubtitle.setText(name);
        return this;
    }

    public ReaderMenuDialog title(String title) {
        mTextViewTitle.setText(title);
        return this;
    }

    public ReaderMenuDialog favourites(@Nullable String title) {
        if (title != null) {
            mButtonFav.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.ic_favorite_dark, 0, 0, 0
            );
            mButtonFav.setText(title);
        }
        return this;
    }

    @SuppressLint("SetTextI18n")
    public ReaderMenuDialog progress(int progress, int max) {
        mProgressBar.setMax(max);
        mProgressBar.setProgress(progress);
        mButtonNav.setText((progress+1) + "/" + max);
        return this;
    }

    public void show() {
        mDialog.show();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_opt:
                if (mOptionsBlock.getVisibility() == View.VISIBLE) {
                    mOptionsBlock.setVisibility(View.GONE);
                    mButtonOpts.setCompoundDrawablesWithIntrinsicBounds(
                            R.drawable.ic_settings_dark, 0, R.drawable.ic_drop_down_dark, 0
                    );
                } else {
                    mOptionsBlock.setVisibility(View.VISIBLE);
                    mButtonOpts.setCompoundDrawablesWithIntrinsicBounds(
                            R.drawable.ic_settings_dark, 0, R.drawable.ic_drop_up_dark, 0
                    );
                }
                break;
            case R.id.button_positive:
                PreferenceManager.getDefaultSharedPreferences(v.getContext()).edit()
                        .putString("direction", String.valueOf(mSpinnerDirection.getSelectedItemPosition()))
                        .putBoolean("keep_screen", mSwitchKeepscreen.isChecked())
                        .putBoolean("volkeyscroll", mSwitchScrollvolume.isChecked())
                        .commit();
            default:
                if (mCallback != null) {
                    mCallback.onClick(v);
                }
                mDialog.dismiss();
        }
    }
}
