package org.nv95.openmanga.recommendations;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.res.Resources;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.nv95.openmanga.common.NativeFragmentPagerAdapter;
import org.nv95.openmanga.core.storage.db.RecommendationsRepository;
import org.nv95.openmanga.core.storage.db.RecommendationsSpecifications;

import java.util.ArrayList;

/**
 * Created by koitharu on 29.01.18.
 */

class RecommendationsPagerAdapter extends NativeFragmentPagerAdapter {

	private final RecommendationsRepository mRepository;
	private final ArrayList<Integer> mDataset;
	private final Resources mResources;

	RecommendationsPagerAdapter(FragmentManager fm, Context context) {
		super(fm);
		mRepository = RecommendationsRepository.get(context);
		mResources = context.getResources();
		mDataset = mRepository.getCategories();
	}

	@NonNull
	@Override
	public Fragment getItem(int position) {
		final RecommendationsFragment fragment = new RecommendationsFragment();
		fragment.setArguments(new RecommendationsSpecifications()
				.category(mDataset.get(position))
				//.orderByRand()
				.toBundle());
		return fragment;
	}

	@Override
	public void notifyDataSetChanged() {
		mDataset.clear();
		mDataset.addAll(mRepository.getCategories());
		super.notifyDataSetChanged();
	}

	@Override
	public int getCount() {
		return mDataset.size();
	}

	@Nullable
	@Override
	public CharSequence getPageTitle(int position) {
		return mResources.getString(mDataset.get(position));
	}
}
