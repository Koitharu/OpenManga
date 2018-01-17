package org.nv95.openmanga.ui.settings.providers;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.v7.widget.AppCompatImageButton;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.RecyclerView;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import org.nv95.openmanga.R;
import org.nv95.openmanga.content.ProviderHeader;

import java.util.ArrayList;

/**
 * Created by koitharu on 17.01.18.
 */

final class ProvidersAdapter extends RecyclerView.Adapter<ProvidersAdapter.ProviderHolder> {

	private final OnStartDragListener mDragListener;
	private final ArrayList<ProviderHeader> mDataset;
	private final SparseBooleanArray mChecks;

	ProvidersAdapter(ArrayList<ProviderHeader> dataset, SparseBooleanArray checks, OnStartDragListener dragListener) {
		mDataset = dataset;
		mChecks = checks;
		mDragListener = dragListener;
		setHasStableIds(true);
	}

	@Override
	public ProviderHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		return new ProviderHolder(LayoutInflater.from(parent.getContext())
				.inflate(R.layout.item_provider, parent, false));
	}

	@Override
	public void onBindViewHolder(ProviderHolder holder, int position) {
		holder.checkBox.setChecked(mChecks.get(position, true));
		holder.textView.setText(mDataset.get(position).dName);
	}

	@Override
	public int getItemCount() {
		return mDataset.size();
	}

	@Override
	public long getItemId(int position) {
		return mDataset.get(position).cName.hashCode();
	}

	@SuppressLint("ClickableViewAccessibility")
	class ProviderHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnTouchListener {

		final CheckBox checkBox;
		final TextView textView;
		final AppCompatImageButton imageButtonSettings;
		final AppCompatImageView imageViewReorder;

		ProviderHolder(View itemView) {
			super(itemView);
			checkBox = itemView.findViewById(android.R.id.checkbox);
			textView = itemView.findViewById(android.R.id.text1);
			imageButtonSettings = itemView.findViewById(R.id.imageButton_settings);
			imageViewReorder = itemView.findViewById(R.id.imageView_reorder);

			imageButtonSettings.setOnClickListener(this);
			checkBox.setOnClickListener(this);
			imageViewReorder.setOnTouchListener(this);
		}

		@Override
		public void onClick(View v) {
			final int position = getAdapterPosition();
			switch (v.getId()) {
				case android.R.id.checkbox:
					mChecks.put(position, checkBox.isChecked());
					break;
				case R.id.imageButton_settings:
					final Context context = v.getContext();
					final String cName = mDataset.get(position).cName;
					//TODO start activity
			}
		}

		@Override
		public boolean onTouch(View v, MotionEvent event) {
			if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
				mDragListener.onStartDrag(this);
				return true;
			}
			return false;
		}
	}

	interface OnStartDragListener {
		void onStartDrag(RecyclerView.ViewHolder viewHolder);
	}
}
