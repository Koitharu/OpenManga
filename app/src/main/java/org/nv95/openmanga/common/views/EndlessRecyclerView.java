package org.nv95.openmanga.common.views;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;

import org.nv95.openmanga.common.utils.LayoutUtils;

/**
 * Created by koitharu on 07.01.18.
 */

public final class EndlessRecyclerView extends RecyclerView {

	private final EndlessScrollHelper mEndlessScrollHelper;
	@Nullable
	private OnLoadMoreListener mOnLoadMoreListener = null;
	private boolean mLoadingEnabled = false;

	public EndlessRecyclerView(Context context) {
		this(context, null, 0);
	}

	public EndlessRecyclerView(Context context, @Nullable AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public EndlessRecyclerView(Context context, @Nullable AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		mEndlessScrollHelper = new EndlessScrollHelper();
		this.addOnScrollListener(mEndlessScrollHelper);
	}

	@Override
	public void setAdapter(Adapter adapter) {
		super.setAdapter(adapter);
		if (adapter != null && adapter instanceof EndlessAdapter) {
			mLoadingEnabled = true;
			mEndlessScrollHelper.mLoading = false;
			((EndlessAdapter) adapter).setHasNext(true);
		}
	}

	public void setOnLoadMoreListener(@Nullable OnLoadMoreListener onLoadMoreListener) {
		mOnLoadMoreListener = onLoadMoreListener;
	}

	public void onLoadingStarted() {
		mEndlessScrollHelper.mLoading = true;
	}

	public void onLoadingFinished(boolean enableNext) {
		mLoadingEnabled = enableNext;
		mEndlessScrollHelper.mLoading = false;
		final Adapter adapter = getAdapter();
		if (adapter != null && adapter instanceof EndlessAdapter) {
			((EndlessAdapter) adapter).setHasNext(enableNext);
		}
	}

	private class EndlessScrollHelper extends OnScrollListener {

		private static final int VISIBLE_THRESHOLD = 2;

		private int mLastVisibleItem, mTotalItemCount;
		private boolean mLoading = false;

		@Override
		public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
			super.onScrolled(recyclerView, dx, dy);
			mTotalItemCount = LayoutUtils.getItemCount(recyclerView);
			mLastVisibleItem = LayoutUtils.findLastVisibleItemPosition(recyclerView);
			if (!mLoading && mLoadingEnabled && mTotalItemCount <= (mLastVisibleItem + VISIBLE_THRESHOLD)) {
				// End has been reached
				// Do something
				if (mOnLoadMoreListener != null) {
					mLoading = mOnLoadMoreListener.onLoadMore();
				}
			}
		}
	}

	public interface OnLoadMoreListener {

		boolean onLoadMore();
	}

	public interface EndlessAdapter {
		void setHasNext(boolean hasNext);
	}
}
