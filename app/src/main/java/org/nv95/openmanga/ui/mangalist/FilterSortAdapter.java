package org.nv95.openmanga.ui.mangalist;

import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import java.util.ArrayList;

/**
 * Created by koitharu on 31.12.17.
 */

public final class FilterSortAdapter extends RecyclerView.Adapter {

	private final ArrayList<Object> mDataset;

	public FilterSortAdapter(ArrayList<Object> dataset) {
		mDataset = dataset;
	}

	@Override
	public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		return null;
	}

	@Override
	public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

	}

	@Override
	public int getItemCount() {
		return 0;
	}
}
