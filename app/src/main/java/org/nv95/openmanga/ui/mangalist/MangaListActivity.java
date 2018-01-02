package org.nv95.openmanga.ui.mangalist;

import android.app.LoaderManager;
import android.content.Loader;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import org.nv95.openmanga.R;
import org.nv95.openmanga.content.MangaGenre;
import org.nv95.openmanga.content.MangaHeader;
import org.nv95.openmanga.content.MangaQueryArguments;
import org.nv95.openmanga.content.providers.MangaProvider;
import org.nv95.openmanga.loaders.MangaListLoader;
import org.nv95.openmanga.ui.AppBaseActivity;
import org.nv95.openmanga.utils.CollectionsUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

/**
 * Created by koitharu on 28.12.17.
 */

public final class MangaListActivity extends AppBaseActivity implements LoaderManager.LoaderCallbacks<ArrayList<MangaHeader>>,
		View.OnClickListener, FilterCallback {

	private RecyclerView mRecyclerView;
	private ProgressBar mProgressBar;
	private FloatingActionButton mFabFilter;

	private MangaListAdapter mAdapter;
	private ArrayList<MangaHeader> mDataset;
	private MangaProvider mProvider;
	private MangaQueryArguments mArguments;

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_mangalist);
		setSupportActionBar(R.id.toolbar);
		enableHomeAsUp();

		mProgressBar = findViewById(R.id.progressBar);
		mRecyclerView = findViewById(R.id.recyclerView);
		mFabFilter = findViewById(R.id.fabFilter);

		mFabFilter.setOnClickListener(this);
		mRecyclerView.setHasFixedSize(true);
		mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
		mRecyclerView.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));

		final String cname = getIntent().getStringExtra("provider.cname");
		assert cname != null;
		mProvider = MangaProvider.getProvider(this, cname);
		setTitle(mProvider.getName());

		mDataset = new ArrayList<>();
		mAdapter = new MangaListAdapter(mDataset);
		mRecyclerView.setAdapter(mAdapter);

		mArguments = new MangaQueryArguments();
		getLoaderManager().initLoader(0, mArguments.toBundle(), this).forceLoad();
	}

	@Override
	public Loader<ArrayList<MangaHeader>> onCreateLoader(int i, Bundle bundle) {
		return new MangaListLoader(this, mProvider, MangaQueryArguments.from(bundle));
	}

	@Override
	public void onLoadFinished(Loader<ArrayList<MangaHeader>> loader, ArrayList<MangaHeader> mangaHeaders) {
		mProgressBar.setVisibility(View.GONE);
		if (mangaHeaders == null) {
			Toast.makeText(this, R.string.loading_error, Toast.LENGTH_SHORT).show();
		} else {
			int firstPos = mDataset.size();
			mDataset.addAll(mangaHeaders);
			if (firstPos == 0) {
				mAdapter.notifyDataSetChanged();
			} else {
				mAdapter.notifyItemRangeInserted(firstPos, mDataset.size() - 1);
			}
		}
	}

	@Override
	public void onLoaderReset(Loader<ArrayList<MangaHeader>> loader) {

	}

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
			case R.id.fabFilter:
				final FilterDialogFragment dialogFragment = new FilterDialogFragment();
				final Bundle args = new Bundle();
				args.putIntArray("sorts", mProvider.getAvailableSortOrders());
				args.putParcelableArray("genres", mProvider.getAvailableGenres());
				args.putBundle("query", mArguments.toBundle());
				dialogFragment.setArguments(args);
				dialogFragment.show(getSupportFragmentManager(), "");
				return;
		}
	}

	@Override
	public void setFilter(int sort, MangaGenre[] genres) {
		final boolean theSame = sort == mArguments.sort && Arrays.equals(mArguments.genres, genres);
		if (theSame) return;
		mArguments.sort = sort;
		mArguments.genres = genres;
		mArguments.page = 0;
		mProgressBar.setVisibility(View.VISIBLE);
		mDataset.clear();
		mAdapter.notifyDataSetChanged();
		getLoaderManager().restartLoader(0, mArguments.toBundle(), this).forceLoad();
	}
}
