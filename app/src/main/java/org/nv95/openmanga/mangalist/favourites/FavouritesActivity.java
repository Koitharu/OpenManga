package org.nv95.openmanga.mangalist.favourites;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;

import org.nv95.openmanga.AppBaseActivity;
import org.nv95.openmanga.R;
import org.nv95.openmanga.common.utils.CollectionsUtils;
import org.nv95.openmanga.core.storage.db.CategoriesRepository;

/**
 * Created by koitharu on 18.01.18.
 */

public final class FavouritesActivity extends AppBaseActivity {

	private static final int REQUEST_CATEGORIES = 14;

	private ViewPager mPager;
	private TabLayout mTabs;
	private CategoriesPagerAdapter mPagerAdapter;

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_favourites);
		setSupportActionBar(R.id.toolbar);
		enableHomeAsUp();

		mPager = findViewById(R.id.pager);
		mTabs = findViewById(R.id.tabs);

		mPagerAdapter = new CategoriesPagerAdapter(getFragmentManager(), CategoriesRepository.get(this));
		mPager.setAdapter(mPagerAdapter);
		mTabs.setupWithViewPager(mPager);

		final int category_id = getIntent().getIntExtra("category_id", 0);
		if (category_id != 0) {
			int index = mPagerAdapter.indexById(category_id);
			if (index != -1) {
				mPager.setCurrentItem(index, false);
			}
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.options_favourites, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.action_manage_categories:
				startActivityForResult(new Intent(this, CategoriesActivity.class), REQUEST_CATEGORIES);
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == REQUEST_CATEGORIES && resultCode == RESULT_OK) {
			int pos = mPager.getCurrentItem();
			int id = mPagerAdapter.getData().get(pos).id;
			mPagerAdapter.notifyDataSetChanged();
			int newPos = CollectionsUtils.findCategoryPositionById(mPagerAdapter.getData(), id);
			if (newPos == -1) { //removed current page
				if (pos >= mPagerAdapter.getCount()) { //it was latest
					mPager.setCurrentItem(mPagerAdapter.getCount() - 1); //switch to new latest
				}
			} else {
				mPager.setCurrentItem(newPos, false);
			}
		}
	}
}
