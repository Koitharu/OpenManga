package org.nv95.openmanga.discover.bookmarks;

import android.app.LoaderManager;
import android.content.Loader;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.nv95.openmanga.AppBaseActivity;
import org.nv95.openmanga.R;
import org.nv95.openmanga.common.utils.AnimationUtils;
import org.nv95.openmanga.common.utils.ErrorUtils;
import org.nv95.openmanga.common.utils.MetricsUtils;
import org.nv95.openmanga.common.utils.ResourceUtils;
import org.nv95.openmanga.common.views.recyclerview.SpaceItemDecoration;
import org.nv95.openmanga.core.ListWrapper;
import org.nv95.openmanga.core.models.MangaBookmark;
import org.nv95.openmanga.core.models.UniqueObject;
import org.nv95.openmanga.core.storage.db.BookmarkSpecification;
import org.nv95.openmanga.core.storage.files.ThumbnailsStorage;

import java.util.ArrayList;

/**
 * Created by koitharu on 29.01.18.
 */

public final class BookmarksListActivity extends AppBaseActivity implements LoaderManager.LoaderCallbacks<ListWrapper<MangaBookmark>>,
		BookmarkRemoveTask.OnBookmarkRemovedListener {

	private RecyclerView mRecyclerView;
	private ProgressBar mProgressBar;
	private TextView mTextViewHolder;

	private BookmarksListAdapter mAdapter;
	private ArrayList<UniqueObject> mDataset;

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_bookmarks);
		setSupportActionBar(R.id.toolbar);
		enableHomeAsUp();

		mProgressBar = findViewById(R.id.progressBar);
		mRecyclerView = findViewById(R.id.recyclerView);
		mTextViewHolder = findViewById(R.id.textView_holder);
		mRecyclerView.setHasFixedSize(true);

		final int spans = MetricsUtils.getPreferredColumnsCountMedium(getResources());
		final GridLayoutManager layoutManager = new GridLayoutManager(this, spans);
		mRecyclerView.addItemDecoration(new SpaceItemDecoration(ResourceUtils.dpToPx(getResources(), 1)));
		mRecyclerView.setLayoutManager(layoutManager);

		mDataset = new ArrayList<>();
		mAdapter = new BookmarksListAdapter(mDataset, new ThumbnailsStorage(this));
		mRecyclerView.setAdapter(mAdapter);
		layoutManager.setSpanSizeLookup(new BookmarkSpanSizeLookup(mAdapter, spans));

		getLoaderManager().initLoader(0, new BookmarkSpecification().orderByMangaAndDate(true).toBundle(), this).forceLoad();
	}

	@Override
	public Loader<ListWrapper<MangaBookmark>> onCreateLoader(int id, Bundle args) {
		return new BookmarksLoader(this, BookmarkSpecification.from(args));
	}

	@Override
	public void onLoadFinished(Loader<ListWrapper<MangaBookmark>> loader, ListWrapper<MangaBookmark> result) {
		mProgressBar.setVisibility(View.GONE);
		if (result.isSuccess()) {
			final ArrayList<MangaBookmark> list = result.get();
			mDataset.clear();
			long prevId = 0;
			for (MangaBookmark o : list) {
				if (prevId != o.manga.id) {
					mDataset.add(o.manga);
				}
				prevId = o.manga.id;
				mDataset.add(o);
			}
			mAdapter.notifyDataSetChanged();
			AnimationUtils.setVisibility(mTextViewHolder, mDataset.isEmpty() ? View.VISIBLE : View.GONE);
		} else {
			Snackbar.make(mRecyclerView, ErrorUtils.getErrorMessage(result.getError()), Toast.LENGTH_SHORT).show();
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
