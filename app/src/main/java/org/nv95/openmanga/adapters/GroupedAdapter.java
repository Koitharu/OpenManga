package org.nv95.openmanga.adapters;

import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.nv95.openmanga.R;
import org.nv95.openmanga.items.MangaInfo;
import org.nv95.openmanga.items.ThumbSize;
import org.nv95.openmanga.providers.staff.ProviderSummary;

import java.util.ArrayList;

/**
 * Created by nv95 on 31.01.16.
 */
public class GroupedAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements View.OnClickListener {

    private static final int VIEW_ITEM = 1;
    private static final int VIEW_HEADER = 0;
    private static final int VIEW_FOOTER = 2;

    private static final int FOOTER_NONE = 0;
    private static final int FOOTER_BUTTON = 1;
    private static final int FOOTER_PROGRESS = 2;

    private final ArrayList<Object> mDataset;
    private boolean mGrid;
    private ThumbSize mThumbSize;
    private final OnMoreClickListener mOnMoreClickListener;
    private int mFooter;
    private CharSequence mFooterText;

    public GroupedAdapter(OnMoreClickListener moreClickListener) {
        mDataset = new ArrayList<>();
        mOnMoreClickListener = moreClickListener;
        mGrid = false;
        mFooter = FOOTER_NONE;
        mFooterText = null;
    }

    public void hideFooter() {
        mFooter = FOOTER_NONE;
        notifyItemChanged(mDataset.size());
    }

    public void setFooterProgress() {
        mFooter = FOOTER_PROGRESS;
        notifyItemChanged(mDataset.size());
    }

    public void setFooterButton(CharSequence title) {
        mFooter = FOOTER_BUTTON;
        mFooterText = title;
        notifyItemChanged(mDataset.size());
    }

    public boolean setGrid(boolean grid) {
        if (mGrid != grid) {
            mGrid = grid;
            notifyDataSetChanged();
            return true;
        } else {
            return false;
        }
    }

    public void setThumbnailsSize(@NonNull ThumbSize size) {
        if (!size.equals(mThumbSize)) {
            mThumbSize = size;
            notifyItemRangeChanged(0, getItemCount());
        }
    }

    public void append(ProviderSummary group, ArrayList<MangaInfo> data) {
        int last = mDataset.size();
        mDataset.add(group);
        mDataset.addAll(data);
        if (last == 0) {
            notifyDataSetChanged();
        } else {
            notifyItemRangeInserted(last, data.size() + 1);
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        switch (viewType) {
            case VIEW_HEADER:
                return new GroupViewHolder(inflater.inflate(R.layout.header_group, parent, false), mOnMoreClickListener);
            case VIEW_FOOTER:
                ButtonViewHolder holder = new ButtonViewHolder(LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.footer_button, parent, false));
                holder.textView.setOnClickListener(this);
                return holder;
            default:
                return new MangaListAdapter.MangaViewHolder(inflater
                        .inflate(mGrid ? R.layout.item_mangagrid : R.layout.item_mangalist, parent, false), null);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof MangaListAdapter.MangaViewHolder) {
            ((MangaListAdapter.MangaViewHolder) holder).fill(getItem(position), mThumbSize, false);
        } else if (holder instanceof ButtonViewHolder) {
            ((ButtonViewHolder) holder).textView.setText(mFooterText);
            ((ButtonViewHolder) holder).textView.setVisibility(mFooter == FOOTER_BUTTON ? View.VISIBLE : View.GONE);
            ((ButtonViewHolder) holder).progressBar.setVisibility(mFooter == FOOTER_PROGRESS ? View.VISIBLE : View.GONE);
        } else {
            ((GroupViewHolder) holder).fill((ProviderSummary) mDataset.get(position));
        }
    }

    @Override
    public int getItemCount() {
        return mDataset.size() + 1;
    }

    @Nullable
    public ProviderSummary getGroup(int position) {
        for (int i = position; i > 0; i--) {
            if (mDataset.get(i) instanceof ProviderSummary) {
                return (ProviderSummary) mDataset.get(i);
            }
        }
        return null;
    }

    @Nullable
    public MangaInfo getItem(int position) {
        Object object = mDataset.get(position);
        return object instanceof MangaInfo ? (MangaInfo) object : null;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == mDataset.size()) {
            return VIEW_FOOTER;
        } else {
            return mDataset.get(position) instanceof MangaInfo ? VIEW_ITEM : VIEW_HEADER;
        }
    }

    public void onLayoutManagerChanged(boolean grid) {
        if (grid != mGrid) {
            mGrid = grid;
        }
        notifyDataSetChanged();
    }

    public AutoSpanSizeLookup getSpanSizeLookup(int spans) {
        return new AutoSpanSizeLookup(spans);
    }

    @Override
    public void onClick(View view) {
        mOnMoreClickListener.onMoreButtonClick();
    }

    public boolean hasItems() {
        return !mDataset.isEmpty();
    }

    public void clearItems() {
        mDataset.clear();
        notifyDataSetChanged();
    }

    public interface OnMoreClickListener {
        void onMoreClick(String title, ProviderSummary provider);
        void onMoreButtonClick();
    }

    private static class GroupViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private final TextView mTextView;
        private final OnMoreClickListener mMoreClickListener;
        private ProviderSummary mData;

        public GroupViewHolder(View itemView, OnMoreClickListener moreClickListener) {
            super(itemView);
            mTextView = (TextView) itemView.findViewById(R.id.textView);
            itemView.setOnClickListener(this);
            mMoreClickListener = moreClickListener;
        }

        public void fill(ProviderSummary data) {
            mData = data;
            mTextView.setText(data.name);
        }

        @Override
        public void onClick(View v) {
            mMoreClickListener.onMoreClick(mData.name, mData);
        }
    }

    private static class ButtonViewHolder extends RecyclerView.ViewHolder {

        public final ProgressBar progressBar;
        public final TextView textView;

        ButtonViewHolder(View v) {
            super(v);
            progressBar = (ProgressBar) v.findViewById(R.id.progressBar);
            textView = (TextView) v.findViewById(R.id.textView);
            boolean light = PreferenceManager.getDefaultSharedPreferences(v.getContext())
                    .getString("theme", "0").equals("0");
            textView.setBackgroundResource(light ? R.drawable.background_button : R.drawable.background_button_light);
        }
    }

    public class AutoSpanSizeLookup extends GridLayoutManager.SpanSizeLookup {
        final int mCount;

        public AutoSpanSizeLookup(int mCount) {
            this.mCount = mCount;
        }

        @Override
        public int getSpanSize(int position) {
            return getItemViewType(position) == VIEW_ITEM ? 1 : mCount;
        }
    }
}
