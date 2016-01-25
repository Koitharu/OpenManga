package org.nv95.openmanga.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.nv95.openmanga.R;
import org.nv95.openmanga.components.AsyncImageView;
import org.nv95.openmanga.items.MangaInfo;
import org.nv95.openmanga.lists.PagedList;

/**
 * Created by nv95 on 30.09.15.
 */
public class MangaListAdapter extends EndlessAdapter<MangaInfo, MangaListAdapter.MangaViewHolder> {

    public MangaListAdapter(PagedList<MangaInfo> dataset, RecyclerView recyclerView) {
        super(dataset, recyclerView);
    }

    public boolean isGrid() {
        return false;
    }

    public void setGrid(boolean grid) {
    /*if (this.grid != grid) {
      this.grid = grid;
      notifyDataSetChanged();
    }*/
    }

    @Override
    public MangaViewHolder onCreateHolder(ViewGroup parent) {
        return new MangaViewHolder(LayoutInflater.from(parent.getContext())
        .inflate(R.layout.item_mangalist, parent, false));
    }

    @Override
    public long getItemId(MangaInfo data) {
        return data.hashCode();
    }

    @Override
    public void onBindHolder(MangaViewHolder viewHolder, MangaInfo data) {
        viewHolder.fill(data);
    }

    protected static class MangaViewHolder extends RecyclerView.ViewHolder {
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
        }

        public void fill(MangaInfo data) {
            mData = data;
            textViewTitle.setText(mData.name);
            textViewSubtitle.setText(mData.subtitle);
            textViewSummary.setText(mData.summary);
            asyncImageView.setImageAsync(mData.preview, true);
        }
    }
}
