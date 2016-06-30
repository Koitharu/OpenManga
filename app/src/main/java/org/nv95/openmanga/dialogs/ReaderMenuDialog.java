package org.nv95.openmanga.dialogs;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.nv95.openmanga.R;
import org.nv95.openmanga.activities.SettingsActivity;
import org.nv95.openmanga.utils.LayoutUtils;

/**
 * Created by nv95 on 12.02.16.
 */
public class ReaderMenuDialog implements View.OnClickListener, DialogInterface.OnDismissListener,
        DialogInterface.OnCancelListener {

    public static final int REQUEST_SETTINGS = 1299;

    private final AppCompatActivity mActivity;
    private final Dialog mDialog;
    private final TextView mTextViewTitle;
    private final TextView mTextViewSubtitle;
    private final TextView mButtonFav;
    private final TextView mButtonSave;
    private final TextView mButtonShare;
    private final TextView mButtonOpts;
    private final TextView mButtonImg;
    private final TextView mButtonNav;
    private final ProgressBar mProgressBar;
    @Nullable
    private View.OnClickListener mCallback;
    private OnDismissListener onDismissListener;

    @SuppressLint("InflateParams")
    public ReaderMenuDialog(AppCompatActivity activity, boolean dark) {
        mActivity = activity;
        View view = LayoutInflater.from(activity)
                .inflate(R.layout.dialog_reader, null);
        mTextViewTitle = (TextView) view.findViewById(R.id.textView_title);
        mTextViewSubtitle = (TextView) view.findViewById(R.id.textView_subtitle);
        mTextViewSubtitle.setOnClickListener(this);
        mButtonFav = (TextView) view.findViewById(R.id.button_fav);
        mButtonFav.setOnClickListener(this);
        mButtonSave = (TextView) view.findViewById(R.id.button_save);
        mButtonSave.setOnClickListener(this);
        mButtonShare = (TextView) view.findViewById(R.id.button_share);
        mButtonShare.setOnClickListener(this);
        mButtonOpts = (TextView) view.findViewById(R.id.button_opt);
        mButtonOpts.setOnClickListener(this);
        mButtonImg = (TextView) view.findViewById(R.id.button_img);
        mButtonImg.setOnClickListener(this);
        mButtonNav = (TextView) view.findViewById(R.id.textView_goto);
        mButtonNav.setOnClickListener(this);
        mProgressBar = (ProgressBar) view.findViewById(R.id.progressBar);
        //preferences
        if (dark) {
            LayoutUtils.setAllImagesColor((ViewGroup) view, R.color.white_overlay_85);
        }
        mDialog = new AlertDialog.Builder(mActivity)
                .setView(view)
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
        mProgressBar.setMax(max - 1);
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
                mDialog.dismiss();
                mActivity.startActivityForResult(new Intent(mActivity, SettingsActivity.class)
                        .putExtra("section", SettingsActivity.SECTION_READER), REQUEST_SETTINGS);
                break;
            default:
                if (mCallback != null) {
                    mCallback.onClick(v);
                }
                mDialog.dismiss();
        }
    }

    public ReaderMenuDialog setOnDismissListener(OnDismissListener onDismissListener){
        this.onDismissListener = onDismissListener;
        return this;
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
