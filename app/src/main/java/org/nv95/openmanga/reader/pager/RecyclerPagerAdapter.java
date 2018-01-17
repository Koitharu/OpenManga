package org.nv95.openmanga.reader.pager;

import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;

import java.util.Stack;

/**
 * Created by koitharu on 11.01.18.
 */

@SuppressWarnings("unchecked")
abstract class RecyclerPagerAdapter<T extends View> extends PagerAdapter {

	private final Stack<T> mViewPool;

	public RecyclerPagerAdapter() {
		mViewPool = new Stack<>();
	}

	@NonNull
	@Override
	public final Object instantiateItem(@NonNull ViewGroup container, int position) {
		final T view = mViewPool.isEmpty() ? onCreateView(container) : mViewPool.pop();
		onBindView(view, position);
		container.addView(view);
		return view;
	}

	@Override
	public final void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
		container.removeView((View) object);
		onRecyclerView((T) object);
		mViewPool.push((T) object);
	}

	@Override
	public final boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
		return view == object;
	}

	protected abstract T onCreateView(@NonNull ViewGroup container);

	protected abstract void onBindView(@NonNull T view, int position);

	protected abstract void onRecyclerView(@NonNull T view);
}
