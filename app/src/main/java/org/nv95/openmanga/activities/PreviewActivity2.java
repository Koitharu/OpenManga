package org.nv95.openmanga.activities;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
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
import org.nv95.openmanga.adapters.BookmarksAdapter;
import org.nv95.openmanga.adapters.ChaptersAdapter;
import org.nv95.openmanga.adapters.OnChapterClickListener;
import org.nv95.openmanga.adapters.SimpleViewPagerAdapter;
import org.nv95.openmanga.dialogs.ChaptersSelectDialog;
import org.nv95.openmanga.dialogs.MenuDialog;
import org.nv95.openmanga.helpers.ContentShareHelper;
import org.nv95.openmanga.helpers.MangaSaveHelper;
import org.nv95.openmanga.items.Bookmark;
import org.nv95.openmanga.items.MangaChapter;
import org.nv95.openmanga.items.MangaInfo;
import org.nv95.openmanga.items.MangaSummary;
import org.nv95.openmanga.lists.ChaptersList;
import org.nv95.openmanga.providers.BookmarksProvider;
import org.nv95.openmanga.providers.FavouritesProvider;
import org.nv95.openmanga.providers.HistoryProvider;
import org.nv95.openmanga.providers.LocalMangaProvider;
import org.nv95.openmanga.providers.MangaProvider;
import org.nv95.openmanga.providers.NewChaptersProvider;
import org.nv95.openmanga.providers.staff.MangaProviderManager;
import org.nv95.openmanga.services.ExportService;
import org.nv95.openmanga.utils.AnimUtils;
import org.nv95.openmanga.utils.ChangesObserver;
import org.nv95.openmanga.utils.ImageUtils;
import org.nv95.openmanga.utils.MangaStore;
import org.nv95.openmanga.utils.NetworkUtils;
import org.nv95.openmanga.utils.ProgressAsyncTask;
import org.nv95.openmanga.utils.WeakAsyncTask;

import java.util.Collections;
import java.util.List;

import static org.nv95.openmanga.R.string.bookmarks;
import static org.nv95.openmanga.R.string.description;

/**
 * Created by unravel22 on 18.02.17.
 */

