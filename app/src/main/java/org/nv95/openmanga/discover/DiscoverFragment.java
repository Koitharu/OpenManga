package org.nv95.openmanga.discover;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import org.nv95.openmanga.AppBaseFragment;
import org.nv95.openmanga.R;
import org.nv95.openmanga.common.views.recyclerview.HeaderDividerItemDecoration;
import org.nv95.openmanga.core.storage.ProvidersStore;
import org.nv95.openmanga.tools.settings.providers.ProvidersSettingsActivity;

import java.util.ArrayList;

/**
 * Created by koitharu on 26.12.17.
 */

public final class DiscoverFragment extends AppBaseFragment {

	private static final int REQUEST_PROVIDERS_CONFIG = 12;

	private RecyclerView mRecyclerView;

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
		return super.onCreateView(inflater, container, R.layout.recyclerview);
	}

	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		mRecyclerView = view.findViewById(R.id.recyclerView);
		mRecyclerView.setHasFixedSize(true);
		mRecyclerView.setLayoutManager(new LinearLayoutManager(view.getContext()));
		mRecyclerView.addItemDecoration(new HeaderDividerItemDecoration(view.getContext()));
	}

	@Override
	public void onActivityCreated(@Nullable Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		final ArrayList<Object> dataset = new ArrayList<>();
		dataset.add(new ProviderHeaderDetailed(SpecificCName.BROWSE_IMPORT, getString(R.string.browse_filesystem),
				getString(R.string.import_cbz_summary), ContextCompat.getDrawable(getActivity(), R.drawable.ic_folder_white)));
		dataset.add(new ProviderHeaderDetailed(SpecificCName.BROWSE_SAVED, getString(R.string.saved_manga),
				getString(R.string.saved_manga_summary), ContextCompat.getDrawable(getActivity(), R.drawable.ic_sdcard_white)));
		dataset.add(new ProviderHeaderDetailed(SpecificCName.BROWSE_RECOMMENDATIONS, getString(R.string.recommendations),
				getString(R.string.recommendations_summary), ContextCompat.getDrawable(getActivity(), R.drawable.ic_lightbulb_white)));
		dataset.add(new ProviderHeaderDetailed(SpecificCName.BROWSE_BOOKMARKS, getString(R.string.bookmarks),
				getString(R.string.bookmarks_summary), ContextCompat.getDrawable(getActivity(), R.drawable.ic_bookmark_white)));
		dataset.add(getString(R.string.storage_remote));
		dataset.addAll(new ProvidersStore(getActivity()).getUserProviders());
		final DiscoverAdapter adapter = new DiscoverAdapter(dataset);
		mRecyclerView.setAdapter(adapter);
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.options_discover, menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.action_configure_providers:
				startActivityForResult(new Intent(getActivity(), ProvidersSettingsActivity.class), REQUEST_PROVIDERS_CONFIG);
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == REQUEST_PROVIDERS_CONFIG) {
			onActivityCreated(null);
		}
	}

	@Override
	public void scrollToTop() {
		mRecyclerView.smoothScrollToPosition(0);
	}
}
