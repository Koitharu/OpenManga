package org.nv95.openmanga.common.views;

import android.content.Context;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;
/**
 * Created by koitharu on 24.12.17.
 */

public class AspectRatioImageView extends AppCompatImageView {

	protected double mAspectRatio = 18f / 13f;

	public AspectRatioImageView(Context context) {
		super(context);
	}

	public AspectRatioImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public AspectRatioImageView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}

	public void setAspectRatio(float value) {
		mAspectRatio = value;
	}

	public void setAspectRatio(int height, int width) {
		mAspectRatio = height / (float)width;
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int originalWidth = MeasureSpec.getSize(widthMeasureSpec);
		int calculatedHeight = (int) (originalWidth * mAspectRatio);
		super.onMeasure(
				MeasureSpec.makeMeasureSpec(originalWidth, MeasureSpec.EXACTLY),
				MeasureSpec.makeMeasureSpec(calculatedHeight, MeasureSpec.EXACTLY)
		);
	}
}
