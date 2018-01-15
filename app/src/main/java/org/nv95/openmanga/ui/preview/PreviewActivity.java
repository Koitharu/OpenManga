package org.nv95.openmanga.ui.preview;

import android.annotation.SuppressLint;
import android.app.LoaderManager;
import android.content.AsyncTaskLoader;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.nv95.openmanga.R;
import org.nv95.openmanga.content.MangaChapter;
import org.nv95.openmanga.content.MangaDetails;
import org.nv95.openmanga.content.MangaFavourite;
import org.nv95.openmanga.content.MangaHeader;
import org.nv95.openmanga.content.MangaStatus;
import org.nv95.openmanga.content.providers.MangaProvider;
import org.nv95.openmanga.content.storage.db.FavouritesRepository;
import org.nv95.openmanga.content.storage.db.HistoryRepository;
import org.nv95.openmanga.ui.AppBaseActivity;
import org.nv95.openmanga.ui.reader.ReaderActivity;
import org.nv95.openmanga.utils.ImageUtils;
import org.nv95.openmanga.utils.MenuUtils;
import org.nv95.openmanga.utils.TextUtils;

/**
 * Created by koitharu on 26.12.17.
 */

public final class PreviewActivity extends AppBaseActivity implements
		LoaderManager.LoaderCallbacks<MangaDetails>,ChaptersListAdapter.OnChapterClickListener, View.OnClickListener {

	private MangaHeader mMangaHeader;
	@Nullable
	private MangaDetails mMangaDetails = null;

	private ImageView mImageView;
	private RecyclerView mRecyclerViewChapters;
	private TextView mTextVireGenres;
	private TextView mTextViewDescription;
	private TextView mTextViewState;
	private ProgressBar mProgressBar;

	private FavouritesRepository mFavouritesRepository;
	private HistoryRepository mHistoryRepository;

	@SuppressLint("CutPasteId")
	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_preview);
		setSupportActionBar(R.id.toolbar);
		enableHomeAsUp();

		mFavouritesRepository = new FavouritesRepository(this);
		mHistoryRepository = new HistoryRepository(this);

		mImageView = findViewById(R.id.imageView);
		mTextVireGenres = findViewById(R.id.textView_genres);
		mProgressBar = findViewById(R.id.progressBar);
		mTextViewState = findViewById(R.id.textView_state);
		mTextViewDescription = findViewById(R.id.textView_description);
		mRecyclerViewChapters = findViewById(R.id.recyclerView_chapters);

		mRecyclerViewChapters.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));

		mMangaHeader = getIntent().getParcelableExtra("manga");
		updateContent();

		getLoaderManager().initLoader(0, null, this).forceLoad();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.options_preview, menu);
		MenuUtils.buildOpenWithSubmenu(this, mMangaHeader, menu.findItem(R.id.action_open_ext));
		MenuUtils.buildCategoriesSubmenu(this, menu.findItem(R.id.action_favourite));
		//MenuUtils.setRadioCheckable(menu.findItem(R.id.action_favourite), R.id.group_categories);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.action_favourites_remove:
				if (mFavouritesRepository.remove(mMangaHeader)) {
					Snackbar.make(mTextViewDescription, R.string.unfavourited, Snackbar.LENGTH_SHORT).show();
					invalidateOptionsMenu();
					return true;
				}
				return false;
			default:
				if (item.getGroupId() == R.id.group_categories) {
					assert mMangaDetails != null;
					MangaFavourite favourite = MangaFavourite.from(mMangaHeader, item.getItemId(), mMangaDetails.chapters.size());
					if (mFavouritesRepository.add(favourite) || mFavouritesRepository.update(favourite)) {
						Snackbar.make(mTextViewDescription, R.string.favourited, Snackbar.LENGTH_SHORT).show();
						invalidateOptionsMenu();
					} else {
						return false;
					}
					return true;
				}
				return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		return super.onPrepareOptionsMenu(menu);
	}

	@NonNull
	@Override
	public Loader<MangaDetails> onCreateLoader(int i, Bundle bundle) {
		return new MangaLoader(this, mMangaHeader);
	}

	@Override
	public void onLoadFinished(Loader<MangaDetails> loader, MangaDetails mangaDetails) {
		if (mangaDetails == null) {
			mTextViewDescription.setText(R.string.loading_error);
			mProgressBar.setVisibility(View.INVISIBLE);
			return;
		}
		mMangaDetails = mangaDetails;
		updateContent();
		ChaptersListAdapter adapter = new ChaptersListAdapter(this, mangaDetails.chapters, this);
		//TODO history
		mRecyclerViewChapters.setAdapter(adapter);
		mProgressBar.setVisibility(View.INVISIBLE);
	}

	@Override
	public void onLoaderReset(Loader<MangaDetails> loader) {

	}

	private void onUpdateMenu(Menu menu) {
		MangaFavourite favourite = mFavouritesRepository.get(mMangaHeader);
		Menu submenu = menu.findItem(R.id.action_favourite).getSubMenu();
		submenu.findItem(R.id.action_favourites_remove).setVisible(favourite != null);
		menu.findItem(R.id.action_favourite).setIcon(favourite == null ? R.drawable.ic_star_border_white : R.drawable.ic_star_white);
		final int len = submenu.size();
		final int favId = favourite == null ? 0 : favourite.categoryId;
		for (int i = 0;i < len; i++) {
			MenuItem item = submenu.getItem(i);
			item.setChecked(item.isCheckable() && item.getItemId() == favId);
		}

	}

	private void updateContent() {
		if (mMangaDetails == null) { //full info wasn't loaded yet
			ImageUtils.setThumbnail(mImageView, mMangaHeader.thumbnail);
			setTitle(mMangaHeader.name);
			setSubtitle(mMangaHeader.summary);
			mTextVireGenres.setText(mMangaHeader.genres);
			if (mMangaHeader.status == MangaStatus.STATUS_UNKNOWN) {
				mTextViewState.setVisibility(View.GONE);
			} else {
				mTextViewState.setVisibility(View.VISIBLE);
				mTextViewState.setText(mMangaHeader.status == MangaStatus.STATUS_COMPLETED ? R.string.status_completed : R.string.status_ongoing);
			}
		} else {
			ImageUtils.updateImage(mImageView, mMangaDetails.cover);
			setTitle(mMangaDetails.name);
			setSubtitle(mMangaDetails.summary);
			mTextVireGenres.setText(mMangaDetails.genres);
			if (mMangaDetails.status == MangaStatus.STATUS_UNKNOWN) {
				mTextViewState.setVisibility(View.GONE);
			} else {
				mTextViewState.setVisibility(View.VISIBLE);
				mTextViewState.setText(mMangaDetails.status == MangaStatus.STATUS_COMPLETED ? R.string.status_completed : R.string.status_ongoing);
			}
			mTextViewDescription.setText(TextUtils.fromHtmlCompat(mMangaDetails.description));
		}
	}

	@Override
	public void onChapterClick(int pos, MangaChapter chapter) {
		startActivity(new Intent(this, ReaderActivity.class)
		.putExtra("manga", mMangaDetails)
		.putExtra("chapter", chapter));
	}

	@Override
	public boolean onChapterLongClick(int pos, MangaChapter chapter) {
		return false;
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			/*case R.id.fab_read:
				if (mBottomSheetBehavior.getState() == BottomSheetBehavior.STATE_HIDDEN) {
					mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
				} else {
					final MangaHistory history = mHistoryRepository.find(mMangaHeader);
					final Intent intent = new Intent(this, ReaderActivity.class);
					intent.putExtra("manga", mMangaDetails);
					if (history == null) {
						intent.putExtra("chapter", mMangaDetails.chapters.get(0));
					} else {
						intent.putExtra("chapter", mMangaDetails.chapters.findItemById(history.chapterId));
						intent.putExtra("page_id", history.pageId);
					}
					startActivity(intent);
				}
				return;*/
		}
	}

	private static class MangaLoader extends AsyncTaskLoader<MangaDetails> {

		private final MangaProvider mProvider;
		private final MangaHeader mManga;

		MangaLoader(Context context, MangaHeader mangaHeader) {
			super(context);
			mManga = mangaHeader;
			mProvider = MangaProvider.getProvider(context, mangaHeader.provider);
		}

		@Override
		public MangaDetails loadInBackground() {
			try {
				return mProvider.getDetails(mManga);
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		}
	}
}
