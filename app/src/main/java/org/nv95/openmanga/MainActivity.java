package org.nv95.openmanga;

import android.app.Fragment;
import android.app.SearchManager;
import android.app.SearchableInfo;
import android.content.ComponentName;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.SearchView;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import org.nv95.openmanga.common.CrashHandler;
import org.nv95.openmanga.core.storage.FlagsStorage;
import org.nv95.openmanga.discover.DiscoverFragment;
import org.nv95.openmanga.search.SearchActivity;
import org.nv95.openmanga.settings.SettingsFragment;
import org.nv95.openmanga.shelf.OnTipsActionListener;
import org.nv95.openmanga.shelf.ShelfFragment;

/**
 * Created by koitharu on 21.12.17.
 */

public final class MainActivity extends AppBaseActivity implements BottomNavigationView.OnNavigationItemSelectedListener,
		BottomNavigationView.OnNavigationItemReselectedListener, View.OnClickListener, PopupMenu.OnMenuItemClickListener,
		OnTipsActionListener {

	private BottomNavigationView mBottomNavigationView;
	private SearchView mSearchView;
	private ImageView mImageViewMenu;
	private PopupMenu mMainMenu;
	private View mContent;
	private AppBaseFragment mFragment;

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		setSupportActionBar(R.id.toolbar);

		mBottomNavigationView = findViewById(R.id.bottomNavView);
		mImageViewMenu = findViewById(R.id.imageViewMenu);
		mSearchView = findViewById(R.id.searchView);
		mContent = findViewById(R.id.content);

		mMainMenu = new PopupMenu(this, mImageViewMenu);
		mMainMenu.setOnMenuItemClickListener(this);

		mBottomNavigationView.setOnNavigationItemSelectedListener(this);
		mBottomNavigationView.setOnNavigationItemReselectedListener(this);
		mImageViewMenu.setOnClickListener(this);

		final SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
		final SearchableInfo searchableInfo;
		if (searchManager != null) {
			searchableInfo = searchManager.getSearchableInfo(new ComponentName(this, SearchActivity.class));
			mSearchView.setSearchableInfo(searchableInfo);
		}

		mFragment = new ShelfFragment();
		initMenu();
		getFragmentManager().beginTransaction()
				.replace(R.id.content, mFragment)
				.commitAllowingStateLoss();
	}

	@Override
	protected void onResume() {
		super.onResume();
		mContent.requestFocus();
	}

	@Override
	public boolean onNavigationItemSelected(@NonNull MenuItem item) {
		mFragment.onDestroyOptionsMenu();
		switch (item.getItemId()) {
			case R.id.section_shelf:
				mFragment = new ShelfFragment();
				break;
			case R.id.section_discover:
				mFragment = new DiscoverFragment();
				break;
			case R.id.section_settings:
				mFragment = new SettingsFragment();
				break;
			default:
				return false;
		}
		initMenu();
		getFragmentManager().beginTransaction()
				.replace(R.id.content, mFragment)
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
				showMainMenu();
				break;
		}
	}

	private void initMenu() {
		mMainMenu.getMenu().clear();
		mFragment.onCreateOptionsMenu(mMainMenu.getMenu(), mMainMenu.getMenuInflater());
		mMainMenu.inflate(R.menu.options_main);
	}

	@Override
	public boolean onMenuItemClick(MenuItem item) {
		switch (item.getItemId()) {

			default:
				return mFragment.onOptionsItemSelected(item);
		}
	}

	private void onPrepareMenu(Menu menu) {

	}

	private void showMainMenu() {
		onPrepareMenu(mMainMenu.getMenu());
		mFragment.onPrepareOptionsMenu(mMainMenu.getMenu());
		mMainMenu.show();
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_MENU) {
			showMainMenu();
			return true;
		}
		return super.onKeyUp(keyCode, event);
	}

	@Override
	public void onTipActionClick(int actionId) {
		switch (actionId) {
			case R.id.action_crash_report:
				final CrashHandler crashHandler = CrashHandler.get();
				if (crashHandler != null) {
					new AlertDialog.Builder(this)
							.setTitle(crashHandler.getErrorClassName())
							.setMessage(crashHandler.getErrorMessage() + "\n\n" + crashHandler.getErrorStackTrace())
							.setNegativeButton(R.string.close, null)
							.create()
							.show();
				}
				break;
			case R.id.action_discover:
				mBottomNavigationView.setSelectedItemId(R.id.section_discover);
				break;
		}
	}

	@Override
	public void onTipDismissed(int actionId) {
		switch (actionId) {
			case R.id.action_crash_report:
				final CrashHandler crashHandler = CrashHandler.get();
				if (crashHandler != null) {
					crashHandler.clear();
				}
				break;
			case R.id.action_wizard:
				FlagsStorage.get(this).setWizardRequired(false);
				break;
		}
	}
}
