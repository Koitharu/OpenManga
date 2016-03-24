package org.nv95.openmanga;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.AppCompatSeekBar;
import android.support.v7.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;

import org.nv95.openmanga.helpers.BrightnessHelper;

/**
 * Created by nv95 on 12.02.16.
 */
public class ReaderMenuDialog implements View.OnClickListener, SeekBar.OnSeekBarChangeListener, DialogInterface.OnDismissListener, DialogInterface.OnCancelListener {
    private static final int[] checkIds = new int[] {R.id.check_ltr, R.id.check_ttb, R.id.check_rtl};

    private final Dialog mDialog;
    private final ScrollView mScrollView;
    private final TextView mTextViewTitle;
    private final TextView mTextViewSubtitle;
    private final TextView mButtonFav;
    private final TextView mButtonSave;
    private final TextView mButtonShare;
    private final TextView mButtonOpts;
    private final TextView mButtonImg;
    private final TextView mButtonNav;
    private final View mOptionsBlock;
    private final View mTitleBlock;
    private final ProgressBar mProgressBar;
    private final Button mButtonApply;

    private final SwitchCompat mSwitchBrightness;
    private final SwitchCompat mSwitchKeepscreen;
    private final SwitchCompat mSwitchScrollvolume;
    private final AppCompatSeekBar mSeekBarBrightness;
    private final RadioGroup mRadioGroupDirections;
    @Nullable
    private View.OnClickListener mCallback;
    @Nullable
    private BrightnessHelper mBrightnessHelper;
    private OnDismissListener onDismissListener;

    @SuppressLint("InflateParams")
    public ReaderMenuDialog(Context context) {
        mScrollView = (ScrollView) LayoutInflater.from(context)
                .inflate(R.layout.dialog_reader, null);
        mTextViewTitle = (TextView) mScrollView.findViewById(R.id.textView_title);
        mTextViewSubtitle = (TextView) mScrollView.findViewById(R.id.textView_subtitle);
        mButtonFav = (TextView) mScrollView.findViewById(R.id.button_fav);
        mButtonFav.setOnClickListener(this);
        mButtonSave = (TextView) mScrollView.findViewById(R.id.button_save);
        mButtonSave.setOnClickListener(this);
        mButtonShare = (TextView) mScrollView.findViewById(R.id.button_share);
        mButtonShare.setOnClickListener(this);
        mButtonOpts = (TextView) mScrollView.findViewById(R.id.button_opt);
        mButtonOpts.setOnClickListener(this);
        mButtonImg = (TextView) mScrollView.findViewById(R.id.button_img);
        mButtonImg.setOnClickListener(this);
        mButtonNav = (TextView) mScrollView.findViewById(R.id.imageButton_goto);
        mButtonNav.setOnClickListener(this);
        mProgressBar = (ProgressBar) mScrollView.findViewById(R.id.progressBar);
        mTitleBlock = mScrollView.findViewById(R.id.block_title);
        mTitleBlock.setOnClickListener(this);
        //preferences
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        mSwitchScrollvolume = (SwitchCompat) mScrollView.findViewById(R.id.switch_volkeyscroll);
        mSwitchScrollvolume.setChecked(prefs.getBoolean("volkeyscroll", false));
        mSwitchKeepscreen = (SwitchCompat) mScrollView.findViewById(R.id.switch_keepscreen);
        mSwitchKeepscreen.setChecked(prefs.getBoolean("keep_screen", true));
        mSwitchBrightness = (SwitchCompat) mScrollView.findViewById(R.id.switch_brightness);
        mSwitchBrightness.setChecked(prefs.getBoolean("brightness", false));
        mSwitchBrightness.setOnClickListener(this);
        mSeekBarBrightness = (AppCompatSeekBar) mScrollView.findViewById(R.id.seekBar_brightness);
        mSeekBarBrightness.setProgress(prefs.getInt("brightness_value", 20));
        mSeekBarBrightness.setEnabled(mSwitchBrightness.isChecked());
        mSeekBarBrightness.setOnSeekBarChangeListener(this);
        mRadioGroupDirections = (RadioGroup) mScrollView.findViewById(R.id.radioGroup_direction);
        int id = Integer.parseInt(prefs.getString("direction","0"));
        if (id < 0 || id > 2)  {
            id = 0;
        }
        mRadioGroupDirections.check(checkIds[id]);
        mOptionsBlock = mScrollView.findViewById(R.id.block_options);
        mButtonApply = (Button) mScrollView.findViewById(R.id.button_positive);
        mButtonApply.setOnClickListener(this);
        mDialog = new AlertDialog.Builder(context)
                .setView(mScrollView)
                .setCancelable(true)
                .setOnDismissListener(this)
                .setOnCancelListener(this)
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

    public ReaderMenuDialog brightnessHelper(BrightnessHelper helper) {
        mBrightnessHelper = helper;
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
                    mScrollView.post(new Runnable() {
                        @Override
                        public void run() {
                            mScrollView.scrollTo(0, mButtonApply.getBottom());
                        }
                    });
                }
                break;
            case R.id.switch_brightness:
                if (mSwitchBrightness.isChecked()) {
                    mSeekBarBrightness.setEnabled(true);
                    if (mBrightnessHelper != null) {
                        mBrightnessHelper.setBrightness(mSeekBarBrightness.getProgress());
                    }
                } else {
                    mSeekBarBrightness.setEnabled(false);
                    if (mBrightnessHelper != null) {
                        mBrightnessHelper.reset();
                    }
                }
                break;
            case R.id.button_positive:
                PreferenceManager.getDefaultSharedPreferences(v.getContext()).edit()
                        .putString("direction", (String) mScrollView.findViewById(mRadioGroupDirections.getCheckedRadioButtonId()).getTag())
                        .putBoolean("keep_screen", mSwitchKeepscreen.isChecked())
                        .putBoolean("volkeyscroll", mSwitchScrollvolume.isChecked())
                        .putBoolean("brightness", mSwitchBrightness.isChecked())
                        .putInt("brightness_value", mSeekBarBrightness.getProgress())
                        .commit();
            default:
                if (mCallback != null) {
                    mCallback.onClick(v);
                }
                mDialog.dismiss();
        }
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (mBrightnessHelper != null) {
            mBrightnessHelper.setBrightness(progress);
        }
    }

    public ReaderMenuDialog setOnDismissListener(OnDismissListener onDismissListener){
        this.onDismissListener = onDismissListener;
        return this;
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        if(onDismissListener !=null)
            onDismissListener.settingsDialogDismiss();
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        if(onDismissListener !=null)
            onDismissListener.settingsDialogDismiss();
    }

    public interface OnDismissListener {
        void settingsDialogDismiss();
    }
}
