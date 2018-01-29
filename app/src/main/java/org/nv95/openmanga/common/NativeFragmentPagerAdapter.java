package org.nv95.openmanga.common;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by koitharu on 18.01.18.
 */

public abstract class NativeFragmentPagerAdapter extends PagerAdapter {

	private final FragmentManager mFragmentManager;
	private Fragment mCurrentPrimaryItem = null;

	public NativeFragmentPagerAdapter(FragmentManager fm) {
		mFragmentManager = fm;
	}

	/**
	 * Return the Fragment associated with a specified position.
	 */
	@NonNull
	public abstract Fragment getItem(int position);

	@NonNull
	@SuppressWarnings("ReferenceEquality")
	@Override
	public Object instantiateItem(@NonNull ViewGroup container, int position) {
		final FragmentTransaction transaction = mFragmentManager.beginTransaction();

		final long itemId = getItemId(position);

		// Do we already have this fragment?
		String name = makeFragmentName(container.getId(), itemId);
		Fragment fragment = mFragmentManager.findFragmentByTag(name);
		if (fragment != null) {
			transaction.attach(fragment);
		} else {
			fragment = getItem(position);
			transaction.add(container.getId(), fragment,
					makeFragmentName(container.getId(), itemId));
		}
		if (fragment != mCurrentPrimaryItem) {
			fragment.setMenuVisibility(false);
			fragment.setUserVisibleHint(false);
		}
		transaction.commitAllowingStateLoss();
		return fragment;
	}

	@Override
	public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
		mFragmentManager.beginTransaction()
				.detach((Fragment)object)
				.commitAllowingStateLoss();
	}

	@SuppressWarnings("ReferenceEquality")
	@Override
	public void setPrimaryItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
		Fragment fragment = (Fragment)object;
		if (fragment != mCurrentPrimaryItem) {
			if (mCurrentPrimaryItem != null) {
				mCurrentPrimaryItem.setMenuVisibility(false);
				mCurrentPrimaryItem.setUserVisibleHint(false);
			}
			fragment.setMenuVisibility(true);
			fragment.setUserVisibleHint(true);
			mCurrentPrimaryItem = fragment;
		}
	}

	@Override
	public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
		return ((Fragment)object).getView() == view;
	}

	@Override
	public Parcelable saveState() {
		return null;
	}

	@Override
	public void restoreState(Parcelable state, ClassLoader loader) {
	}

	@Override
	public int getItemPosition(@NonNull Object object) {
		return POSITION_NONE;
	}

	/**
	 * Return a unique identifier for the item at the given position.
	 *
	 * <p>The default implementation returns the given position.
	 * Subclasses should override this method if the positions of items can change.</p>
	 *
	 * @param position Position within this adapter
	 * @return Unique identifier for the item at position
	 */
	public long getItemId(int position) {
		return position;
	}

	private static String makeFragmentName(int viewId, long id) {
		return "android:switcher:" + viewId + ":" + id;
	}
}
