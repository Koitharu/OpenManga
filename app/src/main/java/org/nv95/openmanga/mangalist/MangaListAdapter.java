package org.nv95.openmanga.mangalist;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.IntDef;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.nv95.openmanga.R;
import org.nv95.openmanga.core.models.MangaHeader;
import org.nv95.openmanga.core.providers.MangaProvider;
import org.nv95.openmanga.preview.PreviewActivity;
import org.nv95.openmanga.common.views.EndlessRecyclerView;
import org.nv95.openmanga.common.utils.ImageUtils;
import org.nv95.openmanga.common.utils.LayoutUtils;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.List;

/**
 * Created by koitharu on 28.12.17.
 */

public final class MangaListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements
		EndlessRecyclerView.EndlessAdapter {

	private final List<MangaHeader> mDataset;
	private boolean mInProgress;

	MangaListAdapter(List<MangaHeader> dataset) {
		setHasStableIds(true);
		mDataset = dataset;
		mInProgress = true;
	}

	@Override
	public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, @ItemViewType int viewType) {
		final LayoutInflater inflater = LayoutInflater.from(parent.getContext());
		RecyclerView.ViewHolder holder;
		switch (viewType) {
			case ItemViewType.TYPE_ITEM_MANGA:
				holder = new MangaHeaderHolder(inflater.inflate(R.layout.item_manga_list, parent, false));
				break;
			case ItemViewType.TYPE_ITEM_PROGRESS:
				return new ProgressHolder(inflater.inflate(R.layout.item_progress, parent, false));
			default:
				throw new AssertionError("Unknown viewType");
		}
		return holder;
	}

	@Override
	public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
		if (holder instanceof MangaHeaderHolder) {
			((MangaHeaderHolder) holder).bind(mDataset.get(position));
		} else if (holder instanceof ProgressHolder) {
			((ProgressHolder) holder).progressBar.setVisibility(mInProgress ? View.VISIBLE : View.INVISIBLE);
		}
 	}

	@Override
	public int getItemCount() {
		final int size = mDataset.size();
		return size == 0 ? 0 : size + 1;
	}

	@ItemViewType
	@Override
	public int getItemViewType(int position) {
		return position == mDataset.size() ? ItemViewType.TYPE_ITEM_PROGRESS : ItemViewType.TYPE_ITEM_MANGA;
	}

	@Override
	public long getItemId(int position) {
		return position >= mDataset.size() ? 0 : mDataset.get(position).id;
	}

	@Override
	public void onViewRecycled(RecyclerView.ViewHolder holder) {
		if (holder instanceof MangaHeaderHolder) {
			ImageUtils.recycle(((MangaHeaderHolder) holder).imageView);
		}
		super.onViewRecycled(holder);
	}

	@Override
	public void setHasNext(boolean hasNext) {
		mInProgress = hasNext;
	}

	class MangaHeaderHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

		final TextView text1;
		final TextView text2;
		final TextView summary;
		final ImageView imageView;

		MangaHeaderHolder(View itemView) {
			super(itemView);
			text1 = itemView.findViewById(android.R.id.text1);
			text2 = itemView.findViewById(android.R.id.text2);
			summary = itemView.findViewById(android.R.id.summary);
			imageView = itemView.findViewById(R.id.imageView);
			itemView.setOnClickListener(this);
		}

		void bind(MangaHeader item) {
			itemView.setTag(item);
			text1.setText(item.name);
			summary.setText(item.genres);
			LayoutUtils.setTextOrHide(text2, item.summary);
			ImageUtils.setThumbnail(imageView, item.thumbnail, MangaProvider.getDomain(item.provider));
		}

		@Override
		public void onClick(View v) {
			MangaHeader mangaHeader = mDataset.get(getAdapterPosition());
			Context context = v.getContext();
			context.startActivity(new Intent(context.getApplicationContext(), PreviewActivity.class)
					.putExtra("manga", mangaHeader));
		}
	}

	class ProgressHolder extends RecyclerView.ViewHolder {

		final ProgressBar progressBar;

		ProgressHolder(View itemView) {
			super(itemView);
			progressBar = itemView.findViewById(android.R.id.progress);
		}
	}

	@Retention(RetentionPolicy.SOURCE)
	@IntDef({ItemViewType.TYPE_ITEM_MANGA, ItemViewType.TYPE_ITEM_PROGRESS})
	public @interface ItemViewType {
		int TYPE_ITEM_MANGA = 0;
		int TYPE_ITEM_PROGRESS = 1;
	}
}
