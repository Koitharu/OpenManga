package org.nv95.openmanga.mangalist.favourites;

import android.app.Fragment;
import android.app.FragmentManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.nv95.openmanga.common.NativeFragmentPagerAdapter;
import org.nv95.openmanga.core.models.Category;
import org.nv95.openmanga.core.storage.db.CategoriesRepository;
import org.nv95.openmanga.core.storage.db.CategoriesSpecification;
import org.nv95.openmanga.core.storage.db.FavouritesSpecification;

import java.util.ArrayList;

/**
 * Created by koitharu on 18.01.18.
 */

final class CategoriesPagerAdapter extends NativeFragmentPagerAdapter {

	@NonNull
	private final CategoriesRepository mRepository;
	@NonNull
	private final CategoriesSpecification mSpecification;
	@NonNull
	private final ArrayList<Category> mDataset;

	public CategoriesPagerAdapter(@NonNull FragmentManager fm, @NonNull CategoriesRepository repository) {
		super(fm);
		mSpecification = new CategoriesSpecification()
				.orderByDate(false);
		mRepository = repository;
		mDataset = repository.query(mSpecification);
	}

	@NonNull
	public CategoriesSpecification getSpecification() {
		return mSpecification;
	}

	@NonNull
	@Override
	public Fragment getItem(int position) {
		final FavouritesFragment fragment = new FavouritesFragment();
		fragment.setArguments(new FavouritesSpecification()
				.category(mDataset.get(position).id)
				.orderByDate(true)
				.toBundle());
		return fragment;
	}

	@Override
	public void notifyDataSetChanged() {
		mDataset.clear();
		mDataset.addAll(mRepository.query(mSpecification));
		super.notifyDataSetChanged();
	}

	@Nullable
	@Override
	public CharSequence getPageTitle(int position) {
		return mDataset.get(position).name;
	}

	@Override
	public int getCount() {
		return mDataset.size();
	}

	int indexById(int id) {
		for (int i = 0; i < mDataset.size(); i++) {
			if (mDataset.get(i).id == id) {
				return i;
			}
		}
		return -1;
	}

	public ArrayList<Category> getData() {
		return mDataset;
	}
}
