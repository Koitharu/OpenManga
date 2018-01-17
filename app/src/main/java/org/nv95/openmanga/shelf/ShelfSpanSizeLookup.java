package org.nv95.openmanga.shelf;

import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;

/**
 * Created by koitharu on 24.12.17.
 */

public class ShelfSpanSizeLookup extends GridLayoutManager.SpanSizeLookup {

	private final int mMaxSpans;
	private final RecyclerView.Adapter mAdapter;

	public ShelfSpanSizeLookup(RecyclerView.Adapter adapter, int maxSpans) {
		mAdapter = adapter;
		mMaxSpans = maxSpans;
	}

	@Override
	public int getSpanSize(int position) {
		switch (mAdapter.getItemViewType(position)) {
			case ShelfItemType.TYPE_ITEM_DEFAULT:
				return 4;
			case ShelfItemType.TYPE_ITEM_SMALL:
				return 3;
			case ShelfItemType.TYPE_TIP:
			case ShelfItemType.TYPE_RECENT:
			case ShelfItemType.TYPE_HEADER:
				return mMaxSpans;
			default:
				return 1;
		}
	}
}
