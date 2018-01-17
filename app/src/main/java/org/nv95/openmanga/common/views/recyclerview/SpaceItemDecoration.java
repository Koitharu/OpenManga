package org.nv95.openmanga.common.views.recyclerview;

import android.graphics.Rect;
import android.support.annotation.Px;
import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * Created by koitharu on 13.01.18.
 */

public final class SpaceItemDecoration extends RecyclerView.ItemDecoration {

	@Px
	private final int mSpacing;

	public SpaceItemDecoration(@Px int spacing) {
		mSpacing = spacing;
	}

	@Override
	public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
		outRect.set(mSpacing, mSpacing, mSpacing, mSpacing);
	}
}
