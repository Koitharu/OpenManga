package org.nv95.openmanga.recommendations;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;

import org.nv95.openmanga.AppBaseActivity;
import org.nv95.openmanga.R;

/**
 * Created by koitharu on 29.01.18.
 */

public final class RecommendationsActivity extends AppBaseActivity {

	private ViewPager mPager;
	private RecommendationsPagerAdapter mPagerAdapter;
	private BroadcastReceiver mBroadcastReceiver;
	@Nullable
	private Snackbar mSnackBar;

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_recommendations);
		setSupportActionBar(R.id.toolbar);
		enableHomeAsUp();

		mPager = findViewById(R.id.pager);
		TabLayout mTabs = findViewById(R.id.tabs);

		mPagerAdapter = new RecommendationsPagerAdapter(getFragmentManager(), this);
		mPager.setAdapter(mPagerAdapter);
		mTabs.setupWithViewPager(mPager);
		mBroadcastReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				onUpdated();
			}
		};
		final IntentFilter intentFilter = new IntentFilter(RecommendationsUpdateService.ACTION_RECOMMENDATIONS_UPDATED);
		registerReceiver(mBroadcastReceiver, intentFilter);
		if (mPagerAdapter.getCount() == 0) {
			updateContent();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.options_recommendations, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.action_refresh:
				updateContent();
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	private void updateContent() {
		if (mSnackBar != null) {
			mSnackBar.dismiss();
		}
		mSnackBar = Snackbar.make(mPager, R.string.updating_recommendations, Snackbar.LENGTH_INDEFINITE);
		mSnackBar.addCallback(new Snackbar.Callback() {
			@Override
			public void onDismissed(Snackbar transientBottomBar, int event) {
				mSnackBar = null;
				super.onDismissed(transientBottomBar, event);
			}
		});
		mSnackBar.show();
		startService(new Intent(this, RecommendationsUpdateService.class));
	}

	@Override
	protected void onDestroy() {
		unregisterReceiver(mBroadcastReceiver);
		super.onDestroy();
	}

	private void onUpdated() {
		mPagerAdapter.notifyDataSetChanged();
		if (mSnackBar != null) {
			mSnackBar.dismiss();
		}
	}
}
