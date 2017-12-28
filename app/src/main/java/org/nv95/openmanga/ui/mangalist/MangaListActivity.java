package org.nv95.openmanga.ui.mangalist;

import android.app.LoaderManager;
import android.content.Loader;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import org.nv95.openmanga.R;
import org.nv95.openmanga.content.MangaHeader;
import org.nv95.openmanga.content.providers.MangaProvider;
import org.nv95.openmanga.loaders.MangaListLoader;
import org.nv95.openmanga.ui.AppBaseActivity;

import java.util.ArrayList;

/**
 * Created by koitharu on 28.12.17.
 */

public final class MangaListActivity extends AppBaseActivity implements LoaderManager.LoaderCallbacks<ArrayList<MangaHeader>> {

	private RecyclerView mRecyclerView;
	private ProgressBar mProgressBar;

	private MangaListAdapter mAdapter;
	private ArrayList<MangaHeader> mDataset;
	private MangaProvider mProvider;

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_mangalist);
		setSupportActionBar(R.id.toolbar);
		enableHomeAsUp();

		mProgressBar = findViewById(R.id.progressBar);
		mRecyclerView = findViewById(R.id.recyclerView);
		mRecyclerView.setHasFixedSize(true);
		mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
		mRecyclerView.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));

		String cname = getIntent().getStringExtra("provider.cname");
		assert cname != null;
		mProvider = MangaProvider.getProvider(this, cname);
		setTitle(mProvider.getName());

		mDataset = new ArrayList<>();
		mAdapter = new MangaListAdapter(mDataset);
		mRecyclerView.setAdapter(mAdapter);

		getLoaderManager().initLoader(0, null, this);
		getLoaderManager().getLoader(0).forceLoad();
	}

	@Override
	public Loader<ArrayList<MangaHeader>> onCreateLoader(int i, Bundle bundle) {
		return new MangaListLoader(this, mProvider);
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
}
