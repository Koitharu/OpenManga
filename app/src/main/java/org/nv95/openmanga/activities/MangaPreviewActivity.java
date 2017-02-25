package org.nv95.openmanga.activities;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v4.util.Pair;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.text.format.Formatter;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.nv95.openmanga.R;
import org.nv95.openmanga.dialogs.BottomSheet;
import org.nv95.openmanga.dialogs.ChaptersSelectDialog;
import org.nv95.openmanga.items.MangaChapter;
import org.nv95.openmanga.items.MangaInfo;
import org.nv95.openmanga.items.MangaSummary;
import org.nv95.openmanga.lists.ChaptersList;
import org.nv95.openmanga.providers.FavouritesProvider;
import org.nv95.openmanga.providers.HistoryProvider;
import org.nv95.openmanga.providers.LocalMangaProvider;
import org.nv95.openmanga.providers.MangaProvider;
import org.nv95.openmanga.providers.NewChaptersProvider;
import org.nv95.openmanga.providers.staff.MangaProviderManager;
import org.nv95.openmanga.services.DownloadService;
import org.nv95.openmanga.utils.ChangesObserver;
import org.nv95.openmanga.utils.ImageUtils;
import org.nv95.openmanga.utils.MangaStore;
import org.nv95.openmanga.utils.NetworkUtils;
import org.nv95.openmanga.utils.StorageUtils;

import java.io.File;

/**
 * Created by nv95 on 30.09.15.
 */
