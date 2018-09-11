package org.nv95.openmanga.recommendations;

/*
 * Created by koitharu on 29.01.18.
 */

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
import org.nv95.openmanga.common.utils.LayoutUtils;
import org.nv95.openmanga.core.models.MangaRecommendation;
import org.nv95.openmanga.core.providers.MangaProvider;
import org.nv95.openmanga.preview.PreviewActivity;

import java.util.ArrayList;

final class RecommendationsAdapter extends RecyclerView.Adapter<RecommendationsAdapter.RecommendationsHolder> {

	private final ArrayList<MangaRecommendation> mDataset;

	RecommendationsAdapter(ArrayList<MangaRecommendation> dataset) {
		setHasStableIds(true);
		mDataset = dataset;
	}

	@Override
	public RecommendationsHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		return new RecommendationsHolder(LayoutInflater.from(parent.getContext())
				.inflate(R.layout.item_manga_list, parent, false));
	}

	@Override
	public void onBindViewHolder(RecommendationsHolder holder, int position) {
		MangaRecommendation item = mDataset.get(position);
		holder.text1.setText(item.name);
		LayoutUtils.setTextOrHide(holder.text2, item.summary);
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
	public void onViewRecycled(RecommendationsHolder holder) {
		ImageUtils.recycle(holder.imageView);
		super.onViewRecycled(holder);
	}

	class RecommendationsHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

		final TextView text1;
		final TextView text2;
		final TextView summary;
		final ImageView imageView;

		RecommendationsHolder(View itemView) {
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
			final MangaRecommendation item = mDataset.get(getAdapterPosition());
			switch (view.getId()) {
				default:
					context.startActivity(new Intent(context.getApplicationContext(), PreviewActivity.class)
							.putExtra("manga", item));
			}
		}
	}
}

