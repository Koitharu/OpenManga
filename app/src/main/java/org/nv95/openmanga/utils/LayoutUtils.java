package org.nv95.openmanga.utils;

import android.app.ActivityManager;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

/**
 * Created by koitharu on 26.12.17.
 */

public final class LayoutUtils {

	public static int getItemCount(RecyclerView recyclerView) {
		RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
		return layoutManager == null ? 0 : layoutManager.getItemCount();
	}

	public static int findLastCompletelyVisibleItemPosition(RecyclerView recyclerView) {
		RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
		final View child = findOneVisibleChild(layoutManager, layoutManager.getChildCount() - 1, -1, true, false);
		return child == null ? RecyclerView.NO_POSITION : recyclerView.getChildAdapterPosition(child);
	}

	public static int findLastVisibleItemPosition(RecyclerView recyclerView) {
		RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
		final View child = findOneVisibleChild(layoutManager, layoutManager.getChildCount() - 1, -1, false, true);
		return child == null ? RecyclerView.NO_POSITION : recyclerView.getChildAdapterPosition(child);
	}

	public static int findFirstVisibleItemPosition(RecyclerView recyclerView) {
		RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
		final View child = findOneVisibleChild(layoutManager, 0, layoutManager.getChildCount(), false, true);
		return child == null ? RecyclerView.NO_POSITION : recyclerView.getChildAdapterPosition(child);
	}

	public static int findFirstCompletelyVisibleItemPosition(RecyclerView recyclerView) {
		RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
		final View child = findOneVisibleChild(layoutManager, 0, layoutManager.getChildCount(), true, false);
		return child == null ? RecyclerView.NO_POSITION : recyclerView.getChildAdapterPosition(child);
	}

	private static View findOneVisibleChild(RecyclerView.LayoutManager layoutManager, int fromIndex, int toIndex,
											boolean completelyVisible, boolean acceptPartiallyVisible) {
		OrientationHelper helper;
		if (layoutManager.canScrollVertically()) {
			helper = OrientationHelper.createVerticalHelper(layoutManager);
		} else {
			helper = OrientationHelper.createHorizontalHelper(layoutManager);
		}

		final int start = helper.getStartAfterPadding();
		final int end = helper.getEndAfterPadding();
		final int next = toIndex > fromIndex ? 1 : -1;
		View partiallyVisible = null;
		for (int i = fromIndex; i != toIndex; i += next) {
			final View child = layoutManager.getChildAt(i);
			final int childStart = helper.getDecoratedStart(child);
			final int childEnd = helper.getDecoratedEnd(child);
			if (childStart < end && childEnd > start) {
				if (completelyVisible) {
					if (childStart >= start && childEnd <= end) {
						return child;
					} else if (acceptPartiallyVisible && partiallyVisible == null) {
						partiallyVisible = child;
					}
				} else {
					return child;
				}
			}
		}
		return partiallyVisible;
	}

	public static void showSoftKeyboard(@NonNull View view) {
		view.requestFocus();
		InputMethodManager imm = (InputMethodManager) view.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
		if (imm != null) {
			imm.showSoftInput(view, 0);
		}
	}

	public static void hideSoftKeyboard(@NonNull View view) {
		InputMethodManager imm = (InputMethodManager)view.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
		if (imm != null) {
			imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
		}
	}
}
