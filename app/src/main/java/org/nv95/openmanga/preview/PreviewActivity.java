package org.nv95.openmanga.preview;

import android.app.LoaderManager;
import android.content.Intent;
import android.content.Loader;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.View;
import android.widget.ProgressBar;

import org.nv95.openmanga.AppBaseActivity;
import org.nv95.openmanga.R;
import org.nv95.openmanga.common.dialogs.FavouriteDialog;
import org.nv95.openmanga.common.dialogs.MenuDialog;
import org.nv95.openmanga.core.ObjectWrapper;
import org.nv95.openmanga.core.models.Category;
import org.nv95.openmanga.core.models.MangaBookmark;
import org.nv95.openmanga.core.models.MangaChapter;
import org.nv95.openmanga.core.models.MangaDetails;
import org.nv95.openmanga.core.models.MangaFavourite;
import org.nv95.openmanga.core.models.MangaHeader;
import org.nv95.openmanga.core.models.MangaHistory;
import org.nv95.openmanga.core.storage.db.BookmarkSpecification;
import org.nv95.openmanga.core.storage.db.FavouritesRepository;
import org.nv95.openmanga.core.storage.db.HistoryRepository;
import org.nv95.openmanga.discover.bookmarks.BookmarkRemoveTask;
import org.nv95.openmanga.preview.bookmarks.BookmarksPage;
import org.nv95.openmanga.preview.chapters.ChaptersListAdapter;
import org.nv95.openmanga.preview.chapters.ChaptersPage;
import org.nv95.openmanga.preview.details.DetailsPage;
import org.nv95.openmanga.reader.ReaderActivity;
import org.nv95.openmanga.storage.SaveRequest;
import org.nv95.openmanga.storage.SaveService;

/**
 * Created by koitharu on 26.12.17.
 */

