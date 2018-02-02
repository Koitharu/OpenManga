package org.nv95.openmanga.tools;

import android.app.Activity;
import android.app.LoaderManager;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.NestedScrollView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.nv95.openmanga.AppBaseFragment;
import org.nv95.openmanga.R;
import org.nv95.openmanga.common.utils.TextUtils;
import org.nv95.openmanga.tools.settings.SettingsHeadersActivity;

/**
 * Created by koitharu on 02.02.18.
 */

public final class ToolsFragment extends AppBaseFragment implements View.OnClickListener, LoaderManager.LoaderCallbacks<StorageStats> {

	private static final int LOADER_STORAGE_STATS = 0;

	private NestedScrollView mScrollView;
	private TextView mTextViewStorageTotal;
	private TextView mTextViewStorageCache;

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
		return super.onCreateView(inflater, container, R.layout.fragment_tools);
	}

	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		mScrollView = view.findViewById(R.id.scrollView);
		mTextViewStorageTotal = view.findViewById(R.id.textView_storage_total);
		mTextViewStorageCache = view.findViewById(R.id.textView_storage_cache);

		view.findViewById(R.id.action_settings).setOnClickListener(this);
	}

	@Override
	public void onActivityCreated(@Nullable Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		final Activity activity = getActivity();
		getLoaderManager().initLoader(LOADER_STORAGE_STATS, null, this).forceLoad();
	}

	@Override
	public void scrollToTop() {
		mScrollView.smoothScrollTo(0, 0);
	}

	@Override
	public void onClick(View v) {
		final Context context = v.getContext();
		switch (v.getId()) {
			case R.id.action_settings:
				startActivity(new Intent(context, SettingsHeadersActivity.class));
				break;
		}
	}

	@Override
	public Loader<StorageStats> onCreateLoader(int id, Bundle args) {
		return new StorageStatsLoader(getActivity());
	}

	@Override
	public void onLoadFinished(Loader<StorageStats> loader, StorageStats data) {
		mTextViewStorageTotal.setText(TextUtils.formatFileSize(data.total()));
		mTextViewStorageCache.setText(TextUtils.formatFileSize(data.cacheSize));
	}

	@Override
	public void onLoaderReset(Loader<StorageStats> loader) {

	}
}
