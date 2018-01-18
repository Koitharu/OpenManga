package org.nv95.openmanga.common;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

/**
 * Created by koitharu on 18.01.18.
 */

public abstract class NativeFragmentPagerAdapter<T extends Fragment> extends PagerAdapter {

	private final FragmentManager mFragmentManager;
	private FragmentTransaction mCurTransaction = null;

	private ArrayList<Fragment.SavedState> mSavedState = new ArrayList<Fragment.SavedState>();
	private ArrayList<Fragment> mFragments = new ArrayList<Fragment>();
	private Fragment mCurrentPrimaryItem = null;

	public NativeFragmentPagerAdapter(FragmentManager fragmentManager) {
		mFragmentManager = fragmentManager;
	}

	public abstract T getItem(int position);

	@Override
	public final void startUpdate(@NonNull ViewGroup container) {
		if (container.getId() == View.NO_ID) {
			throw new IllegalStateException("ViewPager with adapter " + this
					+ " requires a view id");
		}
	}

	@NonNull
	@Override
	public final Object instantiateItem(@NonNull ViewGroup container, int position) {
		if (mFragments.size() > position) {
			Fragment f = mFragments.get(position);
			if (f != null) {
				return f;
			}
		}

		if (mCurTransaction == null) {
			mCurTransaction = mFragmentManager.beginTransaction();
		}

		T fragment = getItem(position);
		if (mSavedState.size() > position) {
			Fragment.SavedState fss = mSavedState.get(position);
			if (fss != null) {
				fragment.setInitialSavedState(fss);
			}
		}
		while (mFragments.size() <= position) {
			mFragments.add(null);
		}
		fragment.setMenuVisibility(false);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
			fragment.setUserVisibleHint(false);
		}
		mFragments.set(position, fragment);
		mCurTransaction.add(container.getId(), fragment);

		return fragment;
	}

	@Override
	public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
		Fragment fragment = (Fragment) object;

		if (mCurTransaction == null) {
			mCurTransaction = mFragmentManager.beginTransaction();
		}
		while (mSavedState.size() <= position) {
			mSavedState.add(null);
		}
		mSavedState.set(position, fragment.isAdded()
				? mFragmentManager.saveFragmentInstanceState(fragment) : null);
		mFragments.set(position, null);

		mCurTransaction.remove(fragment);
	}

	@Override
	@SuppressWarnings("ReferenceEquality")
	public void setPrimaryItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
		Fragment fragment = (Fragment) object;
		if (fragment != mCurrentPrimaryItem) {
			if (mCurrentPrimaryItem != null) {
				mCurrentPrimaryItem.setMenuVisibility(false);
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
					mCurrentPrimaryItem.setUserVisibleHint(false);
				}
			}
			fragment.setMenuVisibility(true);
			fragment.setUserVisibleHint(true);
			mCurrentPrimaryItem = fragment;
		}
	}

	@Override
	public void finishUpdate(@NonNull ViewGroup container) {
		if (mCurTransaction != null) {
			mCurTransaction.commitAllowingStateLoss();
			mCurTransaction = null;
		}
	}

	@Override
	public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
		return ((Fragment) object).getView() == view;
	}

	@Override
	public Parcelable saveState() {
		Bundle state = null;
		if (mSavedState.size() > 0) {
			state = new Bundle();
			Fragment.SavedState[] fss = new Fragment.SavedState[mSavedState.size()];
			mSavedState.toArray(fss);
			state.putParcelableArray("states", fss);
		}
		for (int i = 0; i < mFragments.size(); i++) {
			Fragment f = mFragments.get(i);
			if (f != null && f.isAdded()) {
				if (state == null) {
					state = new Bundle();
				}
				String key = "f" + i;
				mFragmentManager.putFragment(state, key, f);
			}
		}
		return state;
	}

	@Override
	public void restoreState(Parcelable state, ClassLoader loader) {
		if (state != null) {
			Bundle bundle = (Bundle) state;
			bundle.setClassLoader(loader);
			Parcelable[] fss = bundle.getParcelableArray("states");
			mSavedState.clear();
			mFragments.clear();
			if (fss != null) {
				for (Parcelable fs : fss) {
					mSavedState.add((Fragment.SavedState) fs);
				}
			}
			Iterable<String> keys = bundle.keySet();
			for (String key : keys) {
				if (key.startsWith("f")) {
					int index = Integer.parseInt(key.substring(1));
					Fragment f = mFragmentManager.getFragment(bundle, key);
					if (f != null) {
						while (mFragments.size() <= index) {
							mFragments.add(null);
						}
						f.setMenuVisibility(false);
						mFragments.set(index, f);
					}
				}
			}
		}
	}
}
