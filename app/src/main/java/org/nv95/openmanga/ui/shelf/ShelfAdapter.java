package org.nv95.openmanga.ui.shelf;

import android.support.annotation.IntDef;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


import org.nv95.openmanga.R;
import org.nv95.openmanga.content.MangaHeader;
import org.nv95.openmanga.ui.common.ListHeader;
import org.nv95.openmanga.ui.views.AspectRatioImageView;
import org.nv95.openmanga.utils.ImageUtils;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;

/**
 * Created by koitharu on 21.12.17.
 */

public final class ShelfAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

	private final ArrayList<Object> mDataset;

	public ShelfAdapter() {
		mDataset = new ArrayList<>();
		setHasStableIds(true);
	}

	void updateData(ArrayList<Object> data) {
		mDataset.clear();
		mDataset.addAll(data);
		notifyDataSetChanged();
	}

	@Override
	public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, @ShelfItemType int viewType) {
		final LayoutInflater inflater = LayoutInflater.from(parent.getContext());
		switch (viewType) {
			case ShelfItemType.TYPE_HEADER:
				return new HeaderHolder(inflater.inflate(R.layout.header_group, parent, false));
			case ShelfItemType.TYPE_ITEM_DEFAULT:
				return new MangaHolder(inflater.inflate(R.layout.item_manga, parent, false));
			case ShelfItemType.TYPE_ITEM_SMALL:
				throw new AssertionError("Unknown viewType");
			case ShelfItemType.TYPE_TIP:
				throw new AssertionError("Unknown viewType");
			default:
				throw new AssertionError("Unknown viewType");
		}
	}

	@Override
	public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
		if (holder instanceof HeaderHolder) {
			ListHeader item = (ListHeader) mDataset.get(position);
			if (item.text != null) {
				((HeaderHolder) holder).textView.setText(item.text);
			} else if (item.textResId != 0) {
				((HeaderHolder) holder).textView.setText(item.textResId);
			} else {
				((HeaderHolder) holder).textView.setText(null);
			}
		} else if (holder instanceof MangaHolder) {
			MangaHeader item = (MangaHeader) mDataset.get(position);
			ImageUtils.setThumbnail(((MangaHolder) holder).imageViewThumbnail, item.thumbnail);
			((MangaHolder) holder).textViewTitle.setText(item.name);
		}

	}

	@ShelfItemType
	@Override
	public int getItemViewType(int position) {
		Object item = mDataset.get(position);
		if (item instanceof ListHeader) {
			return ShelfItemType.TYPE_HEADER;
		} else if (item instanceof MangaHeader) {
			return ShelfItemType.TYPE_ITEM_DEFAULT;
		} else {
			throw new AssertionError("Unknown viewType");
		}
	}

	@Override
	public int getItemCount() {
		return mDataset.size();
	}

	class HeaderHolder extends RecyclerView.ViewHolder {

		private final TextView textView;

		HeaderHolder(View itemView) {
			super(itemView);
			textView = itemView.findViewById(R.id.textView);
		}
	}

	class MangaHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

		private final AspectRatioImageView imageViewThumbnail;
		private final TextView textViewTitle;

		MangaHolder(View itemView) {
			super(itemView);
			imageViewThumbnail = itemView.findViewById(R.id.imageViewThumbnail);
			textViewTitle = itemView.findViewById(R.id.textViewTitle);
			itemView.setOnClickListener(this);
		}

		@Override
		public void onClick(View view) {

		}
	}
}
