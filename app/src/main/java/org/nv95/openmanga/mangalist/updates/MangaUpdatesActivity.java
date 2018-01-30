package org.nv95.openmanga.mangalist.updates;

import android.app.LoaderManager;
import android.content.DialogInterface;
import android.content.Loader;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.nv95.openmanga.AppBaseActivity;
import org.nv95.openmanga.R;
import org.nv95.openmanga.common.OemBadgeHelper;
import org.nv95.openmanga.common.utils.AnimationUtils;
import org.nv95.openmanga.common.utils.ErrorUtils;
import org.nv95.openmanga.common.views.recyclerview.SwipeRemoveHelper;
import org.nv95.openmanga.core.ListWrapper;
import org.nv95.openmanga.core.models.MangaFavourite;
import org.nv95.openmanga.core.storage.db.FavouritesRepository;
import org.nv95.openmanga.core.storage.db.FavouritesSpecification;
import org.nv95.openmanga.mangalist.favourites.FavouritesLoader;

import java.util.ArrayList;

/**
 * Created by koitharu on 30.01.18.
 */

public final class MangaUpdatesActivity extends AppBaseActivity implements SwipeRemoveHelper.OnItemRemovedListener,
		LoaderManager.LoaderCallbacks<ListWrapper<MangaFavourite>> {

	private RecyclerView mRecyclerView;
	private ProgressBar mProgressBar;
	private TextView mTextViewHolder;

	private MangaUpdatesAdapter mAdapter;
	private ArrayList<MangaFavourite> mDataset;

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_history);
		setSupportActionBar(R.id.toolbar);
		enableHomeAsUp();

		mProgressBar = findViewById(R.id.progressBar);
		mRecyclerView = findViewById(R.id.recyclerView);
		mTextViewHolder = findViewById(R.id.textView_holder);
		mRecyclerView.setHasFixedSize(true);
		mRecyclerView.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));
		mTextViewHolder.setText(R.string.no_new_chapters);

		mDataset = new ArrayList<>();
		mAdapter = new MangaUpdatesAdapter(mDataset);
		mRecyclerView.setAdapter(mAdapter);
		SwipeRemoveHelper.setup(mRecyclerView, this, R.color.green_overlay, R.drawable.ic_done_all_white);

		getLoaderManager().initLoader(0, null, this).forceLoad();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.options_updates, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.action_clear:
				new AlertDialog.Builder(this)
						.setMessage(R.string.mark_all_viewed_confirm)
						.setNegativeButton(android.R.string.cancel, null)
						.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								FavouritesRepository.get(MangaUpdatesActivity.this).clearNewChapters();
								onLoadFinished(null, new ListWrapper<>(new ArrayList<MangaFavourite>()));
								Snackbar.make(mRecyclerView, R.string.chapters_marked_as_viewed, Snackbar.LENGTH_LONG).show();
								new OemBadgeHelper(MangaUpdatesActivity.this).applyCount(0);
							}
						})
						.create()
						.show();
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public Loader<ListWrapper<MangaFavourite>> onCreateLoader(int i, Bundle bundle) {
		return new FavouritesLoader(this, new FavouritesSpecification().onlyWithNewChapters().orderByDate(true));
	}

	@Override
	public void onLoadFinished(@Nullable Loader<ListWrapper<MangaFavourite>> loader, ListWrapper<MangaFavourite> result) {
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

	@Override
	public void onItemRemoved(int position) {
		final MangaFavourite item = mDataset.remove(position);
		mAdapter.notifyItemRemoved(position);
		FavouritesRepository.get(this).setNoUpdates(item);
		AnimationUtils.setVisibility(mTextViewHolder, mDataset.isEmpty() ? View.VISIBLE : View.GONE);
		Snackbar.make(mRecyclerView, R.string.chapters_marked_as_viewed, Snackbar.LENGTH_SHORT).show();
		int total = 0;
		for (MangaFavourite o : mDataset) {
			total += o.newChapters;
		}
		new OemBadgeHelper(this).applyCount(total);
	}
}
