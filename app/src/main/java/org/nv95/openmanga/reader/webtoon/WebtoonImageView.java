package org.nv95.openmanga.reader.webtoon;

import android.content.Context;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.view.View;

import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;

import java.util.Vector;

public final class WebtoonImageView extends SubsamplingScaleImageView {

	private final static Vector<WebtoonImageView> sInstances = new Vector<>(3);

	public WebtoonImageView(Context context) {
		this(context, null);
	}

	public WebtoonImageView(Context context, AttributeSet attr) {
		super(context, attr);
		setOnStateChangedListener(new OnStateChangedListener() {
			@Override
			public void onScaleChanged(float newScale, int origin) {
				for (WebtoonImageView o : sInstances) {
					o.setScaleAndCenter(newScale, o.getCenter());
				}
			}

			@Override
			public void onCenterChanged(PointF newCenter, int origin) {

			}
		});
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

	@Override
	protected void onAttachedToWindow() {
		super.onAttachedToWindow();
		sInstances.add(this);
	}

	@Override
	protected void onDetachedFromWindow() {
		sInstances.remove(this);
		super.onDetachedFromWindow();
	}

	/*@Override
	public boolean onTouchEvent(@NonNull MotionEvent event) {
		if (event.getPointerCount() == 1) {
			switch (event.getActionMasked()) {
				case MotionEvent.ACTION_DOWN:
					mOldY = event.getY();
					break;
				case MotionEvent.ACTION_MOVE:
					final float dY = event.getY() - mOldY;
					Log.d("SC", "dY = " + dY + "; pTop = " + getParentTop() + "; pBott = " + getParentBottom());
					if ((dY < 0 && getParentTop() > 0) || (dY > 0 && getParentTop() < 0)) {
						return false;
					}

			}
		}
		return super.onTouchEvent(event);
	}*/

	private int getParentTop() {
		return ((View)this.getParent()).getTop();
	}

	private int getParentBottom() {
		final View parent = (View) getParent();
		return ((View) parent.getParent().getParent()).getHeight() - parent.getBottom();
	}
}
