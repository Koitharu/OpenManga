package org.nv95.openmanga.mangalist.history;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.nv95.openmanga.R;
import org.nv95.openmanga.common.utils.ImageUtils;
import org.nv95.openmanga.common.utils.ResourceUtils;
import org.nv95.openmanga.core.models.MangaHistory;
import org.nv95.openmanga.core.providers.MangaProvider;
import org.nv95.openmanga.preview.PreviewActivity;

import java.util.ArrayList;

/**
 * Created by koitharu on 18.01.18.
 */

final class MangaHistoryAdapter extends RecyclerView.Adapter<MangaHistoryAdapter.HistoryHolder> {

	private final ArrayList<MangaHistory> mDataset;

	MangaHistoryAdapter(ArrayList<MangaHistory> dataset) {
		setHasStableIds(true);
		mDataset = dataset;
	}

	@Override
	public HistoryHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		return new HistoryHolder(LayoutInflater.from(parent.getContext())
				.inflate(R.layout.item_manga_list, parent, false));
	}

	@Override
	public void onBindViewHolder(HistoryHolder holder, int position) {
		MangaHistory item = mDataset.get(position);
		holder.text1.setText(item.name);
		holder.text2.setText(ResourceUtils.formatTimeRelative(item.updatedAt));
		holder.summary.setText(item.genres);
		ImageUtils.setThumbnail(holder.imageView, item.thumbnail, MangaProvider.getDomain(item.provider));
		holder.itemView.setTag(item);
	}

	@Override
	public int getItemCount() {
		return mDataset.size();
	}

	@Override
	public long getItemId(int position) {
		return mDataset.get(position).id;
	}

	@Override
	public void onViewRecycled(HistoryHolder holder) {
		ImageUtils.recycle(holder.imageView);
		super.onViewRecycled(holder);
	}

	class HistoryHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

		final TextView text1;
		final TextView text2;
		final TextView summary;
		final ImageView imageView;

		HistoryHolder(View itemView) {
			super(itemView);
			text1 = itemView.findViewById(android.R.id.text1);
			text2 = itemView.findViewById(android.R.id.text2);
			summary = itemView.findViewById(android.R.id.summary);
			imageView = itemView.findViewById(R.id.imageView);

			itemView.setOnClickListener(this);
		}

		@Override
		public void onClick(View view) {
			final Context context = view.getContext();
			final MangaHistory item = mDataset.get(getAdapterPosition());
			switch (view.getId()) {
				default:
					context.startActivity(new Intent(context.getApplicationContext(), PreviewActivity.class)
							.putExtra("manga", item));
			}
		}
	}
}
