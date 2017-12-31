package org.nv95.openmanga.ui;

import android.content.res.ColorStateList;
import android.graphics.Typeface;
import android.support.annotation.IntDef;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.nv95.openmanga.R;
import org.nv95.openmanga.content.MangaChapter;
import org.nv95.openmanga.utils.CollectionsUtils;
import org.nv95.openmanga.utils.ResourceUtils;
import org.nv95.openmanga.utils.ThemeUtils;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by koitharu on 26.12.17.
 */

public final class ChaptersListAdapter extends RecyclerView.Adapter {

	private final ArrayList<MangaChapter> mDataset;
	private int mLastChapterPosition;
	private long mLastChapterTimestamp;
	private final OnChapterClickListener mClickListener;
	@Nullable
	private ColorStateList[] mTextColors;
	private boolean mReversed = false;

	public ChaptersListAdapter(ArrayList<MangaChapter> dataset, OnChapterClickListener listener) {
		mClickListener = listener;
		mDataset = dataset;
		mLastChapterPosition = -1;
		mLastChapterTimestamp = 0;
		setHasStableIds(true);
	}

	public void setLastChapter(long id, long timestamp) {
		mLastChapterPosition = CollectionsUtils.findPositionById(mDataset, id);
		mLastChapterTimestamp = timestamp;
	}

	public void reverse() {
		Collections.reverse(mDataset);
		notifyDataSetChanged();
		mReversed = !mReversed;
	}

	public boolean isReversed() {
		return mReversed;
	}

	@Override
	public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, @ItemViewType int viewType) {
		switch (viewType) {
			case ItemViewType.TYPE_HEADER:
				return new HeaderHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.header_chapter, parent, false), mClickListener);
			case ItemViewType.TYPE_ITEM_DEFAULT:
				return new ChapterHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_single_line, parent, false), mClickListener);
				default:
					throw new AssertionError("Unknown viewType");
		}
	}

	@Override
	public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
		if (mTextColors == null) {
			mTextColors = new ColorStateList[] {
					ThemeUtils.getAttrColorStateList(holder.itemView.getContext(), android.R.attr.textColorPrimary),
					ThemeUtils.getAttrColorStateList(holder.itemView.getContext(), android.R.attr.textColorSecondary)
			};
		}
		if (holder instanceof ChapterHolder) {
			MangaChapter ch = mDataset.get(position - (mLastChapterPosition == -1 ? 0 : 1));
			TextView tv = ((ChapterHolder) holder).getTextView();
			tv.setText(ch.name);
			tv.setTextColor(position >= mLastChapterPosition ? mTextColors[0] : mTextColors[1]);
			tv.setTypeface(position == mLastChapterPosition ? Typeface.DEFAULT_BOLD : Typeface.DEFAULT);
		} else if (holder instanceof HeaderHolder) {
			MangaChapter ch = CollectionsUtils.getOrNull(mDataset, mLastChapterPosition);
			if (ch != null) {
				((HeaderHolder) holder).textViewTitle.setText(ch.name);
				((HeaderHolder) holder).textViewSubtitle.setText(ResourceUtils.formatDateTimeRelative(holder.itemView.getContext(), mLastChapterTimestamp));
			}
		}
	}

	@ItemViewType
	@Override
	public int getItemViewType(int position) {
		return (position == 0 || mLastChapterPosition == -1) ? ItemViewType.TYPE_ITEM_DEFAULT : ItemViewType.TYPE_HEADER;
	}

	@Override
	public long getItemId(int position) {
		if (mLastChapterPosition == -1) {
			return mDataset.get(position).id;
		} else {
			return position == 0 ? 0 : mDataset.get(position - 1).id;
		}
	}

	@Override
	public int getItemCount() {
		return mDataset.size() + (mLastChapterPosition == -1 ? 0 : 1);
	}

	class HeaderHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

		private final OnChapterClickListener mListener;
		final TextView textViewTitle;
		final TextView textViewSubtitle;

		HeaderHolder(View itemView, OnChapterClickListener listener) {
			super(itemView);
			itemView.findViewById(R.id.button_positive).setOnClickListener(this);
			mListener = listener;
			textViewTitle = itemView.findViewById(R.id.textView_title);
			textViewSubtitle = itemView.findViewById(R.id.textView_subtitle);
		}

		@Override
		public void onClick(View v) {
			mListener.onChapterClick(-1, CollectionsUtils.getOrNull(mDataset, mLastChapterPosition));
		}
	}

	class ChapterHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {

		private final OnChapterClickListener mListener;

		ChapterHolder(View itemView, OnChapterClickListener listener) {
			super(itemView);
			itemView.setOnClickListener(this);
			itemView.setOnLongClickListener(this);
			mListener = listener;
		}

		public TextView getTextView() {
			return (TextView) itemView;
		}

		@Override
		public void onClick(View v) {
			final int pos = getAdapterPosition();
			mListener.onChapterClick(pos, CollectionsUtils.getOrNull(mDataset, pos - (mLastChapterPosition == -1 ? 0 : 1)));
		}

		@Override
		public boolean onLongClick(View view) {
			final int pos = getAdapterPosition();
			return mListener.onChapterLongClick(pos, CollectionsUtils.getOrNull(mDataset, pos - (mLastChapterPosition == -1 ? 0 : 1)));
		}
	}

	public interface OnChapterClickListener {
		void onChapterClick(int pos, MangaChapter chapter);
		boolean onChapterLongClick(int pos, MangaChapter chapter);
	}

	@Retention(RetentionPolicy.SOURCE)
	@IntDef({ItemViewType.TYPE_ITEM_DEFAULT, ItemViewType.TYPE_HEADER})
	public @interface ItemViewType {
		int TYPE_ITEM_DEFAULT = 0;
		int TYPE_HEADER = 1;
	}
}
