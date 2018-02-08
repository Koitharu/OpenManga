package org.nv95.openmanga.mangalist.history;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.ColorInt;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import org.nv95.openmanga.R;
import org.nv95.openmanga.common.DataViewHolder;
import org.nv95.openmanga.common.utils.ImageUtils;
import org.nv95.openmanga.common.utils.ResourceUtils;
import org.nv95.openmanga.common.utils.ThemeUtils;
import org.nv95.openmanga.core.MangaStatus;
import org.nv95.openmanga.core.models.MangaHistory;
import org.nv95.openmanga.core.providers.MangaProvider;
import org.nv95.openmanga.preview.PreviewActivity;

import java.util.ArrayList;

/**
 * Created by koitharu on 18.01.18.
 */

final class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.HistoryHolder> {

	private final ArrayList<MangaHistory> mDataset;
	private boolean mDetailed;

	HistoryAdapter(ArrayList<MangaHistory> dataset, boolean detailed) {
		setHasStableIds(true);
		mDataset = dataset;
		mDetailed = detailed;
	}

	@Override
	public HistoryHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		final LayoutInflater inflater = LayoutInflater.from(parent.getContext());
		return mDetailed ?
				new DetailedHistoryHolder(inflater.inflate(R.layout.item_manga_list_detailed, parent, false)) :
				new HistoryHolder(inflater.inflate(R.layout.item_manga_list, parent, false));
	}

	@Override
	public void onBindViewHolder(HistoryHolder holder, int position) {
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

	@Override
	public void onViewRecycled(HistoryHolder holder) {
		holder.recycle();
	}

	public void setIsDetailed(boolean detailed) {
		mDetailed = detailed;
	}

	public boolean isDetailed() {
		return mDetailed;
	}

	static class HistoryHolder extends DataViewHolder<MangaHistory> implements View.OnClickListener {

		private final TextView mText1;
		private final TextView mText2;
		private final TextView mSummary;
		private final ImageView mImageView;

		private HistoryHolder(View itemView) {
			super(itemView);
			mText1 = itemView.findViewById(android.R.id.text1);
			mText2 = itemView.findViewById(android.R.id.text2);
			mSummary = itemView.findViewById(android.R.id.summary);
			mImageView = itemView.findViewById(R.id.imageView);
			itemView.setOnClickListener(this);
		}

		@Override
		public void bind(MangaHistory item) {
			super.bind(item);
			mText1.setText(item.name);
			mText2.setText(ResourceUtils.formatTimeRelative(item.updatedAt));
			mSummary.setText(item.genres);
			ImageUtils.setThumbnail(mImageView, item.thumbnail, MangaProvider.getDomain(item.provider));
		}

		@Override
		public void recycle() {
			super.recycle();
			ImageUtils.recycle(mImageView);
		}

		@Override
		public void onClick(View view) {
			final Context context = view.getContext();
			final MangaHistory item = getData();
			if (item != null) {
				switch (view.getId()) {
					default:
						context.startActivity(new Intent(context.getApplicationContext(), PreviewActivity.class)
								.putExtra("manga", item));
				}
			}
		}
	}

	static class DetailedHistoryHolder extends HistoryHolder {

		private final RatingBar mRatingBar;
		private final TextView mTextViewStatus;
		@ColorInt
		private final int mPrimaryColor;
		@ColorInt
		private final int mAccentColor;

		private DetailedHistoryHolder(View itemView) {
			super(itemView);
			mRatingBar = itemView.findViewById(R.id.ratingBar);
			mTextViewStatus = itemView.findViewById(R.id.textView_status);
			mPrimaryColor = ThemeUtils.getThemeAttrColor(itemView.getContext(), R.attr.colorPrimary);
			mAccentColor = ThemeUtils.getThemeAttrColor(itemView.getContext(), R.attr.colorAccent);
		}

		@Override
		public void bind(MangaHistory item) {
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
}
