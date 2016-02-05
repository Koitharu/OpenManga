package org.nv95.openmanga.adapters;

import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.nv95.openmanga.R;
import org.nv95.openmanga.items.MangaInfo;

import java.util.ArrayList;

/**
 * Created by nv95 on 31.01.16.
 */
public class GroupedAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    protected static final int VIEW_ITEM = 1;
    protected static final int VIEW_HEADER = 0;

    private ArrayList<Object> mDataset;
    private boolean mGrid;

    public GroupedAdapter() {
        mDataset = new ArrayList<>();
        mGrid = false;
    }

    public void append(String group, ArrayList<MangaInfo> data) {
        int last = mDataset.size();
        mDataset.add(group);
        mDataset.addAll(data);
        notifyItemRangeInserted(last, data.size() + 1);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        return viewType == VIEW_HEADER ?
                new GroupViewHolder(inflater.inflate(R.layout.header_group, parent, false)) :
                new MangaListAdapter.MangaViewHolder(inflater
                        .inflate(mGrid ? R.layout.item_mangagrid : R.layout.item_mangalist, parent, false), null);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof MangaListAdapter.MangaViewHolder) {
            ((MangaListAdapter.MangaViewHolder) holder).fill(getItem(position));
        } else {
            ((GroupViewHolder) holder).fill((String) mDataset.get(position));
        }
    }

    @Override
    public int getItemCount() {
        return mDataset.size();
    }

    @Nullable
    public String getGroup(int position) {
        for (int i = position; i > 0; i--) {
            if (mDataset.get(i) instanceof String) {
                return (String) mDataset.get(i);
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
        return mDataset.get(position) instanceof MangaInfo ? VIEW_ITEM : VIEW_HEADER;
    }

    public void onLayoutManagerChanged(LinearLayoutManager layoutManager) {
        boolean grid;
        if (grid = layoutManager instanceof GridLayoutManager) {
            ((GridLayoutManager) layoutManager).setSpanSizeLookup(new AutoSpanSizeLookup(
                    ((GridLayoutManager) layoutManager).getSpanCount()
            ));
        }
        if (grid != mGrid) {
            mGrid = grid;
            notifyDataSetChanged();
        }
    }

    public interface OnMoreClickListener {
        void onMoreClick(String group, int groupPosition);
    }

    protected static class GroupViewHolder extends RecyclerView.ViewHolder {
        private final TextView mTextView;

        public GroupViewHolder(View itemView) {
            super(itemView);
            mTextView = (TextView) itemView.findViewById(R.id.textView);
        }

        public void fill(String data) {
            mTextView.setText(data);
        }
    }

    private class AutoSpanSizeLookup extends GridLayoutManager.SpanSizeLookup {
        final int mCount;

        public AutoSpanSizeLookup(int mCount) {
            this.mCount = mCount;
        }

        @Override
        public int getSpanSize(int position) {
            return getItemViewType(position) == VIEW_HEADER ? mCount : 1;
        }
    }
}
