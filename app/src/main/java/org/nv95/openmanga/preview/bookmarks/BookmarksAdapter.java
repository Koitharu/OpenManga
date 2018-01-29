package org.nv95.openmanga.preview.bookmarks;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.nv95.openmanga.R;
import org.nv95.openmanga.common.utils.ImageUtils;
import org.nv95.openmanga.common.utils.ResourceUtils;
import org.nv95.openmanga.core.models.MangaBookmark;
import org.nv95.openmanga.core.storage.files.ThumbnailsStorage;
import org.nv95.openmanga.reader.ReaderActivity;
import org.nv95.openmanga.reader.ToolButtonCompat;

import java.util.ArrayList;

/**
 * Created by koitharu on 22.01.18.
 */

public final class BookmarksAdapter extends RecyclerView.Adapter<BookmarksAdapter.BookmarkHolder> {

	private final ArrayList<MangaBookmark> mDataset;
	private final ThumbnailsStorage mThumbStore;

	BookmarksAdapter(ArrayList<MangaBookmark> dataset, ThumbnailsStorage thumbnailsStorage) {
		mDataset = dataset;
		mThumbStore = thumbnailsStorage;
		setHasStableIds(true);
	}

	@NonNull
	@Override
	public BookmarkHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		return new BookmarkHolder(LayoutInflater.from(parent.getContext())
				.inflate(R.layout.item_bookmark_grid, parent, false));
	}

	@Override
	public void onBindViewHolder(BookmarkHolder holder, int position) {
		final MangaBookmark item = mDataset.get(position);
		ImageUtils.setThumbnail(holder.imageView, mThumbStore.getFile(item));
		holder.textView.setText(ResourceUtils.formatTimeRelative(item.createdAt));
	}

	@Override
	public int getItemCount() {
		return mDataset.size();
	}

	@Override
	public long getItemId(int position) {
		return mDataset.get(position).id;
	}

	class BookmarkHolder extends RecyclerView.ViewHolder implements View.OnClickListener, PopupMenu.OnMenuItemClickListener, View.OnLongClickListener {

		final ImageView imageView;
		final TextView textView;
		final ToolButtonCompat toolButtonMenu;
		final PopupMenu popupMenu;

		BookmarkHolder(View itemView) {
			super(itemView);
			textView = itemView.findViewById(R.id.textView);
			imageView = itemView.findViewById(R.id.imageView);
			toolButtonMenu = itemView.findViewById(R.id.toolButton_menu);
			popupMenu = new PopupMenu(itemView.getContext(), toolButtonMenu);
			popupMenu.inflate(R.menu.popup_bookmark);
			popupMenu.setOnMenuItemClickListener(this);
			itemView.setOnClickListener(this);
			itemView.setOnLongClickListener(this);
			toolButtonMenu.setOnClickListener(this);
		}

		@Override
		public void onClick(View view) {
			switch (view.getId()) {
				case R.id.toolButton_menu:
					popupMenu.show();
					break;
				default:
					final Context context = view.getContext();
					context.startActivity(new Intent(context, ReaderActivity.class)
							.setAction(ReaderActivity.ACTION_BOOKMARK_OPEN)
							.putExtra("bookmark", mDataset.get(getAdapterPosition())));
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
			onClick(toolButtonMenu);
			return true;
		}
	}
}
