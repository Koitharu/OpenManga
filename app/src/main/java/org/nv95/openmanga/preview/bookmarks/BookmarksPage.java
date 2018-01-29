package org.nv95.openmanga.preview.bookmarks;

import android.app.LoaderManager;
import android.content.Loader;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.nv95.openmanga.R;
import org.nv95.openmanga.common.utils.ResourceUtils;
import org.nv95.openmanga.common.views.recyclerview.SpaceItemDecoration;
import org.nv95.openmanga.core.ListWrapper;
import org.nv95.openmanga.core.models.MangaBookmark;
import org.nv95.openmanga.core.storage.db.BookmarkSpecification;
import org.nv95.openmanga.core.storage.files.ThumbnailsStorage;
import org.nv95.openmanga.discover.bookmarks.BookmarkRemoveTask;
import org.nv95.openmanga.discover.bookmarks.BookmarksLoader;
import org.nv95.openmanga.preview.PageHolder;

import java.util.ArrayList;

/**
 * Created by koitharu on 22.01.18.
 */

public final class BookmarksPage extends PageHolder implements LoaderManager.LoaderCallbacks<ListWrapper<MangaBookmark>>,
		BookmarkRemoveTask.OnBookmarkRemovedListener {

	public RecyclerView mRecyclerView;
	private TextView mTextViewHolder;
	private final ArrayList<MangaBookmark> mDataset = new ArrayList<>();
	private final BookmarksAdapter mAdapter;


	public BookmarksPage(@NonNull ViewGroup parent) {
		super(parent, R.layout.page_manga_bookmarks);
		mAdapter = new BookmarksAdapter(mDataset, new ThumbnailsStorage(parent.getContext()));
		mRecyclerView.setAdapter(mAdapter);
	}

	@Override
	protected void onViewCreated(@NonNull View view) {
		mRecyclerView = view.findViewById(R.id.recyclerView);
		mRecyclerView.addItemDecoration(new SpaceItemDecoration(ResourceUtils.dpToPx(view.getResources(), 1)));
		mRecyclerView.setLayoutManager(new GridLayoutManager(view.getContext(), 3));
		mTextViewHolder = view.findViewById(R.id.textView_holder);
	}

	@Override
	public Loader<ListWrapper<MangaBookmark>> onCreateLoader(int id, Bundle args) {
		return new BookmarksLoader(getView().getContext(), BookmarkSpecification.from(args));
	}

	@Override
	public void onLoadFinished(Loader<ListWrapper<MangaBookmark>> loader, ListWrapper<MangaBookmark> data) {
		if (data.isSuccess()) {
			mDataset.clear();
			mDataset.addAll(data.get());
			mTextViewHolder.setVisibility(data.isEmpty() ? View.VISIBLE : View.GONE);
		}
	}

	@Override
	public void onLoaderReset(Loader<ListWrapper<MangaBookmark>> loader) {

	}

	@Override
	public void onBookmarkRemoved(@NonNull MangaBookmark bookmark) {
		mDataset.remove(bookmark);
		mAdapter.notifyDataSetChanged();
		Snackbar.make(mRecyclerView, R.string.bookmark_removed, Snackbar.LENGTH_SHORT).show();
	}
}
