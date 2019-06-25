package org.nv95.openmanga.feature.search.adapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.nv95.openmanga.R;
import org.nv95.openmanga.feature.main.adapter.MangaListAdapter;
import org.nv95.openmanga.feature.manga.domain.MangaInfo;
import org.nv95.openmanga.items.ThumbSize;
import org.nv95.openmanga.providers.staff.ProviderSummary;
import org.nv95.openmanga.utils.LayoutUtils;
import org.nv95.openmanga.utils.choicecontrol.ModalChoiceController;

import java.util.ArrayList;

/**
 * Created by nv95 on 24.12.16.
 */

public class SearchResultsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements View.OnClickListener {

    private static final int VIEW_HEADER = 0;
    private static final int VIEW_ITEM = 1;
    private static final int VIEW_FOOTER = 2;

    private static final int FOOTER_NONE = 0;
    private static final int FOOTER_BUTTON = 1;
    private static final int FOOTER_PROGRESS = 2;

    private final ArrayList<Object> mDataset;
    private boolean mGrid;
    private ThumbSize mThumbSize;
    private final int mVisibleThreshold = 2;
    private int mLastVisibleItem, mTotalItemCount;
    private boolean mLoading;
    private int mFooter;
    private CharSequence mFooterText;
    private OnMoreEventListener mOnLoadMoreListener;
    private final ModalChoiceController mChoiceController;

    public SearchResultsAdapter(RecyclerView recyclerView) {
        mDataset = new ArrayList<>();
        attach(recyclerView);
        mGrid = false;
        mLoading = false;
        mFooterText = null;
        mFooter = FOOTER_NONE;
        mChoiceController = new ModalChoiceController(this);
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

    public void clearData() {
        mChoiceController.clearSelection();
        mDataset.clear();
        mFooter = FOOTER_NONE;
        notifyDataSetChanged();
    }

    public ModalChoiceController getChoiceController() {
        return mChoiceController;
    }

    public void setThumbnailsSize(@NonNull ThumbSize size) {
        if (!size.equals(mThumbSize)) {
            mThumbSize = size;
            notifyItemRangeChanged(0, getItemCount());
        }
    }

    public void attach(RecyclerView recyclerView) {
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                SearchResultsAdapter.this.onScrolled(recyclerView);
            }
        });
    }

    public void onScrolled(RecyclerView recyclerView) {
        mTotalItemCount = LayoutUtils.getItemCount(recyclerView);
        mLastVisibleItem = LayoutUtils.findLastVisibleItemPosition(recyclerView);
        if (!mLoading && isLoadEnabled() && mTotalItemCount <= (mLastVisibleItem + mVisibleThreshold)) {
            if (mOnLoadMoreListener != null) {
                mLoading = mOnLoadMoreListener.onLoadMore();
            }
        }
    }

    public void loadingComplete() {
        mLoading = false;
    }

    public void append(@Nullable ProviderSummary group, ArrayList<MangaInfo> data) {
        if (data.isEmpty()) {
            return;
        }
        int last = mDataset.size();
        if (group != null) {
            mDataset.add(group);
        }
        mDataset.addAll(data);
        if (last == 0) {
            notifyDataSetChanged();
        } else {
            notifyItemRangeInserted(last, data.size() + (group == null ? 0 : 1));
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (position == mDataset.size()) {
            return VIEW_FOOTER;
        } else if (position > mDataset.size()) {
            throw new RuntimeException("Something wrong");
        } else {
            return mDataset.get(position) instanceof MangaInfo ? VIEW_ITEM : VIEW_HEADER;
        }
    }

    @Nullable
    public MangaInfo getItem(int pos) {
        Object obj = mDataset.get(pos);
        return obj instanceof MangaInfo ? (MangaInfo) obj : null;
    }

    @Override
    public final RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        switch (viewType) {
            case VIEW_HEADER:
                return new GroupViewHolder(inflater.inflate(R.layout.header_group, parent, false));
            case VIEW_FOOTER:
                FooterViewHolder holder = new FooterViewHolder(LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.footer_button, parent, false));
                holder.textView.setOnClickListener(this);
                return holder;
            default:
                MangaListAdapter.MangaViewHolder mangaHolder = new MangaListAdapter.MangaViewHolder(inflater
                        .inflate(mGrid ? R.layout.item_mangagrid : R.layout.item_mangalist, parent, false), null);
                mangaHolder.setListener(mChoiceController);
                return mangaHolder;
        }
    }

    @Override
    public final void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof MangaListAdapter.MangaViewHolder) {
            ((MangaListAdapter.MangaViewHolder) holder).fill((MangaInfo) mDataset.get(position), mThumbSize, mChoiceController.isSelected(position));
        } else if (holder instanceof FooterViewHolder) {
            ((FooterViewHolder) holder).textView.setText(mFooterText);
            ((FooterViewHolder) holder).textView.setVisibility(mFooter == FOOTER_BUTTON ? View.VISIBLE : View.GONE);
            ((FooterViewHolder) holder).progressBar.setVisibility(mFooter == FOOTER_PROGRESS ? View.VISIBLE : View.GONE);
        } else {
            ((GroupViewHolder) holder).fill((ProviderSummary) mDataset.get(position));
        }
    }

    @Override
    public int getItemCount() {
        return mDataset.size() + 1;
    }

    public void setOnLoadMoreListener(OnMoreEventListener onLoadMoreListener) {
        mOnLoadMoreListener = onLoadMoreListener;
    }

    public AutoSpanSizeLookup getSpanSizeLookup(int spans) {
        return new AutoSpanSizeLookup(spans);
    }

    private boolean isLoadEnabled() {
        return mDataset.size() != 0 && mFooter == FOOTER_PROGRESS;
    }

    @Override
    public void onClick(View v) {
        mOnLoadMoreListener.onMoreButtonClick();
    }

    public boolean hasItems() {
        return !mDataset.isEmpty();
    }

    private static class FooterViewHolder extends RecyclerView.ViewHolder {

        public final ProgressBar progressBar;
        public final TextView textView;

        FooterViewHolder(View v) {
            super(v);
            progressBar = v.findViewById(R.id.progressBar);
            textView = v.findViewById(R.id.textView);
            boolean light = !LayoutUtils.isAppThemeDark(v.getContext());
            textView.setBackgroundResource(light ? R.drawable.background_button : R.drawable.background_button_light);
        }
    }

    private static class GroupViewHolder extends RecyclerView.ViewHolder {

        private final TextView mTextView;
        private ProviderSummary mData;

        public GroupViewHolder(View itemView) {
            super(itemView);
            mTextView = itemView.findViewById(R.id.textView);
        }

        public void fill(ProviderSummary data) {
            mData = data;
            mTextView.setText(data.name);
        }
    }

    private class AutoSpanSizeLookup extends GridLayoutManager.SpanSizeLookup {
        final int mCount;

        AutoSpanSizeLookup(int mCount) {
            this.mCount = mCount;
        }

        @Override
        public int getSpanSize(int position) {
            return getItemViewType(position) == VIEW_ITEM ? 1 : mCount;
        }
    }

    public interface OnMoreEventListener {
        void onMoreButtonClick();
        boolean onLoadMore();
    }
}