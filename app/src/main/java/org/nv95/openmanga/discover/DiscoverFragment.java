package org.nv95.openmanga.discover;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.nv95.openmanga.R;
import org.nv95.openmanga.core.storage.ProvidersStore;
import org.nv95.openmanga.AppBaseFragment;
import org.nv95.openmanga.common.views.recyclerview.HeaderDividerItemDecoration;

import java.util.ArrayList;

/**
 * Created by koitharu on 26.12.17.
 */

public final class DiscoverFragment extends AppBaseFragment {

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
		dataset.add(new ProviderHeaderDetailed("browse/import" /* TODO */, getString(R.string.browse_filesystem),
				getString(R.string.import_cbz_summary), ContextCompat.getDrawable(getActivity(), R.drawable.ic_folder_white)));
		dataset.add(new ProviderHeaderDetailed("browse/saved" /* TODO */, getString(R.string.saved_manga),
				getString(R.string.saved_manga_summary), ContextCompat.getDrawable(getActivity(), R.drawable.ic_sdcard_white)));
		dataset.add(new ProviderHeaderDetailed("browse/recommendations" /* TODO */, getString(R.string.recommendations),
				getString(R.string.recommendations_summary), ContextCompat.getDrawable(getActivity(), R.drawable.ic_lightbulb_white)));
		dataset.add(new ProviderHeaderDetailed("browse/bookmarks" /* TODO */, getString(R.string.bookmarks),
				getString(R.string.bookmarks_summary), ContextCompat.getDrawable(getActivity(), R.drawable.ic_bookmark_white)));
		dataset.add(getString(R.string.storages_remote));
		dataset.addAll(new ProvidersStore(getActivity()).getUserProviders());
		final DiscoverAdapter adapter = new DiscoverAdapter(dataset);
		mRecyclerView.setAdapter(adapter);
	}
}
