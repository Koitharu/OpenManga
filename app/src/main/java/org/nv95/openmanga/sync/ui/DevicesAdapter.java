package org.nv95.openmanga.sync.ui;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.nv95.openmanga.R;
import org.nv95.openmanga.sync.SyncDevice;
import org.nv95.openmanga.utils.ResourceUtils;

import java.util.ArrayList;

/**
 * Created by koitharu on 19.12.17.
 */

public class DevicesAdapter extends RecyclerView.Adapter<DevicesAdapter.DeviceHolder> implements View.OnClickListener {

	private final ArrayList<SyncDevice> mDataset;
	private final OnItemClickListener mClickListener;

	public DevicesAdapter(ArrayList<SyncDevice> dataset, OnItemClickListener clickListener) {
		mDataset = dataset;
		mClickListener = clickListener;
	}

	@Override
	public DeviceHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		DeviceHolder holder = new DeviceHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_device, parent, false));
		holder.itemView.setOnClickListener(this);
		return holder;
	}

	@Override
	public void onBindViewHolder(DeviceHolder holder, int position) {
		SyncDevice item = mDataset.get(position);
		holder.textViewDate.setText(ResourceUtils.formatDateTimeRelative(holder.textViewName.getContext(), item.created_at));
		holder.textViewName.setText(item.name);
		holder.itemView.setTag(item);
	}

	@Override
	public int getItemCount() {
		return mDataset.size();
	}

	@Override
	public void onClick(View view) {
		mClickListener.onItemClick((SyncDevice) view.getTag());
	}

	class DeviceHolder extends RecyclerView.ViewHolder {

		final TextView textViewName;
		final TextView textViewDate;

		DeviceHolder(View itemView) {
			super(itemView);
			textViewName = itemView.findViewById(R.id.textName);
			textViewDate = itemView.findViewById(R.id.textDate);
		}
	}

	interface OnItemClickListener {

		void onItemClick(SyncDevice item);
	}
}
