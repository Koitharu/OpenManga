package org.nv95.openmanga.reader.webtoon;

import android.content.Context;
import android.graphics.PointF;
import android.util.AttributeSet;

import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;

public final class WebtoonImageView extends SubsamplingScaleImageView {

	public WebtoonImageView(Context context) {
		this(context, null);
	}

	public WebtoonImageView(Context context, AttributeSet attr) {
		super(context, attr);
	}

	@Override
	protected void onReady() {
		setupScale();
	}

	void setupScale() {
		setMinScale(getWidth() / (float) getSWidth());
		setScaleAndCenter(
				getMinScale(),
				new PointF(getSWidth() / 2f, 0)
		);
	}
}
