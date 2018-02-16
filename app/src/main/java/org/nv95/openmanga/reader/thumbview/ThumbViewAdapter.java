package org.nv95.openmanga.reader.thumbview;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.nv95.openmanga.R;
import org.nv95.openmanga.common.DataViewHolder;
import org.nv95.openmanga.common.utils.ImageUtils;
import org.nv95.openmanga.common.utils.MetricsUtils;
import org.nv95.openmanga.common.utils.network.NetworkUtils;
import org.nv95.openmanga.core.models.MangaPage;
import org.nv95.openmanga.core.providers.MangaProvider;
import org.nv95.openmanga.reader.loader.PagesCache;

import java.io.File;
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
		holder.bind(mDataset.get(position));
	}

	@Override
	public int getItemCount() {
		return mDataset.size();
	}

	@Override
	public long getItemId(int position) {
		return mDataset.get(position).id;
	}

	final class ThumbHolder extends DataViewHolder<MangaPage> implements View.OnClickListener {

		private final ImageView mImageView;
		private final TextView mTextView;
		private final View mSelector;

		ThumbHolder(View itemView) {
			super(itemView);
			mImageView = itemView.findViewById(R.id.imageView);
			mTextView = itemView.findViewById(R.id.textView);
			mSelector = itemView.findViewById(R.id.selector);
			mSelector.setOnClickListener(this);
		}

		@Override
		public void bind(MangaPage mangaPage) {
			super.bind(mangaPage);
			final MetricsUtils.Size size = MetricsUtils.getPreferredCellSizeMedium(getContext().getResources());
			mTextView.setText(String.valueOf(getAdapterPosition() + 1));
			final File file = mCache.getFileForUrl(mangaPage.url);
			if (file.exists()) {
				ImageUtils.setThumbnailCropped(mImageView, mCache.getFileForUrl(mangaPage.url), size);
			} else if (NetworkUtils.isNetworkAvailable(getContext(), false)) {
				ImageUtils.setThumbnailCropped(mImageView, mangaPage.url, size, MangaProvider.getDomain(mangaPage.provider));
			} else {
				ImageUtils.setEmptyThumbnail(mImageView);
			}
		}

		@Override
		public void onClick(View v) {
			mClickListener.onThumbnailClick(getAdapterPosition());
		}
	}
}
