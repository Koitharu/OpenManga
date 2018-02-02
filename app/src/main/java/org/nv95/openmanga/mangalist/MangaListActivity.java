package org.nv95.openmanga.mangalist;

import android.app.LoaderManager;
import android.content.Loader;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.SearchView;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewStub;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.nv95.openmanga.AppBaseActivity;
import org.nv95.openmanga.R;
import org.nv95.openmanga.common.utils.ErrorUtils;
import org.nv95.openmanga.common.views.EndlessRecyclerView;
import org.nv95.openmanga.core.ListWrapper;
import org.nv95.openmanga.core.models.MangaGenre;
import org.nv95.openmanga.core.models.MangaHeader;
import org.nv95.openmanga.core.providers.MangaProvider;
import org.nv95.openmanga.tools.settings.AuthorizationDialog;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by koitharu on 28.12.17.
 */

public final class MangaListActivity extends AppBaseActivity implements LoaderManager.LoaderCallbacks<ListWrapper<MangaHeader>>,
		View.OnClickListener, FilterCallback, SearchView.OnQueryTextListener, EndlessRecyclerView.OnLoadMoreListener,
		AuthorizationDialog.Callback {

	private EndlessRecyclerView mRecyclerView;
	private ProgressBar mProgressBar;
	private FloatingActionButton mFabFilter;
	private SearchView mSearchView;
	private MenuItem mMenuItemSearch;
	private TextView mTextViewError;
	private View mErrorView;

	private List<MangaHeader> mDataset = new ArrayList<>();
	private MangaListAdapter mAdapter = new MangaListAdapter(mDataset);
	private MangaProvider mProvider;
	private MangaQueryArguments mArguments = new MangaQueryArguments();

	/**
	 * loader's id
	 */
	private static int LOADER_ID = 234;

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_mangalist);
		setSupportActionBar(R.id.toolbar);
		enableHomeAsUp();

		mProgressBar = findViewById(R.id.progressBar);
		mRecyclerView = findViewById(R.id.recyclerView);
		mFabFilter = findViewById(R.id.fabFilter);
		mErrorView = findViewById(R.id.stub_error);

		mFabFilter.setOnClickListener(this);
		mRecyclerView.setHasFixedSize(true);
		mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
		mRecyclerView.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));
		mRecyclerView.setOnLoadMoreListener(this);

		final String cname = getIntent().getStringExtra("provider.cname");
		assert cname != null;
		mProvider = MangaProvider.get(this, cname);
		setTitle(mProvider.getName());
		if (mProvider.getAvailableGenres().length == 0 && mProvider.getAvailableSortOrders().length == 0) {
			mFabFilter.setVisibility(View.GONE);
		}

		mRecyclerView.setAdapter(mAdapter);

		load();
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
	public boolean onPrepareOptionsMenu(Menu menu) {
		menu.findItem(R.id.action_authorize).setVisible(mProvider.isAuthorizationSupported() && !mProvider.isAuthorized());
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.action_authorize:
				final AuthorizationDialog dialog = new AuthorizationDialog();
				final Bundle args = new Bundle(1);
				args.putString("provider", mProvider.getCName());
				dialog.setArguments(args);
				dialog.show(getSupportFragmentManager(), "auth");
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public Loader<ListWrapper<MangaHeader>> onCreateLoader(int i, Bundle bundle) {
		final MangaQueryArguments queryArgs = MangaQueryArguments.from(bundle);

		setSubtitle(!TextUtils.isEmpty(queryArgs.query)
				? queryArgs.query
				: queryArgs.genres.length != 0
				? MangaGenre.joinNames(this, queryArgs.genres, ", ")
				: null);

		return new MangaListLoader(this, mProvider, queryArgs);
	}

	@Override
	public void onLoadFinished(Loader<ListWrapper<MangaHeader>> loader, ListWrapper<MangaHeader> result) {
		mProgressBar.setVisibility(View.GONE);
		if (result.isSuccess()) {
			final ArrayList<MangaHeader> list = result.get();
			int firstPos = mDataset.size();
			mDataset.addAll(list);
			if (firstPos == 0) {
				mAdapter.notifyDataSetChanged();
			} else {
				mAdapter.notifyItemRangeInserted(firstPos, list.size());
			}
			mRecyclerView.onLoadingFinished(!list.isEmpty());
		} else {
			setError(result.getError());
			mRecyclerView.onLoadingFinished(false);
		}
	}

	@Override
	public void onLoaderReset(Loader<ListWrapper<MangaHeader>> loader) {

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
				break;
			case R.id.button_retry:
				mErrorView.setVisibility(View.GONE);
				mProgressBar.setVisibility(View.VISIBLE);
			case android.support.design.R.id.snackbar_action:
				load();
		}
	}

	@Override
	public void setFilter(int sort, MangaGenre[] genres) {
		final boolean theSame = sort == mArguments.sort && Arrays.equals(mArguments.genres, genres);
		if (theSame) return;
		mArguments.sort = sort;
		mArguments.genres = genres;
		loadFirstPage();
	}

	@Override
	public boolean onQueryTextSubmit(String query) {
		mMenuItemSearch.collapseActionView();
		if (TextUtils.equals(query, mArguments.query)) {
			return true;
		}
		mArguments.query = query;
		loadFirstPage();
		return true;
	}

	/**
	 * Сбрасываем на первую страницу
	 */
	private void loadFirstPage() {
		mArguments.page = 0;
		mProgressBar.setVisibility(View.VISIBLE);
		mDataset.clear();
		mAdapter.notifyDataSetChanged();
		load();
	}

	@Override
	public boolean onQueryTextChange(String newText) {
		return false;
	}

	@Override
	public boolean onLoadMore() {
		mArguments.page++;
		load();
		return true;
	}

	@Override
	public void onAuthorized() {
		Snackbar.make(mRecyclerView, R.string.authorization_success, Snackbar.LENGTH_SHORT).show();
		mArguments.page = 0;
		load();
	}

	private void setError(Throwable e) {
		if (mDataset.isEmpty()) {
			if (mErrorView instanceof ViewStub) {
				mErrorView = ((ViewStub) mErrorView).inflate();
				mTextViewError = mErrorView.findViewById(R.id.textView_error);
				mErrorView.findViewById(R.id.button_retry).setOnClickListener(this);
			}
			mTextViewError.setText(ErrorUtils.getErrorMessage(this, e));
			mErrorView.setVisibility(View.VISIBLE);
		} else {
			Snackbar.make(mRecyclerView, ErrorUtils.getErrorMessage(e), Snackbar.LENGTH_INDEFINITE)
					.setAction(R.string.retry, this)
					.show();
		}
	}

	/**
	 * Лоадер не перезагрузится если
	 * Bundle тот же самый с темиже значениями (вроде)
	 */
	private void load() {
		mRecyclerView.onLoadingStarted();
		Loader<ListWrapper<MangaHeader>> loader = getLoaderManager().getLoader(LOADER_ID);
		if (loader == null) {
			loader = getLoaderManager().initLoader(LOADER_ID, mArguments.toBundle(), this);
		} else {
			loader = getLoaderManager().restartLoader(LOADER_ID, mArguments.toBundle(), this);
		}
		loader.forceLoad();
	}
}
