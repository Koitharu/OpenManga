package org.nv95.openmanga.adapters;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.nv95.openmanga.MangaPreviewActivity;
import org.nv95.openmanga.R;
import org.nv95.openmanga.components.AsyncImageView;
import org.nv95.openmanga.items.MangaInfo;
import org.nv95.openmanga.items.ThumbSize;
import org.nv95.openmanga.lists.PagedList;

/**
 * Created by nv95 on 30.09.15.
 */
public class MangaListAdapter extends EndlessAdapter<MangaInfo, MangaListAdapter.MangaViewHolder> {
    private boolean mGrid;
    private ThumbSize mThumbSize;
    @Nullable
    private OnItemLongClickListener<MangaViewHolder> mOnItemLongClickListener;

    public MangaListAdapter(PagedList<MangaInfo> dataset, RecyclerView recyclerView) {
        super(dataset, recyclerView);
    }

    public void setOnItemLongClickListener(OnItemLongClickListener<MangaViewHolder> itemLongClickListener) {
        mOnItemLongClickListener = itemLongClickListener;
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

    @Override
    public MangaViewHolder onCreateHolder(ViewGroup parent) {
        return new MangaViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(mGrid ? R.layout.item_mangagrid : R.layout.item_mangalist, parent, false), mOnItemLongClickListener);
    }

    @Override
    public void onViewRecycled(RecyclerView.ViewHolder holder) {
        if (holder instanceof MangaViewHolder) {
            ((MangaViewHolder) holder).asyncImageView.setImageAsync(null);
        }
        super.onViewRecycled(holder);
    }

    @Override
    public long getItemId(MangaInfo data) {
        return data.hashCode();
    }

    @Override
    public void onBindHolder(MangaViewHolder viewHolder, MangaInfo data) {
        viewHolder.fill(data, mThumbSize);
    }

    public static class MangaViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        @Nullable
        private final OnItemLongClickListener<MangaViewHolder> mLongClickListener;
        private TextView textViewTitle;
        private TextView textViewSubtitle;
        private TextView textViewSummary;
        private TextView textViewBadge;
        private AsyncImageView asyncImageView;
        private MangaInfo mData;

        public MangaViewHolder(View itemView, @Nullable OnItemLongClickListener<MangaViewHolder> longClickListener) {
            super(itemView);
            textViewTitle = (TextView) itemView.findViewById(R.id.textView_title);
            textViewSubtitle = (TextView) itemView.findViewById(R.id.textView_subtitle);
            textViewSummary = (TextView) itemView.findViewById(R.id.textView_summary);
            textViewBadge = (TextView) itemView.findViewById(R.id.textView_badge);
            asyncImageView = (AsyncImageView) itemView.findViewById(R.id.imageView);
            itemView.setOnClickListener(this);
            mLongClickListener = longClickListener;
            itemView.setOnLongClickListener(this);
        }


        public MangaInfo getData() {
            return mData;
        }

        public void fill(MangaInfo data, ThumbSize thumbSize) {
            mData = data;
            textViewTitle.setText(mData.name);
            if (mData.subtitle == null) {
                textViewSubtitle.setVisibility(View.GONE);
            } else {
                textViewSubtitle.setText(mData.subtitle);
                textViewSubtitle.setVisibility(View.VISIBLE);
            }
            textViewSummary.setText(mData.summary);
            asyncImageView.setImageThumbAsync(mData.preview, thumbSize);
            // TODO: 17.02.16
            //textViewTitle.setTypeface(mData.isCompleted() ? Typeface.DEFAULT_BOLD : Typeface.DEFAULT);
            if (mData.extra == null) {
                textViewBadge.setVisibility(View.GONE);
            } else {
                textViewBadge.setText(mData.extra);
                textViewBadge.setVisibility(View.VISIBLE);
            }
        }

        @Override
        public void onClick(View v) {
            Context context = v.getContext();
            Intent intent = new Intent(context, MangaPreviewActivity.class);
            intent.putExtras(mData.toBundle());
            context.startActivity(intent);
        }

        @Override
        public boolean onLongClick(View v) {
            return mLongClickListener != null && mLongClickListener.onItemLongClick(this);
        }
    }

    private class AutoSpanSizeLookup extends GridLayoutManager.SpanSizeLookup {
        final int mCount;

        public AutoSpanSizeLookup(int mCount) {
            this.mCount = mCount;
        }

        @Override
        public int getSpanSize(int position) {
            return getItemViewType(position) == VIEW_PROGRESS ? mCount : 1;
        }
    }

}
