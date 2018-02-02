package org.nv95.openmanga.tools.settings.providers;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.SparseBooleanArray;

import org.nv95.openmanga.R;
import org.nv95.openmanga.core.models.ProviderHeader;
import org.nv95.openmanga.core.storage.ProvidersStore;
import org.nv95.openmanga.AppBaseActivity;
import org.nv95.openmanga.common.utils.CollectionsUtils;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by koitharu on 17.01.18.
 */

public final class ProvidersSettingsActivity extends AppBaseActivity implements ProvidersAdapter.OnStartDragListener {

	private ProvidersStore mProvidersStore;
	private ProvidersAdapter mAdapter;
	private ArrayList<ProviderHeader> mProviders;
	private SparseBooleanArray mChecks;
	private ItemTouchHelper mItemTouchHelper;

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_settings_providers);
		setSupportActionBar(R.id.toolbar);
		enableHomeAsUp();
		mProvidersStore = new ProvidersStore(this);
		mChecks = new SparseBooleanArray();
		mProviders = mProvidersStore.getAllProvidersSorted();
		int[] disabled = mProvidersStore.getDisabledIds();
		for (int i = 0; i < mProviders.size(); i++) {
			if (CollectionsUtils.indexOf(disabled, mProviders.get(i).hashCode()) != -1) {
				mChecks.put(i, false);
			}
		}
		final RecyclerView recyclerView = findViewById(R.id.recyclerView);
		mAdapter = new ProvidersAdapter(mProviders, mChecks, this);
		recyclerView.setAdapter(mAdapter);

		mItemTouchHelper = new ItemTouchHelper(new ReorderCallback());
		mItemTouchHelper.attachToRecyclerView(recyclerView);
	}

	@Override
	public void onStartDrag(RecyclerView.ViewHolder viewHolder) {
		mItemTouchHelper.startDrag(viewHolder);
	}

	@Override
	protected void onPause() {
		mProvidersStore.save(mProviders, mChecks);
		super.onPause();
	}

	private class ReorderCallback extends ItemTouchHelper.SimpleCallback {


		ReorderCallback() {
			super(ItemTouchHelper.DOWN | ItemTouchHelper.UP, 0);
		}

		@Override
		public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
			final int fromPosition = viewHolder.getAdapterPosition();
			final int toPosition = target.getAdapterPosition();
			Collections.swap(mProviders, fromPosition, toPosition);
			CollectionsUtils.swap(mChecks, fromPosition, toPosition, true);
			mAdapter.notifyItemMoved(fromPosition, toPosition);
			return true;
		}

		@Override
		public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {

		}

		@Override
		public boolean isLongPressDragEnabled() {
			return false;
		}
	}
}
