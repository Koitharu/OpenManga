package org.nv95.openmanga.mangalist.favourites;

import android.app.Activity;
import android.app.LoaderManager;
import android.content.Loader;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.nv95.openmanga.AppBaseFragment;
import org.nv95.openmanga.R;
import org.nv95.openmanga.common.utils.AnimationUtils;
import org.nv95.openmanga.common.utils.ErrorUtils;
import org.nv95.openmanga.core.ListWrapper;
import org.nv95.openmanga.core.models.MangaFavourite;
import org.nv95.openmanga.core.storage.db.FavouritesRepository;
import org.nv95.openmanga.core.storage.db.FavouritesSpecification;

import java.util.ArrayList;

/**
 * Created by koitharu on 18.01.18.
 */

public final class FavouritesFragment extends AppBaseFragment implements LoaderManager.LoaderCallbacks<ListWrapper<MangaFavourite>> {

	private RecyclerView mRecyclerView;
	private ProgressBar mProgressBar;
	private TextView mTextViewHolder;

	private FavouritesSpecification mSpecifications;
	private FavouritesAdapter mAdapter;
	private ArrayList<MangaFavourite> mDataset;

	private FavouritesRepository mFavouritesRepository;

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mDataset = new ArrayList<>();
		mSpecifications = FavouritesSpecification.from(getArguments());
	}

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_favourites, container,false);
	}

	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		mProgressBar = view.findViewById(R.id.progressBar);
		mRecyclerView = view.findViewById(R.id.recyclerView);
		mTextViewHolder = view.findViewById(R.id.textView_holder);

		mRecyclerView.setHasFixedSize(true);
	}

	@Override
	public void onActivityCreated(@Nullable Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		final Activity activity = getActivity();
		mAdapter = new FavouritesAdapter(mDataset);
		mRecyclerView.addItemDecoration(new DividerItemDecoration(activity, LinearLayoutManager.VERTICAL));
		mRecyclerView.setAdapter(mAdapter);
		mFavouritesRepository = FavouritesRepository.get(activity);
		getLoaderManager().initLoader((int) mSpecifications.getId(), mSpecifications.toBundle(), this).forceLoad();
	}

	@Override
	public Loader<ListWrapper<MangaFavourite>> onCreateLoader(int id, Bundle args) {
		return new FavouritesLoader(getActivity(), FavouritesSpecification.from(args));
	}

	@Override
	public void onLoadFinished(Loader<ListWrapper<MangaFavourite>> loader, ListWrapper<MangaFavourite> result) {
		mProgressBar.setVisibility(View.GONE);
		if (result.isSuccess()) {
			final ArrayList<MangaFavourite> list = result.get();
			mDataset.clear();
			mDataset.addAll(list);
			mAdapter.notifyDataSetChanged();
			AnimationUtils.setVisibility(mTextViewHolder, mDataset.isEmpty() ? View.VISIBLE : View.GONE);
		} else {
			Snackbar.make(mRecyclerView, ErrorUtils.getErrorMessage(result.getError()), Toast.LENGTH_SHORT).show();
		}
	}

	@Override
	public void onLoaderReset(Loader<ListWrapper<MangaFavourite>> loader) {

	}
}
