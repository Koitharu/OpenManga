package org.nv95.openmanga.shelf;

import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * Created by koitharu on 24.12.17.
 */

public class ShelfItemSpaceDecoration extends RecyclerView.ItemDecoration {

	private final int mSpacing;
	private final int mMaxSpanSize;
	private final RecyclerView.Adapter mAdapter;

	public ShelfItemSpaceDecoration(int spacing, RecyclerView.Adapter adapter, int maxSpanSize) {
		mSpacing = spacing;
		mAdapter = adapter;
		mMaxSpanSize = maxSpanSize;
	}

	@Override
	public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
		final int position = parent.getChildAdapterPosition(view);
		final int itemType = mAdapter.getItemViewType(position);
		switch (itemType) {
			case ShelfItemType.TYPE_RECENT:
			case ShelfItemType.TYPE_TIP:
			case ShelfItemType.TYPE_ITEM_DEFAULT:
			case ShelfItemType.TYPE_ITEM_SMALL:
				outRect.left = mSpacing;
				outRect.right = mSpacing;
				outRect.bottom = mSpacing;
				outRect.top = mSpacing;
		}
	}
}
