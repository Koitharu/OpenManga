package org.nv95.openmanga;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.nv95.openmanga.components.AsyncImageView;
import org.nv95.openmanga.components.BottomSheetDialog;
import org.nv95.openmanga.items.MangaInfo;
import org.nv95.openmanga.items.MangaSummary;
import org.nv95.openmanga.providers.FavouritesProvider;
import org.nv95.openmanga.providers.HistoryProvider;
import org.nv95.openmanga.providers.LocalMangaProvider;
import org.nv95.openmanga.providers.MangaProvider;
import org.nv95.openmanga.utils.UpdatesChecker;

/**
 * Created by nv95 on 30.09.15.
 */
public class MangaPreviewActivity extends AppCompatActivity implements View.OnClickListener,
        DialogInterface.OnClickListener {
    //views
    private FloatingActionButton mFab;
    private AsyncImageView mImageView;
    private ProgressBar mProgressBar;
    private TextView mTextViewSummary;
    private TextView mTextViewDescription;
    private CollapsingToolbarLayout mCollapsingToolbarLayout;
    private AppBarLayout mAppBarLayout;
    //data
    protected MangaSummary mangaSummary;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preview);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
        }
        if (Build.VERSION.SDK_INT >= 21) {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        }
        mangaSummary = new MangaSummary(new MangaInfo(getIntent().getExtras()));
        mCollapsingToolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.collapsing_container);
        mAppBarLayout = (AppBarLayout) findViewById(R.id.appbar_container);
        mImageView = (AsyncImageView) findViewById(R.id.imageView);
        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);
        mFab = (FloatingActionButton) findViewById(R.id.fab_read);
        mTextViewSummary = (TextView) findViewById(R.id.textView_summary);
        mTextViewDescription = (TextView) findViewById(R.id.textView_description);
        mCollapsingToolbarLayout.setTitle(mangaSummary.name);
        mTextViewSummary.setText(mangaSummary.summary);
        mFab.setOnClickListener(this);
        mImageView.setColorFilter(ContextCompat.getColor(this, R.color.preview_filter));
        mImageView.setImageAsync(mangaSummary.preview);
        mImageView.setOnClickListener(this);
        new LoadInfoTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.fab_read:
                showChaptersSheet();
                break;
            case R.id.imageView:
                // TODO: 26.01.16
                break;
            case R.id.snackbar_action:
                new LoadInfoTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                break;
        }
    }

    private void showChaptersSheet() {
        new BottomSheetDialog(this)
                .setItems(mangaSummary.getChapters().getNames(), android.R.layout.simple_list_item_1)
                .setSheetTitle(R.string.chapters_list)
                .setNegativeButton(android.R.string.cancel, null)
                .setOnItemClickListener(this)
                .setPositiveButton(R.string.continue_reading, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent intent = new Intent(MangaPreviewActivity.this, ReadActivity.class);
                                intent.putExtras(mangaSummary.toBundle());
                                HistoryProvider.HistorySummary hs = HistoryProvider.get(MangaPreviewActivity.this, mangaSummary);
                                intent.putExtra("chapter", hs.getChapter());
                                intent.putExtra("page", hs.getPage());
                                startActivity(intent);
                            }
                        }
                ).show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.preview, menu);
        if (LocalMangaProvider.class.equals(mangaSummary.provider)) {
            menu.findItem(R.id.action_save).setVisible(false);
            menu.findItem(R.id.action_remove).setVisible(true);
            menu.findItem(R.id.action_favourite).setVisible(false);
        } else if (FavouritesProvider.Has(this, mangaSummary)) {
            menu.findItem(R.id.action_favourite).setIcon(R.drawable.ic_favorite_light);
            menu.findItem(R.id.action_favourite).setTitle(R.string.action_unfavourite);
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_favourite:
                FavouritesProvider favouritesProvider = FavouritesProvider.getInstacne(this);
                if (favouritesProvider.has(mangaSummary)) {
                    if (favouritesProvider.remove(mangaSummary)) {
                        item.setIcon(R.drawable.ic_favorite_outline_light);
                        item.setTitle(R.string.action_favourite);
                    }
                } else {
                    if (favouritesProvider.add(mangaSummary)) {
                        UpdatesChecker.rememberChaptersCount(this, mangaSummary.hashCode(), mangaSummary.getChapters().size());
                        item.setIcon(R.drawable.ic_favorite_light);
                        item.setTitle(R.string.action_unfavourite);
                    }
                }
                return true;
            case R.id.action_save:
                SaveService.SaveWithDialog(this, mangaSummary);
                return true;
            case R.id.action_remove:
                new AlertDialog.Builder(MangaPreviewActivity.this)
                        .setCancelable(true)
                        .setPositiveButton(R.string.action_remove, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (LocalMangaProvider.getInstacne(MangaPreviewActivity.this)
                                        .remove(new long[]{mangaSummary.path.hashCode()})) {
                                    finish();
                                } else {
                                    Snackbar.make(mAppBarLayout, R.string.error, Snackbar.LENGTH_SHORT)
                                            .show();
                                }
                            }
                        })
                        .setNegativeButton(android.R.string.cancel, null)
                        .setMessage(R.string.manga_delete_confirm)
                        .create().show();

                return true;
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    @Override
    public void onClick(DialogInterface dialog, int which) {
        dialog.dismiss();
        HistoryProvider.addToHistory(this, mangaSummary, which, 0);
        startActivity(new Intent(this, ReadActivity.class).putExtra("chapter", which).putExtras(mangaSummary.toBundle()));
    }

    private class LoadInfoTask extends AsyncTask<Void, Void, MangaSummary> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(MangaSummary mangaSummary) {
            mProgressBar.setVisibility(View.GONE);
            mFab.setEnabled(true);
            super.onPostExecute(mangaSummary);
            if (mangaSummary == null) {
                mFab.hide(); Snackbar.make(mAppBarLayout, R.string.loading_error, Snackbar.LENGTH_INDEFINITE)
                        .setAction(R.string.retry, MangaPreviewActivity.this)
                        .show();
                return;
            }
            MangaPreviewActivity.this.mangaSummary = mangaSummary;
            mTextViewDescription.setText(mangaSummary.getDescription());
            mImageView.setImageAsync(mangaSummary.preview, false);
            if (mangaSummary.getChapters().size() == 0) {
                mFab.setEnabled(false);
                Snackbar.make(mAppBarLayout, R.string.no_chapters_found, Snackbar.LENGTH_INDEFINITE)
                        .show();
            }
        }

        @Override
        protected MangaSummary doInBackground(Void... params) {
            try {
                MangaProvider provider;
                if (mangaSummary.provider.equals(LocalMangaProvider.class)) {
                    provider = LocalMangaProvider.getInstacne(MangaPreviewActivity.this);
                } else {
                    provider = (MangaProvider) mangaSummary.provider.newInstance();
                }
                return provider.getDetailedInfo(mangaSummary);
            } catch (Exception e) {
                return null;
            }
        }
    }
}
