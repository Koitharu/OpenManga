package org.nv95.openmanga;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
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
    private final ImageButton mImageButtonNav;
    private final View mOptionsBlock;
    private final View mTitleBlock;
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
        mImageButtonNav = (ImageButton) view.findViewById(R.id.imageButton_goto);
        mImageButtonNav.setOnClickListener(this);
        mTitleBlock = view.findViewById(R.id.block_title);
        mTitleBlock.setOnClickListener(this);
        mOptionsBlock = view.findViewById(R.id.block_options);
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
            default:
                mDialog.dismiss();
                if (mCallback != null) {
                    mCallback.onClick(v);
                }
        }
    }
}
