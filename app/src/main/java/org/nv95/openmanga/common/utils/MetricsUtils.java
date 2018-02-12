package org.nv95.openmanga.common.utils;

import android.content.res.Resources;
import android.support.annotation.DimenRes;
import android.support.annotation.UiThread;
import android.view.View;

import org.nv95.openmanga.R;

/**
 * Created by koitharu on 29.01.18.
 */

public abstract class MetricsUtils {

	private static final float DEF_ASPECT_RATIO = 18f / 13f;

	public static Size getPreferredCellSizeMedium(Resources resources) {
		return getPreferredCellSize(resources, R.dimen.column_width_medium);
	}

	public static Size getPreferredCellSize(Resources resources, @DimenRes int columnWidthDimen) {
		int columns = getPreferredColumnsCount(resources, columnWidthDimen);
		int width = (int) Math.floor(resources.getDisplayMetrics().widthPixels / (float) columns);
		return new Size(width, (int) (width * DEF_ASPECT_RATIO));
	}

	public static int getPreferredColumnsCountMedium(Resources resources) {
		return getPreferredColumnsCount(resources, R.dimen.column_width_medium);
	}

	public static int getPreferredColumnsCount(Resources resources, @DimenRes int columnWidthDimen) {
		int totalWidth = resources.getDisplayMetrics().widthPixels;
		int columnWidth = resources.getDimensionPixelSize(columnWidthDimen);
		return Math.max(1, Math.round(totalWidth / (float) columnWidth));
	}

	public static class Size {

		public final int width;
		public final int height;

		public Size(int width, int height) {
			this.width = width;
			this.height = height;
		}

		@UiThread
		public static Size from(View view) {
			return new Size(view.getWidth(), view.getHeight());
		}
	}
}
