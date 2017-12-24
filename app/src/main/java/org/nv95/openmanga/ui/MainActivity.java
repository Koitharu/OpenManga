package org.nv95.openmanga.ui;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.view.MenuItem;

import org.nv95.openmanga.R;
import org.nv95.openmanga.ui.shelf.ShelfFragment;

/**
 * Created by koitharu on 21.12.17.
 */

public final class MainActivity extends AppBaseActivity implements BottomNavigationView.OnNavigationItemSelectedListener,
		BottomNavigationView.OnNavigationItemReselectedListener {

	private BottomNavigationView mBottomNavigationView;

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		setSupportActionBar(R.id.toolbar);

		mBottomNavigationView = findViewById(R.id.bottomNavView);
		mBottomNavigationView.setOnNavigationItemSelectedListener(this);
		mBottomNavigationView.setOnNavigationItemReselectedListener(this);
	}

	@Override
	public boolean onNavigationItemSelected(@NonNull MenuItem item) {
		Fragment fragment;
		switch (item.getItemId()) {
			case R.id.section_shelf:
				fragment = new ShelfFragment();
				break;
			default:
				return true;
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
}
