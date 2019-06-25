package org.nv95.openmanga.feature.main.adapter;

import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.nv95.openmanga.R;
import org.nv95.openmanga.items.HistoryMangaInfo;
import org.nv95.openmanga.feature.manga.domain.MangaInfo;
import org.nv95.openmanga.lists.MangaList;
import org.nv95.openmanga.utils.AppHelper;
import org.nv95.openmanga.utils.ImageUtils;
import org.nv95.openmanga.utils.choicecontrol.OnHolderClickListener;

/**
 * Created by admin on 19.07.17.
 */

public class FastHistoryAdapter extends RecyclerView.Adapter<FastHistoryAdapter.FastHistoryHolder> implements View.OnClickListener {

    private final MangaList mDataset;
    private final OnHolderClickListener mClickListener;

    public FastHistoryAdapter(MangaList dataset, OnHolderClickListener clickListener) {
        mDataset = dataset;
        mClickListener = clickListener;
    }

    @Override
    public FastHistoryHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        FastHistoryHolder holder = new FastHistoryHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_history, parent, false));
        holder.contentLayout.setOnClickListener(this);
        holder.contentLayout.setTag(holder);
        return holder;
    }

    @Override
    public void onBindViewHolder(FastHistoryHolder holder, int position) {
        MangaInfo manga = mDataset.get(position);
        holder.textViewTitle.setText(manga.name);
        if (manga instanceof HistoryMangaInfo) {
            holder.textViewSubtitle.setText(AppHelper.getReadableDateTimeRelative(((HistoryMangaInfo) manga).timestamp));
        } else {
            holder.textViewSubtitle.setText(manga.subtitle);
        }
        ImageUtils.setThumbnail(holder.imageView, manga.preview);

    }

    @Override
    public int getItemCount() {
        return mDataset.size();
    }

    @Override
    public void onClick(View view) {
        FastHistoryHolder holder = (FastHistoryHolder) view.getTag();
        mClickListener.onClick(holder);
    }

    public MangaInfo getItem(int position) {
        return mDataset.get(position);
    }

    static class FastHistoryHolder extends RecyclerView.ViewHolder {

        final RelativeLayout contentLayout;
        final ImageView imageView;
        final TextView textViewTitle;
        final TextView textViewSubtitle;

        FastHistoryHolder(View itemView) {
            super(itemView);
            contentLayout = itemView.findViewById(R.id.content);
            imageView = itemView.findViewById(R.id.imageView);
            textViewTitle = itemView.findViewById(R.id.textView_title);
            textViewSubtitle = itemView.findViewById(R.id.textView_subtitle);
        }
    }
}
