package org.nv95.openmanga.ui.mangalist;

import android.app.LoaderManager;
import android.content.Loader;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.SearchView;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import org.nv95.openmanga.R;
import org.nv95.openmanga.content.MangaGenre;
import org.nv95.openmanga.content.MangaHeader;
import org.nv95.openmanga.content.MangaQueryArguments;
import org.nv95.openmanga.content.providers.MangaProvider;
import org.nv95.openmanga.ui.AppBaseActivity;
import org.nv95.openmanga.ui.common.EndlessRecyclerView;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by koitharu on 28.12.17.
 */

public final class MangaListActivity extends AppBaseActivity implements LoaderManager.LoaderCallbacks<ArrayList<MangaHeader>>,
		View.OnClickListener, FilterCallback, SearchView.OnQueryTextListener, EndlessRecyclerView.OnLoadMoreListener {

	private EndlessRecyclerView mRecyclerView;
	private ProgressBar mProgressBar;
	private FloatingActionButton mFabFilter;
	private SearchView mSearchView;
	private MenuItem mMenuItemSearch;

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
		mRecyclerView.setOnLoadMoreListener(this);

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
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.options_mangalist, menu);
		mMenuItemSearch = menu.findItem(R.id.action_search);
		mSearchView = (SearchView) mMenuItemSearch.getActionView();
		mSearchView.setOnQueryTextListener(this);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public Loader<ArrayList<MangaHeader>> onCreateLoader(int i, Bundle bundle) {
		final MangaQueryArguments queryArgs = MangaQueryArguments.from(bundle);
		if (!TextUtils.isEmpty(queryArgs.query)) {
			setSubtitle(queryArgs.query);
		} else if (queryArgs.genres.length != 0) {
			setSubtitle(MangaGenre.joinNames(this, queryArgs.genres, ", "));
		} else {
			setSubtitle(null);
		}
		return new MangaListLoader(this, mProvider, queryArgs);
	}

	@Override
	public void onLoadFinished(Loader<ArrayList<MangaHeader>> loader, ArrayList<MangaHeader> mangaHeaders) {
		mProgressBar.setVisibility(View.GONE);
		if (mangaHeaders == null) {
			Toast.makeText(this, R.string.loading_error, Toast.LENGTH_SHORT).show();
			mRecyclerView.onLoadingFinished(false);
		} else {
			int firstPos = mDataset.size();
			mDataset.addAll(mangaHeaders);
			if (firstPos == 0) {
				mAdapter.notifyDataSetChanged();
			} else {
				mAdapter.notifyItemRangeInserted(firstPos, mangaHeaders.size());
			}
			mRecyclerView.onLoadingFinished(!mangaHeaders.isEmpty());
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

	@Override
	public boolean onQueryTextSubmit(String query) {
		mMenuItemSearch.collapseActionView();
		if (TextUtils.equals(query, mArguments.query)) {
			return true;
		}
		mArguments.query = query;
		mArguments.page = 0;
		mProgressBar.setVisibility(View.VISIBLE);
		mDataset.clear();
		mAdapter.notifyDataSetChanged();
		getLoaderManager().restartLoader(0, mArguments.toBundle(), this).forceLoad();
		return true;
	}

	@Override
	public boolean onQueryTextChange(String newText) {
		return false;
	}

	@Override
	public boolean onLoadMore() {
		mArguments.page++;
		getLoaderManager().restartLoader(0, mArguments.toBundle(), this).forceLoad();
		return true;
	}
}
