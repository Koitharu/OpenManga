package org.nv95.openmanga.components;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import org.nv95.openmanga.R;

/**
 * Created by nv95 on 13.01.16.
 */
public class BottomSheetDialog extends Dialog implements DialogInterface {

    private final Context mContext;
    private final ListView mListView;
    private final View mButtonBar;
    private final TextView mTextViewTitle;
    private final Button[] mButtons;
    private int mHeaders;
    private final OnClickListener[] mClickListeners;
    private final View.OnClickListener mOnButtonClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.button_neutral:
                    if (mClickListeners[0] != null) {
                        mClickListeners[0].onClick(BottomSheetDialog.this, BUTTON_NEUTRAL);
                    }
                    break;
                case R.id.button_negative:
                    dismiss();
                    if (mClickListeners[1] != null) {
                        mClickListeners[1].onClick(BottomSheetDialog.this, BUTTON_NEGATIVE);
                    }
                    break;
                case R.id.button_positive:
                    dismiss();
                    if (mClickListeners[2] != null) {
                        mClickListeners[2].onClick(BottomSheetDialog.this, BUTTON_POSITIVE);
                    }
                    break;
            }
        }
    };

    public BottomSheetDialog(Context context) {
        super(context, R.style.MaterialDialogSheet);
        mHeaders = 0;
        mContext = context;
        ClosableSlidingLayout view = (ClosableSlidingLayout) View.inflate(context, R.layout.bottomsheet, null);

        view.setSlideListener(new ClosableSlidingLayout.SlideListener() {
            @Override
            public void onClosed() {
                BottomSheetDialog.this.dismiss();
            }

            @Override
            public void onOpened() {
                
            }
        });
        view.mTarget = mListView = (ListView) view.findViewById(R.id.listView);
        mButtonBar = view.findViewById(R.id.buttonbar);
        mTextViewTitle = (TextView) view.findViewById(R.id.textView_title);
        mButtons = new Button[]{
                (Button) view.findViewById(R.id.button_neutral),
                (Button) view.findViewById(R.id.button_negative),
                (Button) view.findViewById(R.id.button_positive)
        };
        mClickListeners = new OnClickListener[3];
        setContentView(view);
        setCancelable(true);
        setCanceledOnTouchOutside(true);
        getWindow().setLayout(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        getWindow().setGravity(Gravity.BOTTOM);
    }

    public BottomSheetDialog setPositiveButton(String text, @Nullable OnClickListener onClickListener) {
        mButtonBar.setVisibility(View.VISIBLE);
        mButtons[2].setVisibility(View.VISIBLE);
        mButtons[2].setText(text);
        mClickListeners[2] = onClickListener;
        mButtons[2].setOnClickListener(mOnButtonClick);
        return this;
    }

    public BottomSheetDialog setPositiveButton(int textId, @Nullable OnClickListener onClickListener) {
        return setPositiveButton(mContext.getString(textId), onClickListener);
    }

    public BottomSheetDialog setNegativeButton(String text, @Nullable OnClickListener onClickListener) {
        mButtonBar.setVisibility(View.VISIBLE);
        mButtons[1].setVisibility(View.VISIBLE);
        mButtons[1].setText(text);
        mClickListeners[1] = onClickListener;
        mButtons[1].setOnClickListener(mOnButtonClick);
        return this;
    }

    public BottomSheetDialog setNegativeButton(int textId, @Nullable OnClickListener onClickListener) {
        return setNegativeButton(mContext.getString(textId), onClickListener);
    }

    public BottomSheetDialog setNeutralButton(String text, @Nullable OnClickListener onClickListener) {
        mButtonBar.setVisibility(View.VISIBLE);
        mButtons[0].setVisibility(View.VISIBLE);
        mButtons[0].setText(text);
        mClickListeners[0] = onClickListener;
        mButtons[0].setOnClickListener(mOnButtonClick);
        return this;
    }

    public BottomSheetDialog setNeutralButton(int textId, @Nullable OnClickListener onClickListener) {
        return setNeutralButton(mContext.getString(textId), onClickListener);
    }

    public BottomSheetDialog setAdapter(ListAdapter adapter) {
        mListView.setAdapter(adapter);
        return this;
    }

    public BottomSheetDialog setItems(String[] items, @LayoutRes int layoutId) {
        mListView.setChoiceMode(AbsListView.CHOICE_MODE_NONE);
        mListView.setAdapter(new ArrayAdapter<>(mContext, layoutId, items));
        return this;
    }

    public BottomSheetDialog setOnItemClickListener(final DialogInterface.OnClickListener listener) {
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                listener.onClick(BottomSheetDialog.this, position - mHeaders);
            }
        });
        return this;
    }

    public BottomSheetDialog setOnItemCheckListener(final DialogInterface.OnMultiChoiceClickListener listener) {
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                listener.onClick(BottomSheetDialog.this, position - mHeaders, mListView.isItemChecked(position));
            }
        });
        return this;
    }

    public BottomSheetDialog setSheetTitle(@StringRes int resId) {
        mTextViewTitle.setVisibility(View.VISIBLE);
        mTextViewTitle.setText(resId);
        return this;
    }

    public BottomSheetDialog setMultiChoiceItems(String[] items, boolean[] checkedItems) {
        mListView.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE);
        mListView.setAdapter(new ArrayAdapter<>(mContext, R.layout.item_multiple_choice, items));
        for (int i = 0; i < checkedItems.length; i++) {
            mListView.setItemChecked(i + mHeaders, checkedItems[i]);
        }
        return this;
    }

    public void checkAll(boolean checked) {
        for (int i = mListView.getCount() - 1; i >= mHeaders; i--) {
            mListView.setItemChecked(i, checked);
        }
    }

    public BottomSheetDialog addHeader(String itemTitle, @Nullable String buttonPositive,
                                       @Nullable String buttonNeutral, final OnClickListener callback) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.bottomsheet_header, mListView, false);
        ((TextView)view.findViewById(android.R.id.text1)).setText(itemTitle);
        Button button;
        if (buttonPositive != null) {
            button = (Button) view.findViewById(R.id.button_positive);
            button.setVisibility(View.VISIBLE);
            button.setText(buttonPositive);
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    callback.onClick(BottomSheetDialog.this, BUTTON_POSITIVE);
                }
            });
        }
        if (buttonNeutral != null) {
            button = (Button) view.findViewById(R.id.button_neutral);
            button.setVisibility(View.VISIBLE);
            button.setText(buttonNeutral);
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    callback.onClick(BottomSheetDialog.this, BUTTON_NEUTRAL);
                }
            });
        }
        mListView.addHeaderView(view);
        mHeaders++;
        return this;
    }

    public BottomSheetDialog addHeader(String itemTitle, @StringRes int buttonPositive,
                                       @StringRes int buttonNeutral, OnClickListener callback) {
        return addHeader(itemTitle,
                buttonPositive == 0 ? null : mContext.getString(buttonPositive),
                buttonNeutral == 0 ? null : mContext.getString(buttonNeutral),
                callback);
    }
}
