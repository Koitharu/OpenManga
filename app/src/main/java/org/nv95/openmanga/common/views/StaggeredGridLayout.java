package org.nv95.openmanga.common.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import java.util.Arrays;

public class StaggeredGridLayout extends ViewGroup {

	private int mColumns = 2;
	private float mColumnWidth = 0;
	private int mLayoutHeight = 0;
	private int[] mColumnsHeights = new int[mColumns];

	public StaggeredGridLayout(Context context) {
		this(context, null, 0);
	}

	public StaggeredGridLayout(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public StaggeredGridLayout(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}

	public void setColumnsCount(int columnsCount) {
		mColumns = columnsCount;
		mColumnsHeights = new int[mColumns];
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		Arrays.fill(mColumnsHeights, 0);
		// Total layout width
		int layoutWidth = MeasureSpec.getSize(widthMeasureSpec);
		// Usable layout width for children once padding is removed
		int layoutUsableWidth = layoutWidth - getPaddingLeft() - getPaddingRight();
		if (layoutUsableWidth < 0)
			layoutUsableWidth = 0;

		// Total layout height
		mLayoutHeight = MeasureSpec.getSize(heightMeasureSpec);

		// Calculate width assigned to each column: the usable width divided by
		// the number of columns, minus horizontal spacing
		mColumnWidth = layoutUsableWidth / mColumns
				- ((getPaddingLeft() * (mColumns - 1)) / mColumns);

		// Measure each children
		for (int i = 0; i < getChildCount(); i++) {
			View child = getChildAt(i);
			// force the width of the children to be the width previously
			// calculated for columns...
			int childWidthSpec = MeasureSpec.makeMeasureSpec(
					(int) mColumnWidth, MeasureSpec.EXACTLY);
			// ... but let them grow vertically
			int childHeightSpec = MeasureSpec.makeMeasureSpec(0,
					MeasureSpec.UNSPECIFIED);
			child.measure(childWidthSpec, childHeightSpec);
		}

		// Get the final total height of the layout. It will be that of the
		// higher column once all chidren are in place. Every child is added to
		// the sortest column at the moment of addition
		for (int i = 0; i < getChildCount(); i++) {
			int column = i % mColumns;
			mColumnsHeights[column] += getChildAt(i).getMeasuredHeight() + getPaddingTop();
		}
		int mFinalHeight = mColumnsHeights[0];
		for (int i = 0;i < mColumns; i++) {
			if (mFinalHeight < mColumnsHeights[i]) {
				mFinalHeight = mColumnsHeights[i];
			}
		}

		setMeasuredDimension(layoutWidth, mFinalHeight);
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		Arrays.fill(mColumnsHeights, 0);
		for (int i = 0; i < getChildCount(); i++) {
			final View view = getChildAt(i);
			int column = i % mColumns;
			int left = getPaddingLeft() + l + (int) (mColumnWidth * column)
					+ (getPaddingLeft() * column);
			view.layout(left, mColumnsHeights[column] + getPaddingTop(),
					left + view.getMeasuredWidth(), mColumnsHeights[column]
							+ view.getMeasuredHeight() + getPaddingTop());
			mColumnsHeights[column] = mColumnsHeights[column]
					+ view.getMeasuredHeight() + getPaddingTop();
		}
	}
}
