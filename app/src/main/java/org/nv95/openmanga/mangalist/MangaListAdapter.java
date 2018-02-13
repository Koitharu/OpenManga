package org.nv95.openmanga.mangalist;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.ColorInt;
import android.support.annotation.IntDef;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;

import org.nv95.openmanga.R;
import org.nv95.openmanga.common.DataViewHolder;
import org.nv95.openmanga.common.utils.ImageUtils;
import org.nv95.openmanga.common.utils.LayoutUtils;
import org.nv95.openmanga.common.utils.ThemeUtils;
import org.nv95.openmanga.common.views.EndlessRecyclerView;
import org.nv95.openmanga.core.MangaStatus;
import org.nv95.openmanga.core.models.MangaHeader;
import org.nv95.openmanga.core.providers.MangaProvider;
import org.nv95.openmanga.preview.PreviewActivity;

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
	private boolean mDetailed;

	MangaListAdapter(List<MangaHeader> dataset, boolean detailed) {
		setHasStableIds(true);
		mDataset = dataset;
		mInProgress = true;
		mDetailed = detailed;
	}

	@Override
	public DataViewHolder<?> onCreateViewHolder(ViewGroup parent, @ItemViewType int viewType) {
		final LayoutInflater inflater = LayoutInflater.from(parent.getContext());
		switch (viewType) {
			case ItemViewType.TYPE_ITEM_MANGA:
				return mDetailed ?
						new DetailedMangaHolder(inflater.inflate(R.layout.item_manga_list_detailed, parent, false))
						: new MangaHolder(inflater.inflate(R.layout.item_manga_list, parent, false));
			case ItemViewType.TYPE_ITEM_PROGRESS:
				return new ProgressHolder(inflater.inflate(R.layout.item_progress, parent, false));
			default:
				throw new AssertionError("Unknown viewType");
		}
	}

	@Override
	public void onBindViewHolder(DataViewHolder holder, int position) {
		if (holder instanceof MangaHolder) {
			((MangaHolder) holder).bind(mDataset.get(position));
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

	public void setIsDetailed(boolean detailed) {
		mDetailed = detailed;
	}

	public boolean isDetailed() {
		return mDetailed;
	}

	static class MangaHolder extends DataViewHolder<MangaHeader> implements View.OnClickListener {

		private final TextView mText1;
		private final TextView mText2;
		private final TextView mTextViewSummary;
		private final ImageView mImageView;

		MangaHolder(View itemView) {
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

	static class DetailedMangaHolder extends MangaHolder {

		private final RatingBar mRatingBar;
		private final TextView mTextViewStatus;
		@ColorInt
		private final int mPrimaryColor;
		@ColorInt
		private final int mAccentColor;

		DetailedMangaHolder(View itemView) {
			super(itemView);
			mRatingBar = itemView.findViewById(R.id.ratingBar);
			mTextViewStatus = itemView.findViewById(R.id.textView_status);
			mPrimaryColor = ThemeUtils.getThemeAttrColor(itemView.getContext(), R.attr.colorPrimary);
			mAccentColor = ThemeUtils.getThemeAttrColor(itemView.getContext(), R.attr.colorAccent);
		}

		@Override
		public void bind(MangaHeader item) {
			super.bind(item);
			if (item.rating == 0) {
				mRatingBar.setVisibility(View.GONE);
			} else {
				mRatingBar.setVisibility(View.VISIBLE);
				mRatingBar.setRating(item.rating / 20);
			}
			switch (item.status) {
				case MangaStatus.STATUS_ONGOING:
					mTextViewStatus.setVisibility(View.VISIBLE);
					mTextViewStatus.setText(R.string.status_ongoing);
					mTextViewStatus.setTextColor(mAccentColor);
					break;
				case MangaStatus.STATUS_COMPLETED:
					mTextViewStatus.setVisibility(View.VISIBLE);
					mTextViewStatus.setText(R.string.status_completed);
					mTextViewStatus.setTextColor(mPrimaryColor);
					break;
				default:
					mTextViewStatus.setVisibility(View.GONE);

			}
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
