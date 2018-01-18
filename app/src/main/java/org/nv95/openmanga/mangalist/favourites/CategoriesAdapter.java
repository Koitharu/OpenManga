package org.nv95.openmanga.mangalist.favourites;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import org.nv95.openmanga.R;
import org.nv95.openmanga.core.models.Category;

import java.util.ArrayList;

/**
 * Created by koitharu on 18.01.18.
 */

final class CategoriesAdapter extends RecyclerView.Adapter<CategoriesAdapter.CategoryHolder> {

	private final OnClickListener mClickListener;
	private final ArrayList<Category> mDataset;

	CategoriesAdapter(ArrayList<Category> dataset, OnClickListener clickListener) {
		mDataset = dataset;
		mClickListener = clickListener;
		setHasStableIds(true);
	}

	@Override
	public CategoryHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		return new CategoryHolder(LayoutInflater.from(parent.getContext())
				.inflate(R.layout.item_category, parent, false));
	}

	@Override
	public void onBindViewHolder(CategoryHolder holder, int position) {
		holder.text1.setText(mDataset.get(position).name);
	}

	@Override
	public int getItemCount() {
		return mDataset.size();
	}

	@Override
	public long getItemId(int position) {
		return mDataset.get(position).id;
	}

	class CategoryHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

		final ImageButton buttonRemove;
		final TextView text1;

		CategoryHolder(View itemView) {
			super(itemView);
			text1 = itemView.findViewById(android.R.id.text1);
			buttonRemove = itemView.findViewById(R.id.button_remove);
			itemView.setOnClickListener(this);
			buttonRemove.setOnClickListener(this);
		}

		@Override
		public void onClick(View v) {
			final int position = getAdapterPosition();
			switch (v.getId()) {
				case R.id.button_remove:
					mClickListener.onItemActionClick(position);
					break;
				default:
					mClickListener.onItemClick(position);
			}
		}
	}

	interface OnClickListener {
		void onItemClick(int position);
		void onItemActionClick(int position);
	}
}

