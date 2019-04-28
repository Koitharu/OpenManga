package org.nv95.openmanga.components;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import androidx.annotation.AttrRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StyleRes;
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

public class SearchInput extends FrameLayout implements TextWatcher, View.OnFocusChangeListener {

    private EditText mEditText;
    private ImageView mImageViewClear;
    private boolean mClearVisible;
    @Nullable
    private OnFocusChangeListener mFocusChangeListener;
    @Nullable
    private OnTextChangedListener mTextChangedListener;

    public SearchInput(@NonNull Context context) {
        super(context);
        init(context);
    }

    public SearchInput(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public SearchInput(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public SearchInput(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr, @StyleRes int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context);
    }

    private void init(Context context) {
        LayoutInflater.from(context)
                .inflate(R.layout.layout_search, this, true);
        mEditText = findViewById(R.id.editTextQuery);
        mImageViewClear = findViewById(R.id.image_clear);
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
        updateClearButton(!TextUtils.isEmpty(charSequence) && mEditText.hasFocus());
        if (mTextChangedListener != null) {
            mTextChangedListener.onTextChanged(charSequence);
        }
    }

    @Override
    public void afterTextChanged(Editable editable) {

    }

    @Override
    public void onFocusChange(View view, boolean b) {
        updateClearButton(!TextUtils.isEmpty(mEditText.getText()) && b);
        if (mFocusChangeListener != null) {
            mFocusChangeListener.onFocusChange(view, b);
        }
    }

    private void updateClearButton(boolean show) {
        if (show != mClearVisible) {
            mClearVisible = show;
            if (show) {
                AnimUtils.zooma(null, mImageViewClear);
            } else {
                AnimUtils.zooma(mImageViewClear, null);
            }
        }
    }

    public EditText getEditText() {
        return mEditText;
    }

    public void setOnEditFocusChangeListener(@Nullable OnFocusChangeListener listener) {
        mFocusChangeListener = listener;
    }

    public void setOnTextChangedListener(@Nullable OnTextChangedListener listener) {
        mTextChangedListener = listener;
    }

    public void setText(CharSequence charSequence) {
        mEditText.setText(charSequence);
        mEditText.setSelection(mEditText.getText().length());
    }

    public interface OnTextChangedListener {
        void onTextChanged(CharSequence text);
    }
}
