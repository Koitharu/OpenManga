package org.nv95.openmanga.reader.thumbview;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.nv95.openmanga.R;
import org.nv95.openmanga.core.models.MangaPage;
import org.nv95.openmanga.reader.PagesCache;
import org.nv95.openmanga.common.utils.ImageUtils;

import java.util.ArrayList;

/**
 * Created by koitharu on 13.01.18.
 */

final class ThumbViewAdapter extends RecyclerView.Adapter<ThumbViewAdapter.ThumbHolder> {

	private final ArrayList<MangaPage> mDataset;
	private final OnThumbnailClickListener mClickListener;
	private final PagesCache mCache;

	ThumbViewAdapter(Context context, ArrayList<MangaPage> pages, OnThumbnailClickListener onClickListener) {
		mDataset = pages;
		mClickListener = onClickListener;
		mCache = PagesCache.getInstance(context);
		setHasStableIds(true);
	}

	@Override
	public ThumbHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		return new ThumbHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_thumb, parent, false));
	}

	@Override
	public void onBindViewHolder(ThumbHolder holder, int position) {
		MangaPage item = mDataset.get(position);
		holder.textView.setText(String.valueOf(position + 1));
		ImageUtils.setThumbnail(holder.imageView, mCache.getFileForUrl(item.url));
	}

	@Override
	public int getItemCount() {
		return mDataset.size();
	}

	@Override
	public long getItemId(int position) {
		return mDataset.get(position).id;
	}

	final class ThumbHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

		final ImageView imageView;
		final TextView textView;
		final View selector;

		ThumbHolder(View itemView) {
			super(itemView);
			imageView = itemView.findViewById(R.id.imageView);
			textView = itemView.findViewById(R.id.textView);
			selector = itemView.findViewById(R.id.selector);
			selector.setOnClickListener(this);
		}

		@Override
		public void onClick(View v) {
			mClickListener.onThumbnailClick(getAdapterPosition());
		}
	}
}
