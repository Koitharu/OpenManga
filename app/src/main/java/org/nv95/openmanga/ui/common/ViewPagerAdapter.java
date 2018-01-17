package org.nv95.openmanga.ui.common;

import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.util.Pair;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

/**
 * Created by koitharu on 26.12.17.
 */

public final class ViewPagerAdapter extends PagerAdapter {

	private final View[] mViews;

	public ViewPagerAdapter(View... pages) {
		mViews = pages;
	}

	@Override
	public int getCount() {
		return mViews.length;
	}

	@NonNull
	@Override
	public Object instantiateItem(@NonNull ViewGroup container, int position) {
		View view = mViews[position];
		container.addView(view);
		return view;
	}

	@Override
	public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
		container.removeView((View) object);
	}

	@Override
	public CharSequence getPageTitle(int position) {
		return mViews[position].getTag().toString();
	}

	@Override
	public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
		return view == object;
	}
}
