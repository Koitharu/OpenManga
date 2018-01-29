package org.nv95.openmanga.discover.bookmarks;

import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;

/**
 * Created by koitharu on 29.01.18.
 */

public class BookmarkSpanSizeLookup extends GridLayoutManager.SpanSizeLookup {

	private final int mMaxSpans;
	private final RecyclerView.Adapter mAdapter;

	public BookmarkSpanSizeLookup(RecyclerView.Adapter adapter, int maxSpans) {
		mAdapter = adapter;
		mMaxSpans = maxSpans;
	}

	@Override
	public int getSpanSize(int position) {
		switch (mAdapter.getItemViewType(position)) {
			case BookmarksListAdapter.ItemViewType.TYPE_ITEM_HEADER:
				return mMaxSpans;
			default:
				return 1;
		}
	}
}
