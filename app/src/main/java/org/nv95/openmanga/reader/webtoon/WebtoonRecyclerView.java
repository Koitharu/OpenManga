package org.nv95.openmanga.reader.webtoon;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;

import org.nv95.openmanga.R;

public final class WebtoonRecyclerView extends RecyclerView {

	@Nullable
	private GestureDetector mGestureDetector = null;

	public WebtoonRecyclerView(Context context) {
		this(context, null, 0);
	}

	public WebtoonRecyclerView(Context context, @Nullable AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public WebtoonRecyclerView(Context context, @Nullable AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		if (mGestureDetector != null) {
			mGestureDetector.onTouchEvent(ev);
		}
		return super.dispatchTouchEvent(ev);
	}

	public void setOnGestureListener(@NonNull GestureDetector.SimpleOnGestureListener listener) {
		mGestureDetector = new GestureDetector(getContext(), listener);
	}

	@Nullable
	private WebtoonImageView getWebtoonView(int pos) {
		ViewHolder vh = findViewHolderForAdapterPosition(pos);
		return vh != null ? vh.itemView.findViewById(R.id.webtoonImageView) : null;
	}
}
