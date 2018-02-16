package org.nv95.openmanga.reader.webtoon;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import org.nv95.openmanga.R;
import org.nv95.openmanga.common.utils.LayoutUtils;
import org.nv95.openmanga.reader.ReaderFragment;

public final class WebtoonReaderFragment extends ReaderFragment {

	private WebtoonRecyclerView mRecyclerView;

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_reader_webtoon, container, false);
	}

	@Override
	@SuppressLint("ClickableViewAccessibility")
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		mRecyclerView = view.findViewById(R.id.recyclerView);
		mRecyclerView.setHasFixedSize(true);
		mRecyclerView.setAdapter(new WebtoonReaderAdapter(mPages));
		final GestureDetector detector = new GestureDetector(view.getContext(), new TapDetector());
		mRecyclerView.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				return detector.onTouchEvent(event);
			}
		});
	}

	@Override
	public int getCurrentPageIndex() {
		return LayoutUtils.findFirstVisibleItemPosition(mRecyclerView);
	}

	@Override
	public void scrollToPage(int index) {
		LayoutUtils.setSelectionFromTop(mRecyclerView, index);
	}

	@Override
	public void smoothScrollToPage(int index) {
		mRecyclerView.smoothScrollToPosition(index);
	}

	@Override
	public boolean moveLeft() {
		return false;
	}

	@Override
	public boolean moveRight() {
		return false;
	}

	@Override
	public boolean moveUp() {
		return false;
	}

	@Override
	public boolean moveDown() {
		return false;
	}

	@Override
	public void onRestoreState(@NonNull Bundle savedState) {
		super.onRestoreState(savedState);
	}

	private final class TapDetector extends GestureDetector.SimpleOnGestureListener {

		@Override
		public boolean onSingleTapConfirmed(MotionEvent e) {
			if (mRecyclerView == null) {
				return false;
			}
			final float x = e.getX();
			final float w3 = mRecyclerView.getWidth() / 3;
			if (x < w3) {
				return moveLeft();
			} else if (x <= w3 + w3) {
				final float y = e.getY();
				final float h3 = mRecyclerView.getHeight() / 3;
				if (y < h3) {
					return moveLeft();
				} else if (y <= h3 + h3) {
					toggleUi();
					return true;
				} else {
					return moveRight();
				}
			} else {
				return moveRight();
			}
		}
	}
}
