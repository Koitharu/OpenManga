package org.nv95.openmanga.mangalist;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.IntDef;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.nv95.openmanga.R;
import org.nv95.openmanga.common.DataViewHolder;
import org.nv95.openmanga.core.models.MangaHeader;
import org.nv95.openmanga.core.providers.MangaProvider;
import org.nv95.openmanga.preview.PreviewActivity;
import org.nv95.openmanga.common.views.EndlessRecyclerView;
import org.nv95.openmanga.common.utils.ImageUtils;
import org.nv95.openmanga.common.utils.LayoutUtils;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.List;

/**
 * Created by koitharu on 28.12.17.
 */

public final class MangaListAdapter extends RecyclerView.Adapter<DataViewHolder<?>> implements
		EndlessRecyclerView.EndlessAdapter {

	private final List<MangaHeader> mDataset;
	private boolean mInProgress;

	MangaListAdapter(List<MangaHeader> dataset) {
		setHasStableIds(true);
		mDataset = dataset;
		mInProgress = true;
	}

	@Override
	public DataViewHolder<?> onCreateViewHolder(ViewGroup parent, @ItemViewType int viewType) {
		final LayoutInflater inflater = LayoutInflater.from(parent.getContext());
		DataViewHolder<?> holder;
		switch (viewType) {
			case ItemViewType.TYPE_ITEM_MANGA:
				holder = new MangaHeaderHolder(inflater.inflate(R.layout.item_manga_list, parent, false));
				break;
			case ItemViewType.TYPE_ITEM_PROGRESS:
				return new ProgressHolder(inflater.inflate(R.layout.item_progress, parent, false));
			default:
				throw new AssertionError("Unknown viewType");
		}
		return holder;
	}

	@Override
	public void onBindViewHolder(DataViewHolder holder, int position) {
		if (holder instanceof MangaHeaderHolder) {
			((MangaHeaderHolder) holder).bind(mDataset.get(position));
		} else if (holder instanceof ProgressHolder) {
			((ProgressHolder) holder).bind(mInProgress);
		}
 	}

	@Override
	public int getItemCount() {
		final int size = mDataset.size();
		return size == 0 ? 0 : size + 1;
	}

	@ItemViewType
	@Override
	public int getItemViewType(int position) {
		return position == mDataset.size() ? ItemViewType.TYPE_ITEM_PROGRESS : ItemViewType.TYPE_ITEM_MANGA;
	}

	@Override
	public long getItemId(int position) {
		return position >= mDataset.size() ? 0 : mDataset.get(position).id;
	}

	@Override
	public void onViewRecycled(DataViewHolder holder) {
		holder.recycle();
		super.onViewRecycled(holder);
	}

	@Override
	public void setHasNext(boolean hasNext) {
		mInProgress = hasNext;
	}

	static class MangaHeaderHolder extends DataViewHolder<MangaHeader> implements View.OnClickListener {

		private final TextView mText1;
		private final TextView mText2;
		private final TextView mTextViewSummary;
		private final ImageView mImageView;

		MangaHeaderHolder(View itemView) {
			super(itemView);
			mText1 = itemView.findViewById(android.R.id.text1);
			mText2 = itemView.findViewById(android.R.id.text2);
			mTextViewSummary = itemView.findViewById(android.R.id.summary);
			mImageView = itemView.findViewById(R.id.imageView);
			itemView.setOnClickListener(this);
		}

		public void bind(MangaHeader item) {
			super.bind(item);
			mText1.setText(item.name);
			mTextViewSummary.setText(item.genres);
			LayoutUtils.setTextOrHide(mText2, item.summary);
			ImageUtils.setThumbnail(mImageView, item.thumbnail, MangaProvider.getDomain(item.provider));
		}

		@Override
		public void recycle() {
			super.recycle();
			ImageUtils.recycle(mImageView);
		}

		@Override
		public void onClick(View v) {
			MangaHeader mangaHeader = getData();
			Context context = v.getContext();
			context.startActivity(new Intent(context.getApplicationContext(), PreviewActivity.class)
					.putExtra("manga", mangaHeader));
		}
	}

	static class ProgressHolder extends DataViewHolder<Boolean> {

		private final ProgressBar mProgressBar;

		ProgressHolder(View itemView) {
			super(itemView);
			mProgressBar = itemView.findViewById(android.R.id.progress);
		}

		@Override
		public void bind(Boolean aBoolean) {
			super.bind(aBoolean);
			mProgressBar.setVisibility(aBoolean ? View.VISIBLE : View.INVISIBLE);
		}
	}

	@Retention(RetentionPolicy.SOURCE)
	@IntDef({ItemViewType.TYPE_ITEM_MANGA, ItemViewType.TYPE_ITEM_PROGRESS})
	public @interface ItemViewType {
		int TYPE_ITEM_MANGA = 0;
		int TYPE_ITEM_PROGRESS = 1;
	}
}
