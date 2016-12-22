package org.nv95.openmanga.components;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StyleRes;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;

import org.nv95.openmanga.R;
import org.nv95.openmanga.utils.AnimUtils;

/**
 * Created by nv95 on 22.12.16.
 */

public class SearchLayout extends FrameLayout implements TextWatcher, View.OnFocusChangeListener {

    private EditText mEditText;
    private ImageView mImageViewClear;
    private boolean mClearVisible;
    @Nullable
    private OnFocusChangeListener mFocusChangeListener;

    public SearchLayout(@NonNull Context context) {
        super(context);
        init(context);
    }

    public SearchLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public SearchLayout(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public SearchLayout(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr, @StyleRes int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context);
    }

    private void init(Context context) {
        LayoutInflater.from(context)
                .inflate(R.layout.layout_search, this, true);
        mEditText = (EditText) findViewById(R.id.editTextQuery);
        mImageViewClear = (ImageView) findViewById(R.id.image_clear);
        mFocusChangeListener = null;
        mClearVisible = false;
        mImageViewClear.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                mEditText.getText().clear();
            }
        });
        mEditText.addTextChangedListener(this);
        mEditText.setOnFocusChangeListener(this);
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        if (TextUtils.isEmpty(charSequence)) {
            if (mClearVisible) {
                AnimUtils.zooma(mImageViewClear, null);
                mClearVisible = false;
            }
        } else {
            if (!mClearVisible) {
                AnimUtils.zooma(null, mImageViewClear);
                mClearVisible = true;
            }
        }
    }

    @Override
    public void afterTextChanged(Editable editable) {

    }

    @Override
    public void onFocusChange(View view, boolean b) {
        if (!b) {
            if (mClearVisible) {
                AnimUtils.zooma(mImageViewClear, null);
                mClearVisible = false;
            }
        } else {
            onTextChanged(mEditText.getText(), 0 ,0 ,0);
        }
        if (mFocusChangeListener != null) {
            mFocusChangeListener.onFocusChange(view, b);
        }
    }

    @Nullable
    public EditText getEditText() {
        return mEditText;
    }

    public void setOnEditFocusChangeListener(@Nullable OnFocusChangeListener listener) {
        mFocusChangeListener = listener;
    }
}
