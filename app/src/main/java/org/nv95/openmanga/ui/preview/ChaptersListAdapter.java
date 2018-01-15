package org.nv95.openmanga.ui.preview;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.TextViewCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.nv95.openmanga.R;
import org.nv95.openmanga.content.MangaChapter;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by koitharu on 26.12.17.
 */

public final class ChaptersListAdapter extends RecyclerView.Adapter<ChaptersListAdapter.ChapterHolder> {

	private final ArrayList<MangaChapter> mDataset;
	private final OnChapterClickListener mClickListener;
	private long mCurrentId;
	private boolean mReversed = false;
	private final Drawable[] mIcons;

	public ChaptersListAdapter(Context context, ArrayList<MangaChapter> dataset, OnChapterClickListener listener) {
		mClickListener = listener;
		mDataset = dataset;
		mCurrentId = 0;
		setHasStableIds(true);
		mIcons = new Drawable[] {
				ContextCompat.getDrawable(context, R.drawable.ic_play_green)
		};
	}

	public void setCurrentChapterId(long id) {
		mCurrentId = id;
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
	public ChapterHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		return new ChapterHolder(LayoutInflater.from(parent.getContext())
				.inflate(R.layout.item_single_line, parent, false), mClickListener);
	}

	@Override
	public void onBindViewHolder(ChapterHolder holder, int position) {
		MangaChapter ch = mDataset.get(position);
		TextView tv = holder.getTextView();
		tv.setText(ch.name);
		TextViewCompat.setCompoundDrawablesRelativeWithIntrinsicBounds(
				tv,
				ch.id == mCurrentId ? mIcons[0] : null,
				null,
				null,
				null
		);
	}

	@Override
	public long getItemId(int position) {
		return mDataset.get(position).id;
	}

	@Override
	public int getItemCount() {
		return mDataset.size();
	}

	final class ChapterHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {

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
			mListener.onChapterClick(pos, mDataset.get(pos));
		}

		@Override
		public boolean onLongClick(View view) {
			final int pos = getAdapterPosition();
			return mListener.onChapterLongClick(pos, mDataset.get(pos));
		}
	}

	public interface OnChapterClickListener {
		void onChapterClick(int pos, MangaChapter chapter);
		boolean onChapterLongClick(int pos, MangaChapter chapter);
	}
}
