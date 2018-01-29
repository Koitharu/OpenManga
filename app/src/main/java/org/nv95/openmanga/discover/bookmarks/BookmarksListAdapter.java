package org.nv95.openmanga.discover.bookmarks;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.IntDef;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import org.nv95.openmanga.R;
import org.nv95.openmanga.common.DataViewHolder;
import org.nv95.openmanga.common.utils.ImageUtils;
import org.nv95.openmanga.common.utils.ResourceUtils;
import org.nv95.openmanga.core.models.MangaBookmark;
import org.nv95.openmanga.core.models.MangaHeader;
import org.nv95.openmanga.core.models.UniqueObject;
import org.nv95.openmanga.core.storage.files.ThumbnailsStorage;
import org.nv95.openmanga.preview.PreviewActivity;
import org.nv95.openmanga.reader.ReaderActivity;
import org.nv95.openmanga.reader.ToolButtonCompat;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;

/**
 * Created by koitharu on 29.01.18.
 */

final class BookmarksListAdapter extends RecyclerView.Adapter<DataViewHolder<? extends UniqueObject>> {

	private final ArrayList<UniqueObject> mDataset;
	private final ThumbnailsStorage mThumbStore;

	BookmarksListAdapter(ArrayList<UniqueObject> dataset, ThumbnailsStorage thumbnailsStorage) {
		mDataset = dataset;
		mThumbStore = thumbnailsStorage;
		setHasStableIds(true);
	}

	@Override
	public DataViewHolder<? extends UniqueObject> onCreateViewHolder(ViewGroup parent, @ItemViewType int viewType) {
		final LayoutInflater inflater = LayoutInflater.from(parent.getContext());
		switch (viewType) {
			case ItemViewType.TYPE_ITEM_BOOKMARK:
				return new BookmarkHolder(inflater.inflate(R.layout.item_bookmark_grid, parent, false));
			case ItemViewType.TYPE_ITEM_HEADER:
				return new HeaderHolder(inflater.inflate(R.layout.header_group_button, parent, false));
			default:
				throw new RuntimeException("Invalid ItemViewType");
		}
	}

	@Override
	public void onBindViewHolder(DataViewHolder<? extends UniqueObject> holder, int position) {
		if (holder instanceof HeaderHolder) {
			((HeaderHolder) holder).bind((MangaHeader) mDataset.get(position));
		} else if (holder instanceof BookmarkHolder) {
			((BookmarkHolder) holder).bind((MangaBookmark) mDataset.get(position));
		}
	}

	@Override
	public int getItemCount() {
		return mDataset.size();
	}

	@Override
	@ItemViewType
	public int getItemViewType(int position) {
		return mDataset.get(position) instanceof MangaHeader ? ItemViewType.TYPE_ITEM_HEADER : ItemViewType.TYPE_ITEM_BOOKMARK;
	}

	@Override
	public long getItemId(int position) {
		return mDataset.get(position).getId();
	}

	static class HeaderHolder extends DataViewHolder<MangaHeader> implements View.OnClickListener {

		private final TextView mTextView;
		private final Button mButtonMore;

		HeaderHolder(View itemView) {
			super(itemView);
			mTextView = itemView.findViewById(R.id.textView);
			mButtonMore = itemView.findViewById(R.id.button_more);
			mButtonMore.setOnClickListener(this);
		}

		@Override
		public void bind(MangaHeader mangaHeader) {
			super.bind(mangaHeader);
			mTextView.setText(mangaHeader.name);
		}

		@Override
		public void onClick(View v) {
			final MangaHeader data = getData();
			if (data == null) {
				return;
			}
			final Context context = v.getContext();
			context.startActivity(new Intent(context.getApplicationContext(), PreviewActivity.class)
					.putExtra("manga", data));
		}
	}

	class BookmarkHolder extends DataViewHolder<MangaBookmark> implements View.OnClickListener,
			View.OnLongClickListener, PopupMenu.OnMenuItemClickListener {

		private final ImageView mImageView;
		private final TextView mTextView;
		private final ToolButtonCompat mToolButtonMenu;
		private final PopupMenu mPopupMenu;

		BookmarkHolder(View itemView) {
			super(itemView);
			mTextView = itemView.findViewById(R.id.textView);
			mImageView = itemView.findViewById(R.id.imageView);
			mToolButtonMenu = itemView.findViewById(R.id.toolButton_menu);
			mPopupMenu = new PopupMenu(itemView.getContext(), mToolButtonMenu);
			mPopupMenu.inflate(R.menu.popup_bookmark);
			mPopupMenu.setOnMenuItemClickListener(this);
			itemView.setOnClickListener(this);
			itemView.setOnLongClickListener(this);
			mToolButtonMenu.setOnClickListener(this);
		}

		@Override
		public void bind(MangaBookmark bookmark) {
			super.bind(bookmark);
			ImageUtils.setThumbnail(mImageView, mThumbStore.getFile(bookmark));
			mTextView.setText(ResourceUtils.formatTimeRelative(bookmark.createdAt));
		}

		@Override
		public void recycle() {
			ImageUtils.recycle(mImageView);
			super.recycle();
		}

		@Override
		public void onClick(View view) {
			switch (view.getId()) {
				case R.id.toolButton_menu:
					mPopupMenu.show();
					break;
				default:
					final Context context = view.getContext();
					context.startActivity(new Intent(context, ReaderActivity.class)
							.setAction(ReaderActivity.ACTION_BOOKMARK_OPEN)
							.putExtra("bookmark", getData()));
			}
		}

		@Override
		public boolean onMenuItemClick(MenuItem item) {
			switch (item.getItemId()) {
				default:
					return false;
			}
		}

		@Override
		public boolean onLongClick(View v) {
			onClick(mToolButtonMenu);
			return true;
		}
	}

	@Retention(RetentionPolicy.SOURCE)
	@IntDef({ItemViewType.TYPE_ITEM_BOOKMARK, ItemViewType.TYPE_ITEM_HEADER})
	public @interface ItemViewType {
		int TYPE_ITEM_BOOKMARK = 0;
		int TYPE_ITEM_HEADER = 1;
	}
}