public class PreviewActivity2 extends BaseAppActivity implements BookmarksAdapter.OnBookmarkClickListener,
        OnChapterClickListener, AppBarLayout.OnOffsetChangedListener, ChangesObserver.OnMangaChangesListener {

    private MangaSummary mManga;
    private boolean mToolbarCollapsed = false;

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

    private SimpleViewPagerAdapter mPagerAdapter;
    private ChaptersAdapter mChaptersAdapter;

    @SuppressLint("CutPasteId")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preview2);
        setSupportActionBar(R.id.toolbar);
        enableHomeAsUp();
        disableTitle();

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
        mPagerAdapter = new SimpleViewPagerAdapter();
        //
        View page = LayoutInflater.from(this).inflate(R.layout.page_text, mViewPager, false);
        mTextViewDescription = page.findViewById(R.id.textView);
        mPagerAdapter.addView(page, getString(description));
        //
        page = LayoutInflater.from(this).inflate(R.layout.page_list, mViewPager, false);
        mRecyclerViewChapters = page.findViewById(R.id.recyclerView);
        mTextViewChaptersHolder = page.findViewById(R.id.textView_holder);
        mRecyclerViewChapters.setLayoutManager(new LinearLayoutManager(this));
        mTextViewChaptersHolder.setText(R.string.no_chapters_found);
        mPagerAdapter.addView(page, getString(R.string.chapters));
        //
        page = LayoutInflater.from(this).inflate(R.layout.page_list, mViewPager, false);
        mRecyclerViewBookmarks = page.findViewById(R.id.recyclerView);
        mTextViewBookmarksHolder = page.findViewById(R.id.textView_holder);
        mRecyclerViewBookmarks.setLayoutManager(new LinearLayoutManager(this));
        mTextViewBookmarksHolder.setText(R.string.no_bookmarks_tip);
        mPagerAdapter.addView(page, getString(bookmarks));

        mViewPager.setAdapter(mPagerAdapter);
        mTabLayout.setupWithViewPager(mViewPager);
        mChaptersAdapter = new ChaptersAdapter(this);
        mChaptersAdapter.setOnItemClickListener(this);
        mRecyclerViewChapters.setAdapter(mChaptersAdapter);
        mToolbarMenu.inflateMenu(R.menu.toolbar_actions);
        mToolbarMenu.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                return PreviewActivity2.this.onOptionsItemSelected(item);
            }
        });

        MangaInfo mangaInfo = new MangaInfo(getIntent().getExtras());
        if (mangaInfo.provider != LocalMangaProvider.class && !NetworkUtils.checkConnection(this)) {
            mManga = LocalMangaProvider.getInstance(this).getLocalManga(mangaInfo);
            Snackbar.make(mViewPager, R.string.no_network_connection, Snackbar.LENGTH_SHORT).show();
        } else {
            mManga = new MangaSummary(mangaInfo);
        }
        ImageUtils.setImage(mImageView, mManga.preview);
        mTextViewTitle.setText(mManga.name);
        mTextViewSummary.setText(mManga.genres);
        mViewPager.setCurrentItem(HistoryProvider.getInstance(this).has(mManga) ? 1 : 0, false);
        switch (LocalMangaProvider.class.equals(mManga.provider) ? MangaInfo.STATUS_UNKNOWN : mManga.status) {
            case MangaInfo.STATUS_COMPLETED:
                mTextViewState.setText(R.string.status_completed);
                break;
            case MangaInfo.STATUS_ONGOING:
                mTextViewState.setText(R.string.status_ongoing);
                break;
            default:
                mTextViewState.setVisibility(View.GONE);
        }
        invalidateMenuBar();
        ChangesObserver.getInstance().addListener(this);
        new LoadTask(this).attach(this).start();

        new ContentShareHelper(this).buildOpenWithSubmenu(mManga,
                mToolbarMenu.getMenu().findItem(R.id.action_open_ext));
    }

    @Override
    protected void onDestroy() {
        ChangesObserver.getInstance().removeListener(this);
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.preview2, menu);
        new ContentShareHelper(this).buildOpenWithSubmenu(mManga,
                menu.findItem(R.id.action_open_ext));
        return super.onCreateOptionsMenu(menu);
    }

    public void invalidateMenuBar() {
        if (mToolbarCollapsed) {
            return;
        }
        Menu menu = mToolbarMenu.getMenu();
        boolean isLocal = LocalMangaProvider.class.equals(mManga.provider);
        menu.findItem(R.id.action_save).setVisible(!isLocal);
        menu.findItem(R.id.action_remove).setVisible(isLocal);
        menu.findItem(R.id.action_export).setVisible(isLocal);
        menu.findItem(R.id.action_sort).setIcon(mChaptersAdapter.isReversed() ? R.drawable.ic_sort_ascending_white : R.drawable.ic_sort_descending_white);
        menu.findItem(R.id.action_save_more).setVisible(isLocal && mManga.status == MangaInfo.STATUS_ONGOING);
        if (isLocal) {
            menu.findItem(R.id.action_favourite).setVisible(false);
        } else if (FavouritesProvider.getInstance(this).has(mManga)) {
            menu.findItem(R.id.action_favourite).setIcon(R.drawable.ic_favorite_light);
            menu.findItem(R.id.action_favourite).setTitle(R.string.action_unfavourite);
        } else {
            menu.findItem(R.id.action_favourite).setIcon(R.drawable.ic_favorite_outline_light);
            menu.findItem(R.id.action_favourite).setTitle(R.string.action_favourite);
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (mToolbarCollapsed) {
            menu.setGroupVisible(R.id.group_all, true);
            boolean isLocal = LocalMangaProvider.class.equals(mManga.provider);
            menu.findItem(R.id.action_save).setVisible(!isLocal);
            menu.findItem(R.id.action_remove).setVisible(isLocal);
            menu.findItem(R.id.action_export).setVisible(isLocal);
            menu.findItem(R.id.action_save_more).setVisible(isLocal && mManga.status == MangaInfo.STATUS_ONGOING);
            if (isLocal) {
                menu.findItem(R.id.action_favourite).setVisible(false);
            } else if (FavouritesProvider.getInstance(this).has(mManga)) {
                menu.findItem(R.id.action_favourite).setTitle(R.string.action_unfavourite);
            } else {
                menu.findItem(R.id.action_favourite).setTitle(R.string.action_favourite);
            }
        } else {
            menu.setGroupVisible(R.id.group_all, false);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_favourite:
                final FavouritesProvider favouritesProvider = FavouritesProvider.getInstance(this);
                FavouritesProvider.dialog(this, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == DialogInterface.BUTTON_NEUTRAL) {
                            if (favouritesProvider.remove(mManga)) {
                                ChangesObserver.getInstance().emitOnFavouritesChanged(mManga, -1);
                                Snackbar.make(mViewPager, R.string.unfavourited, Snackbar.LENGTH_SHORT).show();
                            }
                        } else {
                            NewChaptersProvider.getInstance(PreviewActivity2.this)
                                    .storeChaptersCount(mManga.id, mManga.getChapters().size());
                            ChangesObserver.getInstance().emitOnFavouritesChanged(mManga, which);
                            Snackbar.make(mViewPager, R.string.favourited, Snackbar.LENGTH_SHORT).show();
                        }
                    }
                }, mManga);
                return true;
            case R.id.action_save:
                if (mManga.chapters.size() != 0) {
                    new MangaSaveHelper(this).confirmSave(mManga);
                }
                return true;
            case R.id.action_share:
                new ContentShareHelper(this).share(mManga);
                return true;
            case R.id.action_sort:
                mChaptersAdapter.reverse();
                item.setIcon(mChaptersAdapter.isReversed() ? R.drawable.ic_sort_ascending_white : R.drawable.ic_sort_descending_white);
                return true;
            case R.id.action_relative:
                startActivity(new Intent(this, SearchActivity.class)
                        .putExtra("query", mManga.name));
                return true;
            case R.id.action_shortcut:
                new ContentShareHelper(this).createShortcut(mManga);
                return true;
            case R.id.action_export:
                ExportService.start(this, mManga);
                return true;
            case R.id.action_remove:
                deleteDialog();
                return true;
            case R.id.action_save_more:
                if (checkConnectionWithSnackbar(mTextViewDescription)) {
                    new LoadSourceTask(this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, mManga);
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBookmarkSelected(Bookmark bookmark) {
        HistoryProvider.getInstance(this).add(mManga, bookmark.chapter, 0);
        startActivity(new Intent(this, ReadActivity2.class).putExtra("chapter", bookmark.chapter).putExtra("page", bookmark.page).putExtras(mManga.toBundle()));
    }

    private void deleteDialog() {
        new ChaptersSelectDialog(this)
                .showRemove(mManga, new ChaptersSelectDialog.OnChaptersRemoveListener() {
                    @Override
                    public void onChaptersRemove(@Nullable long[] ids) {
                        if (ids == null) {
                            if (new MangaStore(PreviewActivity2.this).dropMangas(new long[]{mManga.id})) {
                                HistoryProvider.getInstance(PreviewActivity2.this).remove(new long[]{mManga.id});
                                ChangesObserver.getInstance().emitOnLocalChanged(mManga.id, null);
                                finish();
                            }
                        } else {
                            if (new MangaStore(PreviewActivity2.this).dropChapters(mManga.id, ids)) {
                                Snackbar.make(mTextViewDescription, getString(R.string.chapters_removed, ids.length), Snackbar.LENGTH_SHORT).show();
                            } else {
                                Snackbar.make(mTextViewDescription, R.string.error, Snackbar.LENGTH_SHORT).show();
                            }
                            new LoadTask(PreviewActivity2.this).attach(PreviewActivity2.this).start();
                        }
                    }
                });
    }

    @Override
    public void onChapterClick(int pos, MangaChapter chapter) {
        if (pos == -1) {
            Intent intent = new Intent(this, ReadActivity2.class);
            intent.putExtras(mManga.toBundle());
            HistoryProvider.HistorySummary hs = HistoryProvider.getInstance(this).get(mManga);
            if (hs != null) {
                int index = mManga.chapters.indexByNumber(hs.getChapter());
                if (index != -1) {
                    intent.putExtra("chapter", index);
                    intent.putExtra("page", hs.getPage());
                }
            }
            startActivity(intent);
        } else {
            if (mChaptersAdapter.isReversed()) pos = mManga.chapters.size() - pos - 1;
            HistoryProvider.getInstance(this).add(mManga, chapter.number, 0);
            startActivity(new Intent(this, ReadActivity2.class).putExtra("chapter", pos).putExtras(mManga.toBundle()));
        }
    }

    @Override
    public boolean onChapterLongClick(int pos, MangaChapter chapter) {
        if (pos == -1 || mManga.provider == LocalMangaProvider.class) {
            return false;
        } else {
            new MenuDialog(this, R.menu.chapter, chapter.name)
                    .setOnItemClickListener(new ChapterMenuListener(chapter))
                    .show();
            return true;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mChaptersAdapter.getItemCount() != 0) {
            mChaptersAdapter.setExtra(HistoryProvider.getInstance(PreviewActivity2.this).get(mManga));
            mChaptersAdapter.notifyDataSetChanged();
        }
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
    public void onLocalChanged(int id, @Nullable MangaInfo manga) {
        if (id == mManga.id && manga == null) {
            finish();
        }
    }

    @Override
    public void onFavouritesChanged(@NonNull MangaInfo manga, int category) {
        invalidateMenuBar();
        invalidateOptionsMenu();
    }

    @Override
    public void onHistoryChanged(@NonNull MangaInfo manga) {

    }

    private static class LoadTask extends WeakAsyncTask<PreviewActivity2, Void, List<Bookmark>, MangaSummary> {

        LoadTask(PreviewActivity2 object) {
            super(object);
        }

        @Override
        protected void onPreExecute(@NonNull PreviewActivity2 activity) {
            AnimUtils.crossfade(null, activity.mProgressBar);
        }

        @SuppressWarnings("ConstantConditions")
        @Override
        protected MangaSummary doInBackground(Void... params) {
            try {
                //noinspection unchecked
                publishProgress(BookmarksProvider.getInstance(getObject()).getAll(getObject().mManga.id));
                MangaProvider provider = MangaProviderManager.instanceProvider(getObject(), getObject().mManga.provider);
                return provider.getDetailedInfo(getObject().mManga);
            } catch (Exception e) {
                return null;
            }
        }

        @Override
        protected void onProgressUpdate(@NonNull PreviewActivity2 activity, List<Bookmark>[] values) {
            activity.mRecyclerViewBookmarks.setAdapter(new BookmarksAdapter(values[0], activity));
            if (values[0].isEmpty()) {
                activity.mTextViewBookmarksHolder.setText(R.string.no_bookmarks_tip);
                activity.mTextViewBookmarksHolder.setVisibility(View.VISIBLE);
            }
        }

        @Override
        protected void onPostExecute(@NonNull PreviewActivity2 activity, MangaSummary mangaSummary) {
            if (mangaSummary != null) {
                activity.mManga = mangaSummary;
                activity.invalidateOptionsMenu();
                activity.invalidateMenuBar();
                activity.mTextViewSummary.setText(activity.mManga.genres);
                activity.mTextViewDescription.setText(activity.mManga.description);
                ImageUtils.updateImage(activity.mImageView, activity.mManga.preview);
                activity.mChaptersAdapter.setData(activity.mManga.chapters);
                activity.mChaptersAdapter.setExtra(HistoryProvider.getInstance(activity).get(activity.mManga));
                activity.mChaptersAdapter.notifyDataSetChanged();
                if (mangaSummary.chapters.isEmpty()) {
                    activity.mTextViewChaptersHolder.setText(R.string.no_chapters_found);
                    AnimUtils.crossfade(activity.mProgressBar, activity.mTextViewChaptersHolder);
                } else {
                    AnimUtils.crossfade(activity.mProgressBar, null);
                    if (!activity.showcase(activity.mToolbarMenu.findViewById(R.id.action_favourite), R.string.action_favourite, R.string.tip_favourite)) {
                        if (LocalMangaProvider.class.equals(activity.mManga.provider)) {
                            activity.showcase(activity.mToolbarMenu.findViewById(R.id.action_save_more), R.string.action_save_add, R.string.tip_save_more);
                        } else {
                            activity.showcase(activity.mToolbarMenu.findViewById(R.id.action_save), R.string.save_manga, R.string.tip_save);
                        }
                    }
                }
            } else {
                activity.mTextViewChaptersHolder.setText(R.string.loading_error);
                AnimUtils.crossfade(activity.mProgressBar, activity.mTextViewChaptersHolder);
                activity.mTextViewDescription.setText(R.string.loading_error);
            }
        }
    }

    private static class LoadSourceTask extends ProgressAsyncTask<MangaInfo, Void, MangaSummary> implements DialogInterface.OnCancelListener {

        LoadSourceTask(PreviewActivity2 object) {
            super(object);
        }

        @Override
        protected MangaSummary doInBackground(MangaInfo... params) {
            return LocalMangaProvider.getInstance(getActivity())
                    .getSource(params[0]);
        }

        @Override
        protected void onPostExecute(@NonNull BaseAppActivity activity, MangaSummary sourceManga) {
            PreviewActivity2 a = (PreviewActivity2) activity;
            if (sourceManga == null) {
                Snackbar.make(a.mViewPager, R.string.loading_error, Snackbar.LENGTH_SHORT)
                        .show();
                return;
            }
            ChaptersList newChapters = sourceManga.chapters.complementByName(a.mManga.chapters);
            if (sourceManga.chapters.size() <= a.mManga.chapters.size()) {
                Snackbar.make(a.mViewPager, R.string.no_new_chapters, Snackbar.LENGTH_SHORT)
                        .show();
            } else {
                sourceManga.chapters = newChapters;
                new MangaSaveHelper(a).confirmSave(sourceManga, R.string.action_save_add);
            }
        }
    }

    private class ChapterMenuListener implements MenuItem.OnMenuItemClickListener {

        private final MangaChapter mChapter;

        ChapterMenuListener(MangaChapter chapter) {
            mChapter = chapter;
        }

        @Override
        public boolean onMenuItemClick(MenuItem item) {
            switch (item.getItemId()) {
                case R.id.action_save:
                    new MangaSaveHelper(PreviewActivity2.this)
                            .save(mManga, mChapter);
                    return true;
                case R.id.action_save_prev:
                    new MangaSaveHelper(PreviewActivity2.this)
                            .save(mManga, mManga.chapters.first(), mChapter);
                    return true;
                case R.id.action_save_next:
                    new MangaSaveHelper(PreviewActivity2.this)
                            .save(mManga, mChapter, mManga.chapters.last());
                    return true;
                default:
                    return false;
            }
        }
    }
}
