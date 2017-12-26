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

public final class SimpleViewPagerAdapter extends PagerAdapter {

	private final ArrayList<Pair<View,String>> mViews;

	public SimpleViewPagerAdapter() {
		mViews = new ArrayList<>();
	}

	public void addView(View view, String title) {
		mViews.add(new Pair<>(view, title));
	}

	@Override
	public int getCount() {
		return mViews.size();
	}

	@NonNull
	@Override
	public Object instantiateItem(@NonNull ViewGroup container, int position) {
		View view = mViews.get(position).first;
		container.addView(view);
		return view;
	}

	@Override
	public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
		container.removeView((View) object);
	}

	@Override
	public CharSequence getPageTitle(int position) {
		return mViews.get(position).second;
	}

	@Override
	public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
		return view == object;
	}
}
