package org.nv95.openmanga.ui;

import android.app.Fragment;
import android.app.SearchManager;
import android.app.SearchableInfo;
import android.content.ComponentName;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.widget.SearchView;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import org.nv95.openmanga.R;
import org.nv95.openmanga.ui.discover.DiscoverFragment;
import org.nv95.openmanga.ui.search.SearchActivity;
import org.nv95.openmanga.ui.shelf.ShelfFragment;

/**
 * Created by koitharu on 21.12.17.
 */

public final class MainActivity extends AppBaseActivity implements BottomNavigationView.OnNavigationItemSelectedListener,
		BottomNavigationView.OnNavigationItemReselectedListener, View.OnClickListener {

	private BottomNavigationView mBottomNavigationView;
	private SearchView mSearchView;
	private ImageView mImageViewMenu;
	private View mContent;

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		setSupportActionBar(R.id.toolbar);

		mBottomNavigationView = findViewById(R.id.bottomNavView);
		mImageViewMenu = findViewById(R.id.imageViewMenu);
		mSearchView = findViewById(R.id.searchView);
		mContent = findViewById(R.id.content);

		mBottomNavigationView.setOnNavigationItemSelectedListener(this);
		mBottomNavigationView.setOnNavigationItemReselectedListener(this);
		mImageViewMenu.setOnClickListener(this);

		final SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
		final SearchableInfo searchableInfo;
		if (searchManager != null) {
			searchableInfo = searchManager.getSearchableInfo(new ComponentName(this, SearchActivity.class));
			mSearchView.setSearchableInfo(searchableInfo);
		}

		getFragmentManager().beginTransaction()
				.replace(R.id.content, new ShelfFragment())
				.commitAllowingStateLoss();
	}

	@Override
	protected void onResume() {
		super.onResume();
		mContent.requestFocus();
	}

	@Override
	public boolean onNavigationItemSelected(@NonNull MenuItem item) {
		Fragment fragment;
		switch (item.getItemId()) {
			case R.id.section_shelf:
				fragment = new ShelfFragment();
				break;
			case R.id.section_discover:
				fragment = new DiscoverFragment();
				break;
			default:
				return false;
		}
		getFragmentManager().beginTransaction()
				.replace(R.id.content, fragment)
				.commit();
		return true;
	}


	@Override
	public void onNavigationItemReselected(@NonNull MenuItem item) {
		Fragment fragment = getFragmentManager().findFragmentById(R.id.content);
		if (fragment != null && fragment instanceof AppBaseFragment) {
			((AppBaseFragment) fragment).scrollToTop();
		}
	}

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
			case R.id.imageViewMenu:
				//TODO
				break;
		}
	}
}
