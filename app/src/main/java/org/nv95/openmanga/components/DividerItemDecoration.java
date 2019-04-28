package org.nv95.openmanga.components;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import androidx.annotation.DrawableRes;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;

import org.nv95.openmanga.utils.LayoutUtils;

/**
 * source:
 * https://stackoverflow.com/questions/24618829/how-to-add-dividers-and-spaces-between-items-in-recyclerview
 */

public class DividerItemDecoration extends RecyclerView.ItemDecoration {

    private Drawable mDivider;

    /**
     * Default divider will be used
     */
    public DividerItemDecoration(Context context) {
        mDivider = LayoutUtils.getAttrDrawable(context, android.R.attr.listDivider);
    }

    /**
     * Custom divider will be used
     */
    public DividerItemDecoration(Context context, @DrawableRes int resId) {
        mDivider = ContextCompat.getDrawable(context, resId);
    }

    @Override
    public void onDrawOver(Canvas c, RecyclerView parent, RecyclerView.State state) {
        int left = parent.getPaddingLeft();
        int right = parent.getWidth() - parent.getPaddingRight();

        int childCount = parent.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View child = parent.getChildAt(i);

            RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child.getLayoutParams();

            int top = child.getBottom() + params.bottomMargin;
            int bottom = top + mDivider.getIntrinsicHeight();

            mDivider.setBounds(left, top, right, bottom);
            mDivider.draw(c);
        }
    }
}