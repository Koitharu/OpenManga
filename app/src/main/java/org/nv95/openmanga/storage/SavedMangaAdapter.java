package org.nv95.openmanga.storage;

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
import org.nv95.openmanga.core.models.SavedManga;
import org.nv95.openmanga.core.providers.MangaProvider;
import org.nv95.openmanga.preview.PreviewActivity;

import java.util.ArrayList;

/**
 * Created by koitharu on 26.01.18.
 */

public final class SavedMangaAdapter extends RecyclerView.Adapter<SavedMangaAdapter.MangaHolder> {

	private final ArrayList<SavedMangaSummary> mDataset;

	SavedMangaAdapter(ArrayList<SavedMangaSummary> dataset) {
		setHasStableIds(true);
		mDataset = dataset;
	}

	@Override
	public MangaHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		return new MangaHolder(LayoutInflater.from(parent.getContext())
				.inflate(R.layout.item_manga_list, parent, false));
	}

	@Override
	public void onBindViewHolder(MangaHolder holder, int position) {
		SavedMangaSummary item = mDataset.get(position);
		holder.text1.setText(item.manga.name);
		holder.text2.setText(item.savedChapters == -1 ? "" :
				holder.itemView.getResources().getQuantityString(R.plurals.chapters_saved, item.savedChapters, item.savedChapters));
		holder.summary.setText(item.manga.genres);
		ImageUtils.setThumbnail(holder.imageView, item.manga.thumbnail, MangaProvider.getDomain(item.manga.provider));
		holder.itemView.setTag(item);
	}

	@Override
	public int getItemCount() {
		return mDataset.size();
	}

	@Override
	public long getItemId(int position) {
		return mDataset.get(position).manga.id;
	}

	@Override
	public void onViewRecycled(MangaHolder holder) {
		ImageUtils.recycle(holder.imageView);
		super.onViewRecycled(holder);
	}

	class MangaHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

		final TextView text1;
		final TextView text2;
		final TextView summary;
		final ImageView imageView;

		private MangaHolder(View itemView) {
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
			final SavedManga item = mDataset.get(getAdapterPosition()).manga;
			switch (view.getId()) {
				default:
					context.startActivity(new Intent(context.getApplicationContext(), PreviewActivity.class)
							.putExtra("manga", item));
			}
		}
	}
}