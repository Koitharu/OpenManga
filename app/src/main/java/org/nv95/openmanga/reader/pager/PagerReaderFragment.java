package org.nv95.openmanga.reader.pager;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import org.nv95.openmanga.R;
import org.nv95.openmanga.core.models.MangaPage;
import org.nv95.openmanga.reader.OnOverScrollListener;
import org.nv95.openmanga.reader.ReaderFragment;

import java.util.List;

/**
 * Created by koitharu on 09.01.18.
 */

public final class PagerReaderFragment extends ReaderFragment implements ViewPager.OnPageChangeListener {

	private OverScrollPager mPager;
	private PagerReaderAdapter mAdapter;

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_reader_pager, container, false);
	}

	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
		mPager = view.findViewById(R.id.pager);
		mAdapter = new PagerReaderAdapter(getPages(), new GestureDetector(view.getContext(), new TapDetector()));
		mPager.setOffscreenPageLimit(2);
		mPager.setAdapter(mAdapter);
		mPager.addOnPageChangeListener(this);
	}

	@Override
	public void onActivityCreated(@Nullable Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		final Activity activity = getActivity();
		if (activity instanceof OnOverScrollListener) {
			mPager.setOnOverScrollListener((OnOverScrollListener) activity);
		}
	}

	@Override
	public void setPages(List<MangaPage> pages) {
		super.setPages(pages);
		mAdapter.notifyDataSetChanged();
	}

	@Override
	public int getCurrentPageIndex() {
		return mPager.getCurrentItem();
	}

	@Override
	public void scrollToPage(int index) {
		mPager.setCurrentItem(index, false);
	}

	@Override
	public void smoothScrollToPage(int index) {
		mPager.setCurrentItem(index, true);
	}

	@Override
	public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

	}

	@Override
	public void onPageSelected(int position) {
		onPageChanged(position);
	}

	@Override
	public void onPageScrollStateChanged(int state) {

	}

	@Override
	public void moveLeft() {
		smoothScrollToPage(getCurrentPageIndex() - 1);
	}

	@Override
	public void moveRight() {
		smoothScrollToPage(getCurrentPageIndex() + 1);
	}

	@Override
	public void moveUp() {

	}

	@Override
	public void moveDown() {

	}

	@Override
	public void moveNext() {
		smoothScrollToPage(getCurrentPageIndex() + 1);
	}

	private final class TapDetector extends GestureDetector.SimpleOnGestureListener {

		@Override
		public boolean onSingleTapConfirmed(MotionEvent e) {
			if (mPager == null) {
				return false;
			}
			final float x = e.getX();
			final float w3 = mPager.getWidth() / 3;
			if (x < w3) {
				moveLeft();
			} else if (x <= w3 + w3) {
				final float y = e.getY();
				final float h3 = mPager.getHeight() / 3;
				if (y < h3) {
					moveLeft();
				} else if (y <= h3 + h3) {
					toggleUi();
				} else {
					moveRight();
				}
			} else {
				moveRight();
			}
			return true;
		}
	}
}
