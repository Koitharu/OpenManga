package org.nv95.openmanga.dialogs;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.design.widget.BottomSheetDialog;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import org.nv95.openmanga.R;
import org.nv95.openmanga.adapters.HeaderedAdapter;
import org.nv95.openmanga.utils.LayoutUtils;

/**
 * Created by nv95 on 09.08.16.
 */

public class BottomSheet implements DialogInterface {

    private final BottomSheetDialog mSheetDialog;
    private final RecyclerView mRecyclerView;
    private DialogInterface.OnClickListener mItemClickListener;
    private OnMultiChoiceClickListener mMultiChoiceClickListener;
    @Nullable
    private HeaderedAdapter mAdapter;

    public BottomSheet(Context context) {
        View view = LayoutInflater.from(context).inflate(R.layout.bottom_sheet, null);
        mRecyclerView = (RecyclerView) view.findViewById(R.id.recyclerView);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(context));

        mSheetDialog = new BottomSheetDialog(context);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = mSheetDialog.getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(ContextCompat.getColor(context, R.color.transparent_dark));
        }
        mSheetDialog.setContentView(view);
    }

    public BottomSheet setOnItemClickListener(final DialogInterface.OnClickListener listener) {
        mItemClickListener = listener;
        return this;
    }

    public BottomSheet setItems(String[] items, @LayoutRes int layoutId) {
        mRecyclerView.setAdapter(mAdapter = new BasicAdapter(items, layoutId));
        return this;
    }

    public BottomSheet addHeader(String itemTitle, @Nullable String buttonPositive,
                                          @Nullable String buttonNeutral, final DialogInterface.OnClickListener callback) {
        View view = LayoutInflater.from(mRecyclerView.getContext()).inflate(R.layout.bottomsheet_header, mRecyclerView, false);
        ((TextView)view.findViewById(android.R.id.text1)).setText(itemTitle);
        Button button;
        if (buttonPositive != null) {
            button = (Button) view.findViewById(R.id.button_positive);
            button.setVisibility(View.VISIBLE);
            button.setText(buttonPositive);
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    callback.onClick(BottomSheet.this, BUTTON_POSITIVE);
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
                    callback.onClick(BottomSheet.this, BUTTON_NEUTRAL);
                }
            });
        }
        mAdapter.addHeader(view, 0);
        return this;
    }

    public BottomSheet setOnItemCheckListener(OnMultiChoiceClickListener listener) {
        mMultiChoiceClickListener = listener;
        return this;
    }

    public BottomSheet addHeader(String itemTitle, @StringRes int buttonPositive,
                                          @StringRes int buttonNeutral, OnClickListener callback) {
        return addHeader(itemTitle,
                buttonPositive == 0 ? null : mRecyclerView.getContext().getString(buttonPositive),
                buttonNeutral == 0 ? null : mRecyclerView.getContext().getString(buttonNeutral),
                callback);
    }

    public void show() {
        mSheetDialog.show();
    }

    @Override
    public void cancel() {
        mSheetDialog.cancel();
    }

    @Override
    public void dismiss() {
        mSheetDialog.dismiss();
    }

    private class BasicAdapter extends HeaderedAdapter<BasicHolder> {

        private final String[] mDataset;
        @LayoutRes
        private final int mItemLayout;

        BasicAdapter(String[] items, @LayoutRes int layoutId) {
            mDataset = items;
            mItemLayout = layoutId;
        }

        @Override
        public BasicHolder onCreateDataViewHolder(ViewGroup parent, int viewType) {
            return new BasicHolder(parent, mItemLayout);
        }

        @Override
        public void onBindDataViewHolder(BasicHolder holder, int position) {
            holder.primaryTextView.setText(mDataset[position]);
        }

        @Override
        public int getDataItemCount() {
            return mDataset.length;
        }
    }

    class BasicHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        final TextView primaryTextView;
        //final TextView secondaryTextView;

        BasicHolder(ViewGroup parent, @LayoutRes int layout) {
            super(LayoutInflater.from(parent.getContext()).inflate(layout, parent, false));
            primaryTextView = (TextView) itemView.findViewById(android.R.id.text1);
            //secondaryTextView = (TextView) itemView.findViewById(android.R.id.text2);
            itemView.setOnClickListener(this);
            itemView.setBackgroundDrawable(LayoutUtils.getSelectableBackground(itemView.getContext()));
        }

        @Override
        public void onClick(View v) {
            if (mItemClickListener != null) {
                mItemClickListener.onClick(BottomSheet.this, getAdapterPosition() - mAdapter.getHeadersCount());
            }
        }
    }
}
