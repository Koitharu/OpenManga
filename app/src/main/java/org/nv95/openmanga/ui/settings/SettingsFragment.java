package org.nv95.openmanga.ui.settings;

import android.app.Activity;
import android.app.LoaderManager;
import android.content.Intent;
import android.content.Loader;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import org.nv95.openmanga.R;
import org.nv95.openmanga.ui.AppBaseFragment;
import org.nv95.openmanga.ui.common.Dismissible;
import org.nv95.openmanga.ui.settings.providers.ProvidersSettingsActivity;

import java.util.ArrayList;

/**
 * Created by koitharu on 12.01.18.
 */

public final class SettingsFragment extends AppBaseFragment implements AdapterView.OnItemClickListener,
		LoaderManager.LoaderCallbacks<ArrayList<SettingsHeader>> {

	private RecyclerView mRecyclerView;
	private ArrayList<SettingsHeader> mHeaders;
	private SettingsAdapter mAdapter;

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
		return super.onCreateView(inflater, container, R.layout.recyclerview);
	}

	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		mRecyclerView = view.findViewById(R.id.recyclerView);
		mRecyclerView.setLayoutManager(new LinearLayoutManager(view.getContext()));
		mRecyclerView.addItemDecoration(new SettingsDecoration(view.getContext()));
		mRecyclerView.setHasFixedSize(true);
		new ItemTouchHelper(new DismissCallback()).attachToRecyclerView(mRecyclerView);
	}

	@Override
	public void onActivityCreated(@Nullable Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		final Activity activity = getActivity();
		mHeaders = new ArrayList<>();
		mHeaders.add(new SettingsHeader(activity, 0, R.string.general, R.drawable.ic_home_white));
		mHeaders.add(new SettingsHeader(activity, 1, R.string.appearance, R.drawable.ic_appearance_white));
		mHeaders.add(new SettingsHeader(activity, 2, R.string.manga_catalogues, R.drawable.ic_network_white));
		mHeaders.add(new SettingsHeader(activity, 3, R.string.downloads, R.drawable.ic_download_white));
		mHeaders.add(new SettingsHeader(activity, 4, R.string.action_reading_options, R.drawable.ic_read_white));
		mHeaders.add(new SettingsHeader(activity, 5, R.string.checking_new_chapters, R.drawable.ic_notify_new_white));
		mHeaders.add(new SettingsHeader(activity, 6, R.string.sync, R.drawable.ic_cloud_sync_white));
		mHeaders.add(new SettingsHeader(activity, 7, R.string.additional, R.drawable.ic_braces_white));
		mHeaders.add(new SettingsHeader(activity, 8, R.string.help, R.drawable.ic_help_white));

		mAdapter = new SettingsAdapter(mHeaders, this);
		mRecyclerView.setAdapter(mAdapter);

		getLoaderManager().initLoader(0, null, this).forceLoad();
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		SettingsHeader header = mHeaders.get(position);
		switch (header.id) {
			case 2:
				startActivity(new Intent(view.getContext(), ProvidersSettingsActivity.class));
				break;
		}
	}

	@Override
	public Loader<ArrayList<SettingsHeader>> onCreateLoader(int id, Bundle args) {
		return new TipsLoader(getActivity());
	}

	@Override
	public void onLoadFinished(Loader<ArrayList<SettingsHeader>> loader, ArrayList<SettingsHeader> data) {
		mHeaders.addAll(0, data);
		mAdapter.notifyDataSetChanged();
	}

	@Override
	public void onLoaderReset(Loader<ArrayList<SettingsHeader>> loader) {

	}

	@Override
	public void scrollToTop() {
		mRecyclerView.smoothScrollToPosition(0);
	}

	private class DismissCallback extends ItemTouchHelper.SimpleCallback {

		DismissCallback() {
			super(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT);
		}

		@Override
		public int getSwipeDirs(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
			return viewHolder instanceof SettingsAdapter.TipHolder ? super.getSwipeDirs(recyclerView, viewHolder) : 0;
		}

		@Override
		public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
			return false;
		}

		@Override
		public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
			if (viewHolder instanceof Dismissible) {
				((Dismissible) viewHolder).dismiss();
			}
		}
	}
}