@Deprecated
public class MangaPreviewActivity extends BaseAppActivity implements View.OnClickListener,
        DialogInterface.OnClickListener, View.OnLongClickListener, ChangesObserver.OnMangaChangesListener {

    //data
    private MangaSummary mMangaSummary;
    //views
    private FloatingActionButton mFab;
    private ImageView mImageView;
    private ProgressBar mProgressBar;
    private TextView mTextViewSummary;
    private TextView mTextViewDescription;
    private TextView mTextViewExtra;
    private CollapsingToolbarLayout mCollapsingToolbarLayout;
    private AppBarLayout mAppBarLayout;
    private View mViewGenres;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preview);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        enableHomeAsUp();
        enableTransparentStatusBar(android.R.color.transparent);
        mMangaSummary = new MangaSummary(new MangaInfo(getIntent().getExtras()));
        mCollapsingToolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.collapsing_container);
        mAppBarLayout = (AppBarLayout) findViewById(R.id.appbar_container);
        mImageView = (ImageView) findViewById(R.id.imageView);
        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);
        mFab = (FloatingActionButton) findViewById(R.id.fab_read);
        mTextViewSummary = (TextView) findViewById(R.id.textView_summary);
        mTextViewExtra = (TextView) findViewById(R.id.textView_extra);
        mViewGenres = findViewById(R.id.tvGenreTitle);
        mTextViewDescription = (TextView) findViewById(R.id.textView_description);
        mCollapsingToolbarLayout.setTitle(mMangaSummary.name);

        mFab.setOnClickListener(this);
        mFab.setOnLongClickListener(this);
        mImageView.setColorFilter(ContextCompat.getColor(this, R.color.preview_filter));
        ImageUtils.setImage(mImageView, mMangaSummary.preview);

        if (savedInstanceState != null && savedInstanceState.containsKey("chapters")) {
            mMangaSummary = new MangaSummary(savedInstanceState);
            mProgressBar.setVisibility(View.GONE);
            mTextViewDescription.setText(mMangaSummary.getDescription());
            ImageUtils.setImage(mImageView, mMangaSummary.preview);
            if (mMangaSummary.getChapters().size() == 0) {
                mFab.setEnabled(false);
                noChaptersSnackbar();
            } else {
                mFab.setEnabled(true);
            }
        } else {
            new LoadInfoTask().startLoading();
        }

        if(TextUtils.isEmpty(mMangaSummary.genres)){
            mViewGenres.setVisibility(View.GONE);
            mTextViewSummary.setVisibility(View.GONE);
        } else {
            mTextViewSummary.setText(mMangaSummary.genres);
        }
        ChangesObserver.getInstance().addListener(this);
    }

    private void noChaptersSnackbar() {
        Snackbar.make(mAppBarLayout, R.string.no_chapters_found, Snackbar.LENGTH_INDEFINITE)
                .setAction(R.string.action_search, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        startActivity(new Intent(MangaPreviewActivity.this, SearchActivity.class)
                                .putExtra("query", mMangaSummary.name));
                    }
                })
                .show();
    }

    @Override
    protected void onDestroy() {
        ChangesObserver.getInstance().removeListener(this);
        super.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mMangaSummary != null) {
            outState.putAll(mMangaSummary.toBundle());
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.fab_read:
                showChaptersSheet();
                break;
            case R.id.snackbar_action:
                new LoadInfoTask().startLoading();
                break;
        }
    }

    @Override
    public boolean onLongClick(View v) {
        switch (v.getId()) {
            case R.id.fab_read:
                Intent intent = new Intent(this, ReadActivity2.class);
                intent.putExtras(mMangaSummary.toBundle());
                HistoryProvider.HistorySummary hs = HistoryProvider.getInstance(this).get(mMangaSummary);
                if (hs != null) {
                    int index = mMangaSummary.chapters.indexByNumber(hs.getChapter());
                    if (index != -1) {
                        intent.putExtra("chapter", index);
                        intent.putExtra("page", hs.getPage());
                    }
                }
                startActivity(intent);
                return true;
            default:
                return false;
        }
    }

    private void showChaptersSheet() {
        if (mMangaSummary.getChapters().size() == 0) {
            return;
        }
        HistoryProvider.HistorySummary lastChapter = HistoryProvider.getInstance(this).get(mMangaSummary);
        BottomSheet sheet = new BottomSheet(this);
        sheet.setItems(mMangaSummary.getChapters().getNames(), android.R.layout.simple_list_item_1);
        if (lastChapter != null) {
            MangaChapter c = mMangaSummary.chapters.getByNumber(lastChapter.getChapter());
            if (c != null) {
                sheet.addHeader(
                        c.name,
                        R.string.continue_reading, 0, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                onLongClick(mFab);
                            }
                        }
                );
            }
        }
        sheet
                //.setSheetTitle(R.string.chapters_list)
                .setOnItemClickListener(this)
                .show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.preview, menu);
        if (LocalMangaProvider.class.equals(mMangaSummary.provider)) {
            menu.findItem(R.id.action_save).setVisible(false);
            menu.findItem(R.id.action_remove).setVisible(true);
            menu.findItem(R.id.action_save_more).setVisible(mMangaSummary.status == MangaInfo.STATUS_ONGOING);
            menu.findItem(R.id.action_favourite).setVisible(false);
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (FavouritesProvider.getInstance(this).has(mMangaSummary)) {
            menu.findItem(R.id.action_favourite).setIcon(R.drawable.ic_favorite_light);
            menu.findItem(R.id.action_favourite).setTitle(R.string.action_unfavourite);
        } else {
            menu.findItem(R.id.action_favourite).setIcon(R.drawable.ic_favorite_outline_light);
            menu.findItem(R.id.action_favourite).setTitle(R.string.action_favourite);
        }
        menu.findItem(R.id.action_save_more)
                .setVisible(LocalMangaProvider.class.equals(mMangaSummary.provider) &&
                        mMangaSummary.status == MangaInfo.STATUS_ONGOING);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_favourite:
                FavouritesProvider favouritesProvider = FavouritesProvider.getInstance(this);
                if (favouritesProvider.has(mMangaSummary)) {
                    if (favouritesProvider.remove(mMangaSummary)) {
                        ChangesObserver.getInstance().emitOnFavouritesChanged(mMangaSummary, -1);
                    }
                } else {
                    FavouritesProvider.dialog(this, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            NewChaptersProvider.getInstance(MangaPreviewActivity.this)
                                    .storeChaptersCount(mMangaSummary.id, mMangaSummary.getChapters().size());
                            ChangesObserver.getInstance().emitOnFavouritesChanged(mMangaSummary, which);
                        }
                    }, mMangaSummary);
                }
                return true;
            case R.id.action_save:
                if (mMangaSummary.chapters.size() != 0) {
                    DownloadService.start(this, mMangaSummary);
                }
                return true;
            case R.id.action_remove:
                deleteDialog();
                return true;
            case R.id.action_save_more:
                if (checkConnectionWithSnackbar(mTextViewDescription)) {
                    new LoadSourceTask().startLoading(mMangaSummary);
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void deleteDialog() {
        new ChaptersSelectDialog(this)
                .showRemove(mMangaSummary, new ChaptersSelectDialog.OnChaptersRemoveListener() {
                    @Override
                    public void onChaptersRemove(@Nullable long[] ids) {
                        if (ids == null) {
                            if (new MangaStore(MangaPreviewActivity.this).dropMangas(new long[]{mMangaSummary.id})) {
                                HistoryProvider.getInstance(MangaPreviewActivity.this).remove(new long[]{mMangaSummary.id});
                                ChangesObserver.getInstance().emitOnLocalChanged(mMangaSummary.id, null);
                                finish();
                            }
                        } else {
                            if (new MangaStore(MangaPreviewActivity.this).dropChapters(mMangaSummary.id, ids)) {
                                Snackbar.make(mTextViewDescription, getString(R.string.chapters_removed, ids.length), Snackbar.LENGTH_SHORT).show();
                            } else {
                                Snackbar.make(mTextViewDescription, R.string.error, Snackbar.LENGTH_SHORT).show();
                            }
                            new LoadInfoTask().startLoading();
                        }
                    }
                });
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        dialog.dismiss();
        HistoryProvider.getInstance(this).add(mMangaSummary, mMangaSummary.chapters.get(which).number, 0);
        startActivity(new Intent(this, ReadActivity2.class).putExtra("chapter", which).putExtras(mMangaSummary.toBundle()));
    }

    @Override
    public void onLocalChanged(int id, @Nullable MangaInfo manga) {
        if (id == mMangaSummary.id && manga == null) {
            finish();
        }
    }

    @Override
    public void onFavouritesChanged(@NonNull MangaInfo manga, int category) {
        invalidateOptionsMenu();
    }

    @Override
    public void onHistoryChanged(@NonNull MangaInfo manga) {

    }

    private class LoadInfoTask extends LoaderTask<Void, Void, Pair<MangaSummary, String>> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mProgressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected void onPostExecute(Pair<MangaSummary, String> result) {
            super.onPostExecute(result);
            mProgressBar.setVisibility(View.GONE);
            mFab.setEnabled(true);
            if (result == null) {
                mFab.hide();
                Snackbar.make(mAppBarLayout, NetworkUtils.checkConnection(MangaPreviewActivity.this) ?
                        R.string.loading_error : R.string.no_network_connection, Snackbar.LENGTH_INDEFINITE)
                        .setAction(R.string.retry, MangaPreviewActivity.this)
                        .show();
                return;
            }
            MangaPreviewActivity.this.mMangaSummary = result.first;
            mTextViewDescription.setText(mMangaSummary.getDescription());
            mTextViewExtra.setText(result.second);
            ImageUtils.updateImage(mImageView, mMangaSummary.preview);
            if (mMangaSummary.getChapters().size() == 0) {
                mFab.setEnabled(false);
                noChaptersSnackbar();
            }
            invalidateOptionsMenu();
            if (showcase(R.id.action_favourite, R.string.action_favourite, R.string.tip_favourite)) {

            } else {
                if (LocalMangaProvider.class.equals(mMangaSummary.provider)) {
                    showcase(R.id.action_save_more, R.string.action_save_add,  R.string.tip_save_more);
                } else {
                    showcase(R.id.action_save, R.string.save_manga, R.string.tip_save);
                }
            }
        }

        @Override
        protected Pair<MangaSummary, String> doInBackground(Void... params) {
            try {
                MangaProvider provider = MangaProviderManager.instanceProvider(MangaPreviewActivity.this, mMangaSummary.provider);
                MangaSummary ms = provider.getDetailedInfo(mMangaSummary);
                String extra = null;
                if (provider instanceof LocalMangaProvider) {
                    extra = getString(R.string.local_size,
                            Formatter.formatFileSize(MangaPreviewActivity.this, StorageUtils.dirSize(new File(ms.path))));
                } else if (ms.status != MangaInfo.STATUS_UNKNOWN) {
                    extra = getString(ms.isCompleted() ? R.string.status_completed : R.string.status_ongoing);
                }
                return new Pair<>(ms, extra);
            } catch (Exception e) {
                return null;
            }
        }
    }

    private class LoadSourceTask extends LoaderTask<MangaInfo,Void,MangaSummary> implements DialogInterface.OnCancelListener {
        private final ProgressDialog mProgressDialog;

        public LoadSourceTask() {
            mProgressDialog = new ProgressDialog(MangaPreviewActivity.this);
            mProgressDialog.setMessage(getString(R.string.loading));
            mProgressDialog.setCancelable(true);
            mProgressDialog.setOnCancelListener(this);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mProgressDialog.show();
        }

        @Override
        protected MangaSummary doInBackground(MangaInfo... params) {
            return LocalMangaProvider.getInstance(MangaPreviewActivity.this)
                    .getSource(params[0]);
        }

        @Override
        protected void onPostExecute(MangaSummary sourceManga) {
            super.onPostExecute(sourceManga);
            mProgressDialog.dismiss();
            if (sourceManga == null) {
                Snackbar.make(mAppBarLayout, R.string.loading_error, Snackbar.LENGTH_SHORT)
                        .show();
                return;
            }
            ChaptersList newChapters = sourceManga.chapters.complementByName(mMangaSummary.chapters);
            if (sourceManga.chapters.size() <= mMangaSummary.chapters.size()) {
                Snackbar.make(mAppBarLayout, R.string.no_new_chapters, Snackbar.LENGTH_SHORT)
                        .show();
            } else {
                sourceManga.chapters = newChapters;
                DownloadService.start(MangaPreviewActivity.this, sourceManga, R.string.action_save_add);
            }
        }

        @Override
        public void onCancel(DialogInterface dialog) {
            this.cancel(true);
        }
    }
}
