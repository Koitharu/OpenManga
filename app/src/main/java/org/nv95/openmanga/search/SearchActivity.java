package org.nv95.openmanga.search;

import android.app.LoaderManager;
import android.app.SearchManager;
import android.content.Intent;
import android.content.Loader;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.LinearLayoutManager;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.nv95.openmanga.R;
import org.nv95.openmanga.core.ListWrapper;
import org.nv95.openmanga.core.models.MangaHeader;
import org.nv95.openmanga.core.models.ProviderHeader;
import org.nv95.openmanga.core.storage.ProvidersStore;
import org.nv95.openmanga.AppBaseActivity;
import org.nv95.openmanga.common.views.EndlessRecyclerView;

import java.util.ArrayList;
import java.util.Stack;

/**
 * Created by koitharu on 06.01.18.
 */

public final class SearchActivity extends AppBaseActivity implements LoaderManager.LoaderCallbacks<ListWrapper<MangaHeader>>,
		EndlessRecyclerView.OnLoadMoreListener, View.OnClickListener {

	private EndlessRecyclerView mRecyclerView;
	private ProgressBar mProgressBar;
	private TextView mTextViewHolder;

	private final Stack<ProviderHeader> mProviders = new Stack<>();
	private SearchQueryArguments mQueryArguments;

	private final ArrayList<Object> mDataset = new ArrayList<>();
	private SearchAdapter mAdapter;


	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_search);
		setSupportActionBar(R.id.toolbar);
		enableHomeAsUp();

		mRecyclerView = findViewById(R.id.recyclerView);
		mProgressBar = findViewById(R.id.progressBar);
		mTextViewHolder = findViewById(R.id.textView_holder);

		mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
		mRecyclerView.setOnLoadMoreListener(this);

		Intent intent = getIntent();
		if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
			String query = intent.getStringExtra(SearchManager.QUERY);
			beginSearch(query);
		}
	}

	private void beginSearch(@NonNull String query) {
		mProgressBar.setVisibility(View.VISIBLE);
		mTextViewHolder.setVisibility(View.GONE);
		setSubtitle(query);
		SearchSuggestionsProvider.getSuggestions(this).saveRecentQuery(query, null);
		mProviders.clear();
		mProviders.addAll(new ProvidersStore(this).getUserProviders());
		mQueryArguments = new SearchQueryArguments(query, mProviders.pop().cName);
		mDataset.clear();
		mAdapter = new SearchAdapter(mDataset);
		mRecyclerView.setAdapter(mAdapter);
		getLoaderManager().initLoader(0, mQueryArguments.toBundle(), this).forceLoad();
	}

	@Override
	public Loader<ListWrapper<MangaHeader>> onCreateLoader(int id, Bundle args) {
		return new SearchLoader(this, SearchQueryArguments.from(args));
	}

	@Override
	public void onLoadFinished(Loader<ListWrapper<MangaHeader>> loader, @NonNull ListWrapper<MangaHeader> result) {
		if (result.isFailed()) {
			Snackbar.make(mRecyclerView, R.string.loading_error, Snackbar.LENGTH_INDEFINITE)
					.setAction(R.string.retry, this)
					.show();
			mProgressBar.setVisibility(View.GONE);
			if (mDataset.isEmpty()) {
				mTextViewHolder.setVisibility(View.VISIBLE);
			}
			mRecyclerView.onLoadingFinished(false);
		} else if (result.isEmpty()) {
			//next provider
			if (mProviders.empty()) {
				mProgressBar.setVisibility(View.GONE);
				mRecyclerView.onLoadingFinished(false);
				if (mDataset.isEmpty()) {
					mTextViewHolder.setVisibility(View.VISIBLE);
				}
			} else {
				mQueryArguments.page = 0;
				mQueryArguments.providerCName = mProviders.pop().cName;
				mRecyclerView.onLoadingFinished(true);
			}
		} else {
			mProgressBar.setVisibility(View.GONE);
			int firstPos = mDataset.size();
			mDataset.addAll(result.get());
			if (firstPos == 0) {
				mAdapter.notifyDataSetChanged();
			} else {
				mAdapter.notifyItemRangeInserted(firstPos, result.size());
			}
			mQueryArguments.page++;
			mRecyclerView.onLoadingFinished(true);
		}
	}

	@Override
	public void onLoaderReset(Loader<ListWrapper<MangaHeader>> loader) {

	}

	@Override
	public boolean onLoadMore() {
		getLoaderManager().restartLoader(0, mQueryArguments.toBundle(), this).forceLoad();
		return true;
	}

	@Override
	public void onClick(View v) {
		if (mDataset.isEmpty()) {
			mProgressBar.setVisibility(View.VISIBLE);
		}
		mTextViewHolder.setVisibility(View.GONE);
		onLoadMore();
	}
}
