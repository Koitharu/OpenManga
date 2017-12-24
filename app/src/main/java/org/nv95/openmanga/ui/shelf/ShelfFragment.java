package org.nv95.openmanga.ui.shelf;

import android.app.LoaderManager;
import android.content.Loader;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.nv95.openmanga.R;
import org.nv95.openmanga.content.shelf.ShelfContent;
import org.nv95.openmanga.loaders.ShelfLoader;
import org.nv95.openmanga.ui.AppBaseFragment;
import org.nv95.openmanga.utils.ResourceUtils;

/**
 * Created by koitharu on 21.12.17.
 */

public final class ShelfFragment extends AppBaseFragment implements LoaderManager.LoaderCallbacks<ShelfContent> {

	private RecyclerView mRecyclerView;
	private ShelfAdapter mAdapter;

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
		return super.onCreateView(inflater, container, R.layout.fragment_shelf);
	}

	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		mRecyclerView = view.findViewById(R.id.recyclerView);
		mRecyclerView.setHasFixedSize(true);
	}

	@Override
	public void onActivityCreated(@Nullable Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		mAdapter = new ShelfAdapter();
		GridLayoutManager layoutManager = new GridLayoutManager(getActivity(), 6);
		layoutManager.setSpanSizeLookup(new ShelfSpanSizeLookup(mAdapter, 6));
		mRecyclerView.addItemDecoration(new ShelfItemSpaceDecoration(ResourceUtils.dpToPx(getResources(), 4), mAdapter, 6));
		mRecyclerView.setLayoutManager(layoutManager);
		mRecyclerView.setAdapter(mAdapter);
		getLoaderManager().initLoader(0, null, this);
		getLoaderManager().getLoader(0).forceLoad(); //TODO
	}

	@Override
	public Loader<ShelfContent> onCreateLoader(int i, Bundle bundle) {
		return new ShelfLoader(getActivity());
	}

	@Override
	public void onLoadFinished(Loader<ShelfContent> loader, ShelfContent content) {
		ShelfUpdater.update(mAdapter, content);
	}

	@Override
	public void onLoaderReset(Loader<ShelfContent> loader) {

	}

	@Override
	public void scrollToTop() {
		mRecyclerView.smoothScrollToPosition(0);
	}
}
