package org.nv95.openmanga.ui;

import android.annotation.SuppressLint;
import android.app.LoaderManager;
import android.content.AsyncTaskLoader;
import android.content.Context;
import android.content.Loader;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
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
import org.nv95.openmanga.ui.common.SimpleViewPagerAdapter;
import org.nv95.openmanga.utils.ImageUtils;
import org.nv95.openmanga.utils.MenuUtils;
import org.nv95.openmanga.utils.TextUtils;

/**
 * Created by koitharu on 26.12.17.
 */

public final class PreviewActivity extends AppBaseActivity implements AppBarLayout.OnOffsetChangedListener, LoaderManager.LoaderCallbacks<MangaDetails>,ChaptersListAdapter.OnChapterClickListener {

	private boolean mToolbarCollapsed = false;
	private MangaHeader mMangaHeader;
	@Nullable
	private MangaDetails mMangaDetails = null;

	private TabLayout mTabLayout;
	private ImageView mImageView;
	private RecyclerView mRecyclerViewChapters;
	private RecyclerView mRecyclerViewBookmarks;
	private TextView mTextViewChaptersHolder;
	private TextView mTextViewBookmarksHolder;
	private TextView mTextViewSummary;
	private TextView mTextViewDescription;
	private TextView mTextViewState;
	private TextView mTextViewTitle;
	private ProgressBar mProgressBar;
	private ViewPager mViewPager;
	private Toolbar mToolbarMenu;

	private FavouritesRepository mFavouritesRepository;
	private HistoryRepository mHistoryRepository;

