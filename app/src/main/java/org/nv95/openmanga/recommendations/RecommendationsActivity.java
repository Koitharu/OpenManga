package org.nv95.openmanga.recommendations;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.SwipeRefreshLayout;

import org.nv95.openmanga.AppBaseActivity;
import org.nv95.openmanga.R;

/**
 * Created by koitharu on 29.01.18.
 */

public final class RecommendationsActivity extends AppBaseActivity implements SwipeRefreshLayout.OnRefreshListener {

	private ViewPager mPager;
	private TabLayout mTabs;
	private SwipeRefreshLayout mSwipeRefreshLayout;
	private RecommendationsPagerAdapter mPagerAdapter;
	private BroadcastReceiver mBroadcastReceiver;

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_recommendations);
		setSupportActionBar(R.id.toolbar);
		enableHomeAsUp();

		mPager = findViewById(R.id.pager);
		mTabs = findViewById(R.id.tabs);
		mSwipeRefreshLayout = findViewById(R.id.swypeRefreshLayout);

		mPagerAdapter = new RecommendationsPagerAdapter(getFragmentManager(), this);
		mPager.setAdapter(mPagerAdapter);
		mTabs.setupWithViewPager(mPager);
		mSwipeRefreshLayout.setOnRefreshListener(this);
		mBroadcastReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				onUpdated();
			}
		};
		final IntentFilter intentFilter = new IntentFilter(RecommendationsUpdateService.ACTION_RECOMMENDATIONS_UPDATED);
		registerReceiver(mBroadcastReceiver, intentFilter);
	}

	@Override
	public void onRefresh() {
		startService(new Intent(this, RecommendationsUpdateService.class));
	}

	@Override
	protected void onDestroy() {
		unregisterReceiver(mBroadcastReceiver);
		super.onDestroy();
	}

	private void onUpdated() {
		mPagerAdapter.notifyDataSetChanged();
		mSwipeRefreshLayout.setRefreshing(false);
	}
}