public final class PreviewActivity extends AppBaseActivity implements LoaderManager.LoaderCallbacks<ObjectWrapper<MangaDetails>>,
		ChaptersListAdapter.OnChapterClickListener, View.OnClickListener, BookmarkRemoveTask.OnBookmarkRemovedListener,
		FavouriteDialog.OnFavouriteListener, MenuDialog.OnMenuItemClickListener<MangaChapter> {

	public static final String ACTION_PREVIEW = "org.nv95.openmanga.ACTION_PREVIEW";

	//views
	//activity
	private ViewPager mPager;
	private ProgressBar mProgressBar;
	private TabLayout mTabs;
	//tabs
	private DetailsPage mDetailsPage;
	private ChaptersPage mChaptersPage;
	private BookmarksPage mBookmarksPage;
	//data
	private MangaHeader mMangaHeader;
	@Nullable
	private MangaDetails mMangaDetails;
	private HistoryRepository mHistory;
	private FavouritesRepository mFavourites;

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_preview);
		setSupportActionBar(R.id.toolbar);
		enableHomeAsUp();

		mPager = findViewById(R.id.pager);
		mProgressBar = findViewById(R.id.progressBar);
		mTabs = findViewById(R.id.tabs);

		final PagesAdapter adapter = new PagesAdapter(
				mDetailsPage = new DetailsPage(mPager),
				mChaptersPage = new ChaptersPage(mPager),
				mBookmarksPage = new BookmarksPage(mPager)
		);
		mPager.setAdapter(adapter);
		mTabs.setupWithViewPager(mPager);

		mDetailsPage.buttonRead.setOnClickListener(this);
		mDetailsPage.buttonFavourite.setOnClickListener(this);

		mMangaHeader = getIntent().getParcelableExtra("manga");
		mMangaDetails = null;
		assert mMangaHeader != null;
		mHistory = HistoryRepository.get(this);
		mFavourites = FavouritesRepository.get(this);

		setTitle(mMangaHeader.name);
		setSubtitle(mMangaHeader.summary);
		mDetailsPage.updateContent(mMangaHeader, mMangaDetails);

		Bundle args = new Bundle(1);
		args.putParcelable("manga", mMangaHeader);
		getLoaderManager().initLoader(0, args, this).forceLoad();
		getLoaderManager().initLoader(1, new BookmarkSpecification().manga(mMangaHeader).orderByDate(true).toBundle(), mBookmarksPage).forceLoad();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.options_preview, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	protected void onResume() {
		super.onResume();
		final MangaHistory history = mHistory.find(mMangaHeader);
		if (history != null) {
			mChaptersPage.updateHistory(history);
			mDetailsPage.buttonRead.setText(R.string._continue);
		}
		final MangaFavourite favourite = mFavourites.get(mMangaHeader);
		mDetailsPage.buttonFavourite.setImageResource(favourite == null ? R.drawable.ic_tag_black : R.drawable.ic_tag_heart_black);
	}

	@NonNull
	@Override
	public Loader<ObjectWrapper<MangaDetails>> onCreateLoader(int id, Bundle args) {
		return new MangaDetailsLoader(this, args.<MangaHeader>getParcelable("manga"));
	}

	@Override
	public void onLoadFinished(Loader<ObjectWrapper<MangaDetails>> loader, ObjectWrapper<MangaDetails> data) {
		mProgressBar.setVisibility(View.GONE);
		if (data.isSuccess()) {
			mMangaDetails = data.get();
			assert mMangaDetails != null;
			setTitle(mMangaDetails.name);
			setSubtitle(mMangaDetails.summary);
			final MangaHistory history = mHistory.find(mMangaHeader);
			/*if (history != null) {
				int pos = CollectionsUtils.findChapterPositionById(mMangaDetails.chapters, history.chapterId);
				if (pos != -1 && LayoutUtils.findLastVisibleItemPosition(mRecyclerView) > pos) {
					mRecyclerView.scrollToPosition(Math.max(0, pos - 3));
				}
			}*/
			mDetailsPage.updateContent(mMangaHeader, mMangaDetails);
			mChaptersPage.setData(mMangaDetails.chapters, history, this);
		} else {
			mChaptersPage.setError();
			mDetailsPage.setError(data.getError());
		}
	}

	@Override
	public void onLoaderReset(Loader<ObjectWrapper<MangaDetails>> loader) {

	}

	@Override
	public void onChapterClick(int pos, MangaChapter chapter) {
		startActivity(new Intent(this, ReaderActivity.class)
				.putExtra("manga", mMangaDetails)
				.putExtra("chapter", chapter));
	}

	@Override
	public boolean onChapterLongClick(int pos, MangaChapter chapter) {
		final int totalChapters = mMangaDetails == null ? 0 : mMangaDetails.chapters.size();
		MenuDialog<MangaChapter> menu = new MenuDialog<MangaChapter>(this)
				.setTitle(chapter.name)
				.setItemClickListener(this)
				.addItem(R.id.action_chapter_save_this, R.string.save_this_chapter);
		if (pos > 0) {
			menu.addItem(R.id.action_chapter_save_prev, R.string.save_prev_chapters);
		}
		if (pos + 5 < totalChapters) {
				menu.addItem(R.id.action_chapter_save_5, R.string.save_next_5_chapters);
				if (pos + 10 < totalChapters) {
					menu.addItem(R.id.action_chapter_save_10, R.string.save_next_10_chapters);
					if (pos + 30 < totalChapters) {
						menu.addItem(R.id.action_chapter_save_30, R.string.save_next_30_chapters);
					}
				}
		}
		if (pos < totalChapters - 1) {
			menu.addItem(R.id.action_chapter_save_next, R.string.save_next_all_chapters);
		}
		menu.addItem(R.id.action_chapter_save_all, R.string.save_all_chapters);
		menu.create(chapter)
				.show();
		return true;
	}


	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.button_read:
				startActivity(new Intent(this, ReaderActivity.class)
						.setAction(ReaderActivity.ACTION_READING_CONTINUE)
						.putExtra("manga", mMangaDetails));
				break;
			case R.id.button_favourite:
				if (mMangaDetails != null) {
					new FavouriteDialog(this, mMangaDetails)
							.setListener(this)
							.show();
				}
				break;
		}
	}

	@Override
	public void onFavouritesChanged(MangaDetails manga, @Nullable Category category) {
		mDetailsPage.buttonFavourite.setImageResource(category == null ? R.drawable.ic_tag_black : R.drawable.ic_tag_heart_black);
	}

	@Override
	public void onMenuItemClick(@IdRes int id, MangaChapter mangaChapter) {
		if (mMangaDetails == null) {
			return;
		}
		switch (id) {
			case R.id.action_chapter_save_this:
				SaveService.start(this, new SaveRequest(mMangaDetails, mangaChapter));
				break;
			case R.id.action_chapter_save_5:
				SaveService.start(this, new SaveRequest(mMangaDetails, mMangaDetails.chapters.subListFrom(mangaChapter, 5)));
				break;
			case R.id.action_chapter_save_10:
				SaveService.start(this, new SaveRequest(mMangaDetails, mMangaDetails.chapters.subListFrom(mangaChapter, 10)));
				break;
			case R.id.action_chapter_save_30:
				SaveService.start(this, new SaveRequest(mMangaDetails, mMangaDetails.chapters.subListFrom(mangaChapter, 30)));
				break;
			case R.id.action_chapter_save_all:
				SaveService.start(this, new SaveRequest(mMangaDetails, mMangaDetails.chapters));
				break;
			case R.id.action_chapter_save_next:
				SaveService.start(this, new SaveRequest(mMangaDetails, mMangaDetails.chapters.subListFrom(mangaChapter)));
				break;
			case R.id.action_chapter_save_prev:
				SaveService.start(this, new SaveRequest(mMangaDetails, mMangaDetails.chapters.subListTo(mangaChapter)));
				break;
			default:
				stub();
		}
	}

	@Override
	public void onBookmarkRemoved(@NonNull MangaBookmark bookmark) {
		mBookmarksPage.onBookmarkRemoved(bookmark);
	}
}
