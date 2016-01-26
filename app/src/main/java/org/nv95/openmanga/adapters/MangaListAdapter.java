package org.nv95.openmanga.adapters;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.nv95.openmanga.MangaPreviewActivity;
import org.nv95.openmanga.R;
import org.nv95.openmanga.components.AsyncImageView;
import org.nv95.openmanga.items.MangaInfo;
import org.nv95.openmanga.lists.PagedList;

/**
 * Created by nv95 on 30.09.15.
 */
public class MangaListAdapter extends EndlessAdapter<MangaInfo, MangaListAdapter.MangaViewHolder> {
    private boolean mGrid;
    public MangaListAdapter(PagedList<MangaInfo> dataset, RecyclerView recyclerView) {
        super(dataset, recyclerView);
    }

    @Override
    public void onLayoutManagerChanged(LinearLayoutManager layoutManager) {
        super.onLayoutManagerChanged(layoutManager);
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

    @Override
    public MangaViewHolder onCreateHolder(ViewGroup parent) {
        return new MangaViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(mGrid ? R.layout.item_mangagrid : R.layout.item_mangalist, parent, false));
    }

    @Override
    public long getItemId(MangaInfo data) {
        return data.hashCode();
    }

    @Override
    public void onBindHolder(MangaViewHolder viewHolder, MangaInfo data) {
        viewHolder.fill(data);
    }

    protected static class MangaViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private TextView textViewTitle;
        private TextView textViewSubtitle;
        private TextView textViewSummary;
        private AsyncImageView asyncImageView;
        private MangaInfo mData;

        public MangaViewHolder(View itemView) {
            super(itemView);
            textViewTitle = (TextView) itemView.findViewById(R.id.textView_title);
            textViewSubtitle = (TextView) itemView.findViewById(R.id.textView_subtitle);
            textViewSummary = (TextView) itemView.findViewById(R.id.textView_summary);
            asyncImageView = (AsyncImageView) itemView.findViewById(R.id.imageView);
            itemView.setOnClickListener(this);
        }

        public void fill(MangaInfo data) {
            mData = data;
            textViewTitle.setText(mData.name);
            if (mData.subtitle == null) {
                textViewSubtitle.setVisibility(View.GONE);
            } else {
                textViewSubtitle.setText(mData.subtitle);
                textViewSubtitle.setVisibility(View.VISIBLE);
            }
            textViewSummary.setText(mData.summary);
            asyncImageView.setImageAsync(mData.preview, true);
        }

        @Override
        public void onClick(View v) {
            Context context = v.getContext();
            Intent intent = new Intent(context, MangaPreviewActivity.class);
            intent.putExtras(mData.toBundle());
            context.startActivity(intent);
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
