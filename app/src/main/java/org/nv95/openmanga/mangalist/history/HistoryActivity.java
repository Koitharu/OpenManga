package org.nv95.openmanga.mangalist.history;

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
import org.nv95.openmanga.common.UndoHelper;
import org.nv95.openmanga.common.utils.AnimationUtils;
import org.nv95.openmanga.common.utils.ErrorUtils;
import org.nv95.openmanga.common.utils.LayoutUtils;
import org.nv95.openmanga.common.utils.MenuUtils;
import org.nv95.openmanga.common.views.recyclerview.SwipeRemoveHelper;
import org.nv95.openmanga.core.ListWrapper;
import org.nv95.openmanga.core.models.MangaHistory;
import org.nv95.openmanga.core.storage.FlagsStorage;
import org.nv95.openmanga.core.storage.db.HistoryRepository;
import org.nv95.openmanga.core.storage.db.HistorySpecification;

import java.util.ArrayList;

/**
 * Created by koitharu on 18.01.18.
 */

public final class HistoryActivity extends AppBaseActivity implements LoaderManager.LoaderCallbacks<ListWrapper<MangaHistory>>,
		SwipeRemoveHelper.OnItemRemovedListener, UndoHelper.OnActionUndoCallback<MangaHistory> {

	private RecyclerView mRecyclerView;
	private ProgressBar mProgressBar;
	private TextView mTextViewHolder;

	private HistoryAdapter mAdapter;
	private ArrayList<MangaHistory> mDataset;
	private HistorySpecification mSpecifications;
	private HistoryRepository mHistoryRepository;

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

		mSpecifications = new HistorySpecification()
				.orderByDate(true);
		mHistoryRepository = HistoryRepository.get(this);

		mDataset = new ArrayList<>();
		mAdapter = new HistoryAdapter(mDataset, FlagsStorage.get(this).isHistoryDetailed());
		mRecyclerView.setAdapter(mAdapter);
		SwipeRemoveHelper.setup(mRecyclerView, this);

		getLoaderManager().initLoader(0, mSpecifications.toBundle(), this).forceLoad();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.options_history, menu);
		MenuUtils.setRadioCheckable(menu.findItem(R.id.action_sort), R.id.group_sort);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		final String orderBy = mSpecifications.getOrderBy();
		if (orderBy != null && orderBy.contains("name")) {
			menu.findItem(R.id.sort_name).setChecked(true);
		} else {
			menu.findItem(R.id.sort_latest).setChecked(true);
		}
		menu.findItem(R.id.option_details).setChecked(mAdapter.isDetailed());
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.sort_latest:
				item.setChecked(true);
				mSpecifications.orderByDate(true);
				getLoaderManager().restartLoader(0, mSpecifications.toBundle(), this).forceLoad();
				return true;
			case R.id.sort_name:
				item.setChecked(true);
				mSpecifications.orderByName(false);
				getLoaderManager().restartLoader(0, mSpecifications.toBundle(), this).forceLoad();
				return true;
			case R.id.option_details:
				boolean checked = !item.isChecked();
				item.setChecked(checked);
				FlagsStorage.get(this).setIsHistoryDetailed(checked);
				mAdapter.setIsDetailed(checked);
				LayoutUtils.forceUpdate(mRecyclerView);
				return true;
			case R.id.action_clear:
				new AlertDialog.Builder(this)
						.setMessage(R.string.history_clear_confirm)
						.setNegativeButton(android.R.string.cancel, null)
						.setPositiveButton(R.string.clear, new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								mHistoryRepository.clear();
								onLoadFinished(null, new ListWrapper<>(new ArrayList<MangaHistory>()));
								Snackbar.make(mRecyclerView, R.string.history_cleared, Snackbar.LENGTH_LONG).show();
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
	public Loader<ListWrapper<MangaHistory>> onCreateLoader(int i, Bundle bundle) {
		final HistorySpecification spec = HistorySpecification.from(bundle);
		return new HistoryLoader(this, spec);
	}

	@Override
	public void onLoadFinished(@Nullable Loader<ListWrapper<MangaHistory>> loader, ListWrapper<MangaHistory> result) {
		mProgressBar.setVisibility(View.GONE);
		if (result.isSuccess()) {
			final ArrayList<MangaHistory> list = result.get();
			mDataset.clear();
			mDataset.addAll(list);
			mAdapter.notifyDataSetChanged();
			AnimationUtils.setVisibility(mTextViewHolder, mDataset.isEmpty() ? View.VISIBLE : View.GONE);
		} else {
			Snackbar.make(mRecyclerView, ErrorUtils.getErrorMessage(result.getError()), Toast.LENGTH_SHORT).show();
		}
	}

	@Override
	public void onLoaderReset(Loader<ListWrapper<MangaHistory>> loader) {

	}

	@Override
	public void onItemRemoved(int position) {
		final MangaHistory item = mDataset.remove(position);
		mHistoryRepository.remove(item);
		mAdapter.notifyItemRemoved(position);
		AnimationUtils.setVisibility(mTextViewHolder, mDataset.isEmpty() ? View.VISIBLE : View.GONE);
		new UndoHelper<>(position, this)
				.snackbar(mRecyclerView, getString(R.string.removed_from_history, item.name), item, Snackbar.LENGTH_SHORT);
	}

	@Override
	public void onActionUndo(int actionId, MangaHistory data) {
		mHistoryRepository.add(data);
		mDataset.add(actionId, data);
		mAdapter.notifyItemRemoved(actionId);
		AnimationUtils.setVisibility(mTextViewHolder, mDataset.isEmpty() ? View.VISIBLE : View.GONE);
	}
}
