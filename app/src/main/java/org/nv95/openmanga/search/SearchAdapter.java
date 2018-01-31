package org.nv95.openmanga.search;

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
import org.nv95.openmanga.common.utils.ImageUtils;
import org.nv95.openmanga.common.utils.LayoutUtils;
import org.nv95.openmanga.common.views.EndlessRecyclerView;
import org.nv95.openmanga.core.models.MangaHeader;
import org.nv95.openmanga.core.providers.MangaProvider;
import org.nv95.openmanga.preview.PreviewActivity;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;

/**
 * Created by koitharu on 07.01.18.
 */

public final class SearchAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements
		EndlessRecyclerView.EndlessAdapter, View.OnClickListener {

	private final ArrayList<Object> mDataset;
	private boolean mHasNext = true;

	public SearchAdapter(ArrayList<Object> dataset) {
		mDataset = dataset;
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
			case ItemViewType.TYPE_ITEM_SUBHEADER:
				return new HeaderHolder(inflater.inflate(R.layout.header_group, parent, false));
		}
		holder.itemView.setOnClickListener(this);
		return holder;
	}

	@Override
	public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
		if (holder instanceof MangaHeaderHolder) {
			final MangaHeader item = (MangaHeader) mDataset.get(position);
			((MangaHeaderHolder) holder).text1.setText(item.name);
			LayoutUtils.setTextOrHide(((MangaHeaderHolder) holder).text2, item.summary);
			((MangaHeaderHolder) holder).summary.setText(item.genres);
			ImageUtils.setThumbnail(((MangaHeaderHolder) holder).imageView, item.thumbnail, MangaProvider.getDomain(item.provider));
			holder.itemView.setTag(item);
		} else if (holder instanceof ProgressHolder) {
			((ProgressHolder) holder).progressBar.setVisibility(mHasNext ? View.VISIBLE : View.INVISIBLE);
		} else if (holder instanceof HeaderHolder) {
			String item = (String) mDataset.get(position);
			((HeaderHolder) holder).textView.setText(item);
		}
	}

	@Override
	public void setHasNext(boolean hasNext) {
		mHasNext = hasNext;
		if (mDataset.size() != 0) {
			notifyItemChanged(mDataset.size());
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
		if (position == mDataset.size()) {
			return ItemViewType.TYPE_ITEM_PROGRESS;
		} else {
			return mDataset.get(position) instanceof MangaHeader ? ItemViewType.TYPE_ITEM_MANGA : ItemViewType.TYPE_ITEM_SUBHEADER;
		}
	}

	@Override
	public long getItemId(int position) {
		if (position == mDataset.size()) {
			return 0;
		} else {
			final Object item = mDataset.get(position);
			return item instanceof MangaHeader ? ((MangaHeader) item).id : item.hashCode();
		}
	}

	@Override
	public void onClick(View view) {
		MangaHeader mangaHeader = (MangaHeader) view.getTag();
		Context context = view.getContext();
		context.startActivity(new Intent(context.getApplicationContext(), PreviewActivity.class)
				.putExtra("manga", mangaHeader));
	}

	final class MangaHeaderHolder extends RecyclerView.ViewHolder {

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
		}
	}

	final class ProgressHolder extends RecyclerView.ViewHolder {

		final ProgressBar progressBar;

		ProgressHolder(View itemView) {
			super(itemView);
			progressBar = itemView.findViewById(android.R.id.progress);
		}
	}

	final class HeaderHolder extends RecyclerView.ViewHolder {

		final TextView textView;

		HeaderHolder(View itemView) {
			super(itemView);
			textView = itemView.findViewById(R.id.textView);
		}
	}

	@Retention(RetentionPolicy.SOURCE)
	@IntDef({ItemViewType.TYPE_ITEM_MANGA, ItemViewType.TYPE_ITEM_PROGRESS, ItemViewType.TYPE_ITEM_SUBHEADER})
	public @interface ItemViewType {
		int TYPE_ITEM_MANGA = 0;
		int TYPE_ITEM_PROGRESS = 1;
		int TYPE_ITEM_SUBHEADER = 2;
	}
}
