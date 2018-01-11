package org.nv95.openmanga.ui.shelf;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import org.nv95.openmanga.R;
import org.nv95.openmanga.content.MangaFavourite;
import org.nv95.openmanga.content.MangaHeader;
import org.nv95.openmanga.content.MangaHistory;
import org.nv95.openmanga.ui.PreviewActivity;
import org.nv95.openmanga.ui.common.ListHeader;
import org.nv95.openmanga.utils.ImageUtils;
import org.nv95.openmanga.utils.ResourceUtils;

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
				return new HeaderHolder(inflater.inflate(R.layout.header_group_button, parent, false));
			case ShelfItemType.TYPE_ITEM_DEFAULT:
				return new MangaHolder(inflater.inflate(R.layout.item_manga, parent, false));
			case ShelfItemType.TYPE_RECENT:
				return new RecentHolder(inflater.inflate(R.layout.item_recent, parent, false));
			case ShelfItemType.TYPE_ITEM_SMALL:
				return new MangaHolder(inflater.inflate(R.layout.item_manga_small, parent, false));
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
			holder.itemView.setTag(item);
			if (holder instanceof RecentHolder) {
				MangaHistory history = (MangaHistory) item;
				((RecentHolder) holder).textViewSubtitle.setText(item.summary);
				((RecentHolder) holder).textViewStatus.setText(ResourceUtils.formatTimeRelative(history.updatedAt));
			}
		}

	}

	@ShelfItemType
	@Override
	public int getItemViewType(int position) {
		Object item = mDataset.get(position);
		if (item instanceof ListHeader) {
			return ShelfItemType.TYPE_HEADER;
		} else if (item instanceof MangaHistory) {
			return ShelfItemType.TYPE_RECENT;
		} else if (item instanceof MangaFavourite) {
			return ShelfItemType.TYPE_ITEM_DEFAULT;
		} else if (item instanceof MangaHeader) {
			return ShelfItemType.TYPE_ITEM_SMALL;
		} else {
			throw new AssertionError("Unknown viewType");
		}
	}

	@Override
	public int getItemCount() {
		return mDataset.size();
	}

	@Override
	public long getItemId(int position) {
		Object item = mDataset.get(position);
		if (item instanceof MangaHeader) {
			return ((MangaHeader) item).id;
		} else if (item instanceof ListHeader) {
			final String text = ((ListHeader) item).text;
			return text != null ? text.hashCode() : ((ListHeader) item).textResId;
		} else {
			throw new AssertionError("Unknown viewType");
		}
	}

	class HeaderHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

		final TextView textView;
		final Button buttonMore;

		HeaderHolder(View itemView) {
			super(itemView);
			textView = itemView.findViewById(R.id.textView);
			buttonMore = itemView.findViewById(R.id.button_more);
			buttonMore.setOnClickListener(this);
		}

		@Override
		public void onClick(View v) {
			switch (v.getId()) {
				//todo
			}
		}
	}

	class MangaHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

		final ImageView imageViewThumbnail;
		final TextView textViewTitle;

		MangaHolder(View itemView) {
			super(itemView);
			imageViewThumbnail = itemView.findViewById(R.id.imageViewThumbnail);
			textViewTitle = itemView.findViewById(R.id.textViewTitle);
			itemView.setOnClickListener(this);
		}

		@Override
		public void onClick(View view) {
			MangaHeader mangaHeader = (MangaHeader) itemView.getTag();
			Context context = view.getContext();
			context.startActivity(new Intent(context.getApplicationContext(), PreviewActivity.class)
					.putExtra("manga", mangaHeader));
		}
	}

	class RecentHolder extends MangaHolder {

		final TextView textViewStatus;
		final TextView textViewSubtitle;

		RecentHolder(View itemView) {
			super(itemView);
			textViewStatus = itemView.findViewById(R.id.textView_status);
			textViewSubtitle = itemView.findViewById(R.id.textView_subtitle);
			itemView.findViewById(R.id.button_continue).setOnClickListener(this);
		}

		@Override
		public void onClick(View view) {
			if (view.getId() == R.id.button_continue) {

			} else {
				super.onClick(view);
			}
		}
	}
}
