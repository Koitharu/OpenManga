package org.nv95.openmanga.preview.bookmarks;

import android.app.LoaderManager;
import android.content.Loader;
import android.os.Bundle;
import android.support.annotation.NonNull;
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
import org.nv95.openmanga.preview.PageHolder;

/**
 * Created by koitharu on 22.01.18.
 */

public final class BookmarksPage extends PageHolder implements LoaderManager.LoaderCallbacks<ListWrapper<MangaBookmark>> {

	public RecyclerView mRecyclerView;
	public TextView mTextViewHolder;

	public BookmarksPage(@NonNull ViewGroup parent) {
		super(parent, R.layout.page_manga_bookmarks);
	}

	@Override
	protected void onViewCreated(@NonNull View view) {
		mRecyclerView = view.findViewById(R.id.recyclerView);
		mRecyclerView.addItemDecoration(new SpaceItemDecoration(ResourceUtils.dpToPx(view.getResources(), 2)));
		mTextViewHolder = view.findViewById(R.id.textView_holder);
	}

	@Override
	public Loader<ListWrapper<MangaBookmark>> onCreateLoader(int id, Bundle args) {
		return new BookmarksLoader(getView().getContext(), BookmarkSpecification.from(args));
	}

	@Override
	public void onLoadFinished(Loader<ListWrapper<MangaBookmark>> loader, ListWrapper<MangaBookmark> data) {
		if (data.isSuccess()) {
			mRecyclerView.setAdapter(new BookmarksAdapter(data.get()));
			mTextViewHolder.setVisibility(data.isEmpty() ? View.VISIBLE : View.GONE);
		}
	}

	@Override
	public void onLoaderReset(Loader<ListWrapper<MangaBookmark>> loader) {

	}
}
