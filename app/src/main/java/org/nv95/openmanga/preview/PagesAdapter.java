package org.nv95.openmanga.preview;

import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by koitharu on 22.01.18.
 */

final class PagesAdapter extends PagerAdapter {

	private final PageHolder[] mPages;

	public PagesAdapter(PageHolder... pages) {
		mPages = pages;
	}

	@Override
	public int getCount() {
		return mPages.length;
	}

	@NonNull
	@Override
	public Object instantiateItem(@NonNull ViewGroup container, int position) {
		View view = mPages[position].getView();
		container.addView(view);
		return view;
	}

	@Override
	public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
		container.removeView((View) object);
	}

	@Override
	public CharSequence getPageTitle(int position) {
		return mPages[position].getTitle();
	}

	@Override
	public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
		return view == object;
	}
}
