package org.nv95.openmanga.common.utils;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ListView;
import android.widget.TextView;

/**
 * Created by koitharu on 26.12.17.
 */

public abstract class LayoutUtils {

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

	public static void setTextOrHide(TextView textView, String text) {
		if (android.text.TextUtils.isEmpty(text)) {
			textView.setVisibility(View.GONE);
		} else {
			textView.setText(text);
			textView.setVisibility(View.VISIBLE);
		}
	}

	public static void setSelectionFromTop(RecyclerView recyclerView, int position) {
		Log.d("Scroll", "#" + position);
		final RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
		if (layoutManager != null) {
			if (layoutManager instanceof LinearLayoutManager) {
				((LinearLayoutManager) layoutManager).scrollToPositionWithOffset(position, 0);
			} else {
				layoutManager.scrollToPosition(position);
			}
		}
	}

	public static void scrollToCenter(RecyclerView recyclerView, int position) {
		final int firstPos = findFirstVisibleItemPosition(recyclerView);
		final int lastPos = findLastVisibleItemPosition(recyclerView);
		final int count = getItemCount(recyclerView);
		if (position < firstPos) {
			setSelectionFromTop(recyclerView, calculateScrollPos(firstPos, position, count));
		} else if (position > lastPos) {
			setSelectionFromTop(recyclerView, calculateScrollPos(position, position - lastPos + firstPos, count));
		}
	}

	private static int calculateScrollPos(int a, int b, int max) {
		return Math.min((a + b) / 2, max);
	}

	public static void forceUpdate(@NonNull RecyclerView recyclerView) {
		int pos = findFirstCompletelyVisibleItemPosition(recyclerView);
		final RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
		final RecyclerView.Adapter adapter = recyclerView.getAdapter();
		recyclerView.setAdapter(null);
		recyclerView.setLayoutManager(null);
		recyclerView.setAdapter(adapter);
		recyclerView.setLayoutManager(layoutManager);
		adapter.notifyDataSetChanged();
		setSelectionFromTop(recyclerView, pos);
	}

	public static void checkAll(@NonNull ListView listView) {
		final int size = listView.getAdapter().getCount();
		for (int i = 0; i <= size; i++) {
			listView.setItemChecked(i, true);
		}
	}
}
