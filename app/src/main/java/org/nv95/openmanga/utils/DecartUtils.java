package org.nv95.openmanga.utils;

import android.graphics.Rect;

/**
 * Created by admin on 16.08.17.
 */

public class DecartUtils {

    public static void scaleRect(Rect outRect, float scaleFactor) {
        outRect.top *= scaleFactor;
        outRect.right *= scaleFactor;
        outRect.bottom *= scaleFactor;
        outRect.left *= scaleFactor;
    }

    public static void trimRect(Rect outRect, Rect bounds) {
        if (outRect.top < bounds.top) outRect.top = bounds.top;
        if (outRect.left < bounds.left) outRect.left = bounds.left;
        if (outRect.right > bounds.right) outRect.right = bounds.right;
        if (outRect.bottom > bounds.bottom) outRect.bottom = bounds.bottom;
    }

    public static void translateRect(Rect outRect, int dX, int dY) {
        outRect.top += dY;
        outRect.left += dX;
        outRect.bottom += dY;
        outRect.right += dX;
    }
}
