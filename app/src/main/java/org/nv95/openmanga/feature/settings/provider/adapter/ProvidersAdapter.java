package org.nv95.openmanga.feature.settings.provider.adapter;

import android.app.Activity;
import android.content.Intent;
import androidx.core.view.MotionEventCompat;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.nv95.openmanga.R;
import org.nv95.openmanga.feature.settings.provider.activity.ProviderPreferencesActivity;
import org.nv95.openmanga.providers.staff.ProviderSummary;
import org.nv95.openmanga.utils.LayoutUtils;

import java.util.List;

/**
 * Created by nv95 on 12.07.16.
 */

public class ProvidersAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements View.OnClickListener {

    private static final int ITEM_VIEW = 0;
    private static final int ITEM_FOOTER = 1;
    private static final int ITEM_DIVIDER = 2;

    private final List<ProviderSummary> mDataset;
    private final String[] languages;
    private final OnStartDragListener mDragListener;
    private final Activity mActivity;
    private int mActiveCount = 4;

    public ProvidersAdapter(Activity context, List<ProviderSummary> providers, OnStartDragListener dragListener) {
        mActivity = context;
        mDataset = providers;
        mDragListener = dragListener;
        languages = context.getResources().getStringArray(R.array.languages);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case ITEM_VIEW:
                ProviderHolder pholder = new ProviderHolder(
                        LayoutInflater.from(parent.getContext())
                                .inflate(R.layout.item_provider, parent, false),
                        mDragListener
                );
                pholder.imageButton.setOnClickListener(this);
                return pholder;
            case ITEM_DIVIDER:
                return new DividerHolder(
                        LayoutInflater.from(parent.getContext())
                                .inflate(R.layout.item_provider_divider, parent, false),
                        mDragListener
                );
            default:
                return new FooterHolder(
                        LayoutInflater.from(parent.getContext())
                                .inflate(R.layout.footer_disclaimer, parent, false)
                );
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof ProviderHolder) {
            ProviderSummary item = getItem(position);
            ((ProviderHolder)holder).text1.setText(item.name);
            ((ProviderHolder)holder).text2.setText(languages[item.lang]);
            ((ProviderHolder)holder).imageButton.setVisibility(item.preferences == 0 ? View.GONE : View.VISIBLE);
            ((ProviderHolder)holder).imageButton.setTag(item);
        }
    }

    private ProviderSummary getItem(int position) {
        return mDataset.get(position < mActiveCount ? position : position - 1);
    }


    public void setActiveCount(int count) {
        mActiveCount = count;
    }

    @Override
    public int getItemCount() {
        return mDataset.size() + 2;
    }

    @Override
    public int getItemViewType(int position) {
        if (position <= mDataset.size()) {
            if (position == mActiveCount) {
                return ITEM_DIVIDER;
            } else {
                return ITEM_VIEW;
            }
        } else {
            return ITEM_FOOTER;
        }
    }

    public int getActiveCount() {
        return mActiveCount;
    }

    @Override
    public void onClick(View view) {
        Object tag = view.getTag();
        if (tag != null && tag instanceof ProviderSummary) {
            mActivity.startActivity(new Intent(mActivity, ProviderPreferencesActivity.class).putExtra("provider", ((ProviderSummary) tag).id));
        }
    }

    private static class ProviderHolder extends RecyclerView.ViewHolder implements View.OnTouchListener {

        final TextView text1;
        final TextView text2;
        final ImageView imageButton;
        private final OnStartDragListener mStartDragListener;

        ProviderHolder(View itemView, OnStartDragListener sdl) {
            super(itemView);
            text1 = itemView.findViewById(android.R.id.text1);
            text2 = itemView.findViewById(android.R.id.text2);
            imageButton = itemView.findViewById(R.id.imageButton);
            mStartDragListener = sdl;
            itemView.findViewById(R.id.imageView_draghandle).setOnTouchListener(this);
            if (LayoutUtils.isAppThemeDark(itemView.getContext())) {
                LayoutUtils.setAllImagesColor((ViewGroup) itemView, R.color.white_overlay_85);
            }
        }

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (MotionEventCompat.getActionMasked(event) ==
                    MotionEvent.ACTION_DOWN) {
                mStartDragListener.onStartDrag(this);
            }
            return false;
        }
    }

    private static class FooterHolder extends RecyclerView.ViewHolder {

        FooterHolder(View itemView) {
            super(itemView);
        }
    }

    public static class DividerHolder extends RecyclerView.ViewHolder implements View.OnTouchListener {

        private final OnStartDragListener mStartDragListener;

        DividerHolder(View itemView, OnStartDragListener startDragListener) {
            super(itemView);
            mStartDragListener = startDragListener;
            if (LayoutUtils.isAppThemeDark(itemView.getContext())) {
                LayoutUtils.setAllImagesColor((ViewGroup) itemView, R.color.white_overlay_85);
            }
            itemView.findViewById(R.id.imageView_draghandle).setOnTouchListener(this);
        }

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (MotionEventCompat.getActionMasked(event) ==
                    MotionEvent.ACTION_DOWN) {
                mStartDragListener.onStartDrag(this);
            }
            return false;
        }
    }

    public interface OnStartDragListener {
        void onStartDrag(RecyclerView.ViewHolder viewHolder);
    }
}