	@SuppressLint("CutPasteId")
	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_preview);
		setSupportActionBar(R.id.toolbar);
		enableHomeAsUp();
		disableTitle();

		mFavouritesRepository = new FavouritesRepository(this);
		mHistoryRepository = new HistoryRepository(this);

		mImageView = findViewById(R.id.imageView);
		mTabLayout = findViewById(R.id.tabs);
		mTextViewSummary = findViewById(R.id.textView_summary);
		mTextViewTitle = findViewById(R.id.textView_title);
		mProgressBar = findViewById(R.id.progressBar);
		mTextViewState = findViewById(R.id.textView_state);
		mViewPager = findViewById(R.id.pager);
		mToolbarMenu = findViewById(R.id.toolbarMenu);
		AppBarLayout appBar = findViewById(R.id.appbar_container);
		if (appBar != null) {
			appBar.addOnOffsetChangedListener(this);
		}
		final SimpleViewPagerAdapter adapter = new SimpleViewPagerAdapter();
		//Page 0 - description
		View page = getLayoutInflater().inflate(R.layout.page_text, mViewPager, false);
		mTextViewDescription = page.findViewById(R.id.textView);
		adapter.addView(page, getString(R.string.description));
		//Page 1 - chapters
		page = LayoutInflater.from(this).inflate(R.layout.page_list_fastscroll, mViewPager, false);
		mRecyclerViewChapters = page.findViewById(R.id.recyclerView);
		mTextViewChaptersHolder = page.findViewById(R.id.textView_holder);
		mRecyclerViewChapters.setLayoutManager(new LinearLayoutManager(this));
		mRecyclerViewChapters.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));
		mTextViewChaptersHolder.setText(R.string.no_chapters_found);
		adapter.addView(page, getString(R.string.chapters));
		//Page 3 - bookmarks
		page = LayoutInflater.from(this).inflate(R.layout.page_list, mViewPager, false);
		mRecyclerViewBookmarks = page.findViewById(R.id.recyclerView);
		mTextViewBookmarksHolder = page.findViewById(R.id.textView_holder);
		mRecyclerViewBookmarks.setLayoutManager(new LinearLayoutManager(this));
		mTextViewBookmarksHolder.setText(R.string.no_bookmarks_tip);
		adapter.addView(page, getString(R.string.bookmarks));

		mViewPager.setAdapter(adapter);
		mTabLayout.setupWithViewPager(mViewPager);
		mToolbarMenu.inflateMenu(R.menu.options_preview_toolbar);
		mToolbarMenu.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
			@Override
			public boolean onMenuItemClick(MenuItem item) {
				return PreviewActivity.this.onOptionsItemSelected(item);
			}
		});

		mMangaHeader = getIntent().getParcelableExtra("manga");
		updateContent();
		//init toolbar menu
		final Menu menu = mToolbarMenu.getMenu();
		MenuUtils.buildOpenWithSubmenu(this, mMangaHeader, menu.findItem(R.id.action_open_ext));
		MenuUtils.buildCategoriesSubmenu(this, menu.findItem(R.id.action_favourite));
		//MenuUtils.setRadioCheckable(menu.findItem(R.id.action_favourite), R.id.group_categories);
		invalidateMenuBar();

		getLoaderManager().initLoader(0, null, this);
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
	public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
		if (verticalOffset <= appBarLayout.getTotalScrollRange() / -2) {
			if (!mToolbarCollapsed) {
				mToolbarCollapsed = true;
				invalidateOptionsMenu();
			}
		} else {
			if (mToolbarCollapsed) {
				mToolbarCollapsed = false;
				invalidateOptionsMenu();
				invalidateMenuBar();
			}
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.action_favourites_remove:
				if (mFavouritesRepository.remove(mMangaHeader)) {
					Snackbar.make(mViewPager, R.string.unfavourited, Snackbar.LENGTH_SHORT).show();
					invalidateOptionsMenu();
					invalidateMenuBar();
					return true;
				}
				return false;
			default:
				if (item.getGroupId() == R.id.group_categories) {
					assert mMangaDetails != null;
					MangaFavourite favourite = MangaFavourite.from(mMangaHeader, item.getItemId(), mMangaDetails.chapters.size());
					if (mFavouritesRepository.add(favourite) || mFavouritesRepository.update(favourite)) {
						Snackbar.make(mViewPager, R.string.favourited, Snackbar.LENGTH_SHORT).show();
						invalidateOptionsMenu();
						invalidateMenuBar();
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
		if (mToolbarCollapsed) {
			menu.setGroupVisible(R.id.group_all, true);
			onUpdateMenu(menu);
		} else {
			menu.setGroupVisible(R.id.group_all, false);
		}
		return super.onPrepareOptionsMenu(menu);
	}

	@NonNull
	@Override
	public Loader<MangaDetails> onCreateLoader(int i, Bundle bundle) {
		MangaLoader loader = new MangaLoader(this, mMangaHeader);
		if (mRecyclerViewChapters.getAdapter() == null) {
			loader.forceLoad();
		}
		return loader;
	}

	@Override
	public void onLoadFinished(Loader<MangaDetails> loader, MangaDetails mangaDetails) {
		if (mangaDetails == null) {
			mTextViewChaptersHolder.setText(R.string.loading_error);
			mTextViewDescription.setText(R.string.loading_error);
			mProgressBar.setVisibility(View.GONE);
			return;
		}
		mMangaDetails = mangaDetails;
		updateContent();
		ChaptersListAdapter adapter = new ChaptersListAdapter(mangaDetails.chapters, this);
		//TODO history
		mRecyclerViewChapters.setAdapter(adapter);
		mProgressBar.setVisibility(View.GONE);
	}

	@Override
	public void onLoaderReset(Loader<MangaDetails> loader) {

	}

	private void invalidateMenuBar() {
		if (!mToolbarCollapsed) {
			onUpdateMenu(mToolbarMenu.getMenu());
		}
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
			mTextViewTitle.setText(mMangaHeader.name);
			setTitle(mMangaHeader.name);
			mTextViewSummary.setText(mMangaHeader.genres);
			if (mMangaHeader.status == MangaStatus.STATUS_UNKNOWN) {
				mTextViewState.setVisibility(View.GONE);
			} else {
				mTextViewState.setVisibility(View.VISIBLE);
				mTextViewState.setText(mMangaHeader.status == MangaStatus.STATUS_COMPLETED ? R.string.status_completed : R.string.status_ongoing);
			}
		} else {
			ImageUtils.updateImage(mImageView, mMangaDetails.cover);
			mTextViewTitle.setText(mMangaDetails.name);
			setTitle(mMangaDetails.name);
			mTextViewSummary.setText(mMangaDetails.genres);
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
		//TODO
	}

	@Override
	public boolean onChapterLongClick(int pos, MangaChapter chapter) {
		return false;
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
