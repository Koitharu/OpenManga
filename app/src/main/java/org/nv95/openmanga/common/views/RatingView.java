package org.nv95.openmanga.common.views;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.DrawableUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;

import org.nv95.openmanga.R;

/**
 * Created by koitharu on 26.12.17.
 */

public class RatingView extends AppCompatTextView {

	private final Drawable mStarDrawable;
	private static int[] sColors = null;

	public RatingView(Context context) {
		this(context, null, 0);
	}

	public RatingView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public RatingView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		if (sColors == null) {
			sColors = new int[] {
					ContextCompat.getColor(context, R.color.rating_1),
					ContextCompat.getColor(context, R.color.rating_2),
					ContextCompat.getColor(context, R.color.rating_3),
					ContextCompat.getColor(context, R.color.rating_4),
					ContextCompat.getColor(context, R.color.rating_5)
			};
		}
		setLines(1);
		setSingleLine(true);
		mStarDrawable = ContextCompat.getDrawable(context, R.drawable.ic_star);
		setCompoundDrawablesWithIntrinsicBounds(mStarDrawable, null, null, null);
	}

	@SuppressLint("SetTextI18n")
	public void setRating(short value) {
		if (value == 0) {
			setVisibility(View.INVISIBLE);
		} else {
			setVisibility(View.VISIBLE);
		}
		setText(value + "%");
		int color;
		if (value >= 90) {
			color = sColors[4];
		} else if (value >= 85) {
			color = sColors[3];
		} else if (value >= 60) {
			color = sColors[2];
		} else if (value >= 45) {
			color = sColors[1];
		} else {
			color = sColors[0];
		}
		mStarDrawable.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
	}
}
