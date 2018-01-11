package org.nv95.openmanga.ui.reader.pager;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.nv95.openmanga.content.MangaPage;
import org.nv95.openmanga.ui.reader.ReaderFragment;

import java.util.List;

/**
 * Created by koitharu on 09.01.18.
 */

public final class PagerReaderFragment extends ReaderFragment implements ViewPager.OnPageChangeListener {

	private ViewPager mPager;
	private PagerReaderAdapter mAdapter;

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mAdapter = new PagerReaderAdapter(getPages());
	}

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
		assert container != null;
		ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
		mPager = new ViewPager(container.getContext());
		mPager.setLayoutParams(params);
		return mPager;
	}

	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		mPager.setOffscreenPageLimit(1);
		mPager.setAdapter(mAdapter);
		mPager.addOnPageChangeListener(this);
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
}
