package org.nv95.openmanga;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.cocosw.bottomsheet.BottomSheet;

import org.nv95.openmanga.components.MangaPager;
import org.nv95.openmanga.components.SimpleAnimator;
import org.nv95.openmanga.items.MangaChapter;
import org.nv95.openmanga.items.MangaPage;
import org.nv95.openmanga.items.MangaSummary;
import org.nv95.openmanga.providers.FavouritesProvider;
import org.nv95.openmanga.providers.HistoryProvider;
import org.nv95.openmanga.providers.LocalMangaProvider;
import org.nv95.openmanga.providers.MangaProvider;
import org.nv95.openmanga.utils.ContentShareHelper;
import org.nv95.openmanga.utils.UpdatesChecker;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by nv95 on 30.09.15.
 */
public class ReadActivity extends AppCompatActivity implements View.OnClickListener,
        ViewPager.OnPageChangeListener, ReaderOptionsDialog.OnOptionsChangedListener,
        NavigationDialog.NavigationListener, MangaPager.OverScrollListener, MenuItem.OnMenuItemClickListener {
    //views
    private MangaPager mPager;
    private ImageView mOversrollImageView;
    //data
    private MangaSummary mangaSummary;
    private MangaChapter chapter;
    private int chapterId;
    private int pageId;
    private boolean scrollWithVolkeys = false;
    private int overscrollSize;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reader);
        mPager = (MangaPager) findViewById(R.id.pager);
        mOversrollImageView = (ImageView) findViewById(R.id.imageViewOverscroll);
        findViewById(R.id.imageView_menu).setOnClickListener(this);
        mPager.addOnPageChangeListener(this);
        mPager.setOverScrollListener(this);
        onOptionsChanged();
        mangaSummary = new MangaSummary(getIntent().getExtras());
        if (mangaSummary.getChapters().size() == 0) {
            Snackbar.make(mPager, R.string.loading_error, Snackbar.LENGTH_SHORT).show();
            finish();
            return;
        }
        if (savedInstanceState != null) {
            chapterId = savedInstanceState.getInt("chapter");
            pageId = savedInstanceState.getInt("page");
        } else {
            chapterId = getIntent().getIntExtra("chapter", 0);
            pageId = getIntent().getIntExtra("page", 0);
        }
        overscrollSize = getResources().getDimensionPixelSize(R.dimen.overscroll_size);
        chapter = mangaSummary.getChapters().get(chapterId);
        mPager.setOffscreenPageLimit(3);
        new LoadPagesTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    @Override
    protected void onPause() {
        HistoryProvider.addToHistory(this, mangaSummary, chapterId, mPager.getCurrentPageIndex());
        super.onPause();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return onMenuItemClick(item) || super.onOptionsItemSelected(item);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("page", pageId);
        outState.putInt("chapter", chapterId);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.imageView_menu:
                BottomSheet sheet =
                new BottomSheet.Builder(this)
                        .sheet(R.menu.reader)
                        .listener(this)
                        .title(mangaSummary.getChapters().getNames()[chapterId] +
                                " (" + mPager.getCurrentPageIndex() + "/" + mPager.getCount() + ")")
                        .build();
                if (FavouritesProvider.Has(this, mangaSummary)) {
                    MenuItem item = sheet.getMenu().findItem(R.id.action_favourite);
                    item.setIcon(R.drawable.ic_favorite_dark);
                    item.setTitle(R.string.action_unfavourite);
                }
                sheet.show();
                break;
        }
    }

    private void SaveImage(File file, MangaPage page) {
        File dest = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), page.getPath().substring(page.getPath().lastIndexOf('/') + 1));
        try {
            LocalMangaProvider.CopyFile(file, dest);
            MediaScannerConnection.scanFile(ReadActivity.this,
                    new String[]{dest.getPath()}, null,
                    new MediaScannerConnection.OnScanCompletedListener() {
                        @Override
                        public void onScanCompleted(String path, Uri uri) {
                            //....
                        }
                    });
            Snackbar.make(mPager, R.string.image_saved, Snackbar.LENGTH_SHORT).show();
        } catch (IOException e) {
            Snackbar.make(mPager, R.string.unable_to_save_image, Snackbar.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (scrollWithVolkeys) {
            int page = mPager.getCurrentPageIndex();
            if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
                if (page < mPager.getCount()) {
                    mPager.scrollToPage(page + 1);
                }
                return true;
            }
            if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
                if (page > 0) {
                    mPager.scrollToPage(page - 1);
                }
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        return scrollWithVolkeys &&
                (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN || keyCode == KeyEvent.KEYCODE_VOLUME_UP) ||
                super.onKeyUp(keyCode, event);
    }

    @Override
    public void onPageSelected(int position) {
        pageId = mPager.getCurrentPageIndex();
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
            if (hasFocus) {
                getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
            }
        }
    }

    @Override
    public void onOptionsChanged() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        mPager.setReverse(prefs.getInt("scroll_direction", 0) > 1);
        mPager.setVertical(prefs.getInt("scroll_direction", 0) == 1);
        if (prefs.getBoolean("keep_screen", false)) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        } else {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
        scrollWithVolkeys = prefs.getBoolean("volkeyscroll", false);
    }

    @Override
    public void onPageChange(int page) {
        mPager.setCurrentPageIndex(page);
    }

    private boolean hasNextChapter() {
        if (mPager.isReverse()) {
            return chapterId > 0;
        } else {
            return chapterId < mangaSummary.getChapters().size() - 1;
        }
    }

    private boolean hasPrevChapter() {
        if (mPager.isReverse()) {
            return chapterId < mangaSummary.getChapters().size() - 1;
        } else {
            return chapterId > 0;
        }
    }

    @Override
    public boolean OnOverScroll(MangaPager viewPager, int deltaX, int direction) {
        if (deltaX == 0) {
            FrameLayout.LayoutParams params = ((FrameLayout.LayoutParams) mOversrollImageView.getLayoutParams());
            if (direction == -1 && hasPrevChapter()) {
                params.gravity = Gravity.CENTER_VERTICAL | Gravity.LEFT;
                params.leftMargin = -mOversrollImageView.getWidth();
                mOversrollImageView.setTranslationX(0);
                mOversrollImageView.setVisibility(View.VISIBLE);
                mOversrollImageView.getParent().requestLayout();
                return true;
            } else if (direction == 1 && hasNextChapter()) {
                params.gravity = Gravity.CENTER_VERTICAL | Gravity.RIGHT;
                params.rightMargin = -mOversrollImageView.getWidth();
                mOversrollImageView.setTranslationX(0);
                mOversrollImageView.setVisibility(View.VISIBLE);
                mOversrollImageView.getParent().requestLayout();
                return true;
            } else if (direction == 0) {
                if (mOversrollImageView.getTag() != null) {
                    float scrollFactor = (float) mOversrollImageView.getTag();
                    if (scrollFactor >= (float) overscrollSize / viewPager.getWidth()) {
                        new SimpleAnimator(mOversrollImageView).forceGravity(Gravity.CENTER).hide();
                        if ((params.gravity == Gravity.CENTER_VERTICAL + Gravity.RIGHT)) {
                            chapterId += mPager.isReverse() ? -1 : 1;
                            pageId = mPager.isReverse() ? -1 : 0;
                        } else {
                            chapterId += mPager.isReverse() ? 1 : -1;
                            pageId = mPager.isReverse() ? 0 : -1;
                        }
                        chapter = mangaSummary.getChapters().get(chapterId);
                        new LoadPagesTask().execute();
                        return true;
                    }
                }
                new SimpleAnimator(mOversrollImageView).hide();
                return false;
            } else {
                return false;
            }
        }
        float scrollFactor = deltaX / 2;
        mOversrollImageView.setTranslationX(-direction * scrollFactor);
        scrollFactor = scrollFactor / viewPager.getWidth() + 0.1f;
        if (scrollFactor > 0.5f) {
            scrollFactor = 0.5f;
        }
        mOversrollImageView.setColorFilter(Color.argb((int) (500 * (0.5f - scrollFactor)), 183, 28, 28));
        mOversrollImageView.setRotation(scrollFactor * 360 - (direction == 1 ? 180 : 0));
        mOversrollImageView.setTag(scrollFactor);
        return true;
    }

    private void showChaptersList() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setSingleChoiceItems(mangaSummary.getChapters().getNames(), chapterId, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                chapterId = which;
                pageId = 0;
                chapter = mangaSummary.getChapters().get(chapterId);
                new LoadPagesTask().execute();
                dialog.dismiss();
            }
        });
        builder.setTitle(R.string.chapters_list);
        builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.create().show();
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.action_chapters:
                showChaptersList();
                return true;
            case R.id.action_readprefs:
                new ReaderOptionsDialog(this).setOptionsChangedListener(this).show();
                return true;
            case R.id.action_goto:
                new NavigationDialog(this, mPager.getCount(), mPager.getCurrentPageIndex())
                        .setNavigationListener(this).show();
                return true;
            case R.id.action_favourite:
                FavouritesProvider favouritesProvider = FavouritesProvider.getInstacne(this);
                if (favouritesProvider.has(mangaSummary)) {
                    if (favouritesProvider.remove(mangaSummary)) {
                        item.setIcon(R.drawable.ic_favorite_outline_dark);
                        item.setTitle(R.string.action_unfavourite);
                    }
                } else {
                    if (favouritesProvider.add(mangaSummary)) {
                        UpdatesChecker.rememberChaptersCount(this, mangaSummary.hashCode(), mangaSummary.getChapters().size());
                        item.setIcon(R.drawable.ic_favorite_dark);
                        item.setTitle(R.string.action_favourite);
                    }
                }
                return true;
            case R.id.action_share:
                new ContentShareHelper(this).share(mangaSummary);
                return true;
            case R.id.action_save:
                if (!LocalMangaProvider.class.equals(mangaSummary.provider)) {
                    SaveService.SaveWithDialog(this, mangaSummary);
                } else {
                    Snackbar.make(mPager, R.string.already_saved, Snackbar.LENGTH_SHORT).show();
                }
                return true;
            case R.id.action_img_save:
                new GetImageTask() {
                    @Override
                    protected void onFileReady(@Nullable File file) {
                        if (file != null) {
                            SaveImage(file, mPager.getCurrentPage());
                        } else {
                            Snackbar.make(mPager, R.string.file_not_found, Snackbar.LENGTH_SHORT).show();
                        }
                    }
                }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, mPager.getCurrentPage());
                return true;
            case R.id.action_img_share:
                new GetImageTask() {
                    @Override
                    protected void onFileReady(@Nullable File file) {
                        if (file != null) {
                            Intent shareIntent = new Intent();
                            shareIntent.setAction(Intent.ACTION_SEND);
                            shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file));
                            shareIntent.setType("image/jpeg");
                            startActivity(Intent.createChooser(shareIntent, getString(R.string.action_share)));
                        } else {
                            Snackbar.make(mPager, R.string.file_not_found, Snackbar.LENGTH_SHORT).show();
                        }
                    }
                }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, mPager.getCurrentPage());
                return true;
            default:
                return false;
        }
    }

    private class LoadPagesTask extends AsyncTask<Void, Void, ArrayList<MangaPage>> implements DialogInterface.OnCancelListener {
        private ProgressDialog progressDialog;

        public LoadPagesTask() {
            progressDialog = new ProgressDialog(ReadActivity.this);
            progressDialog.setMessage(getString(R.string.loading));
            progressDialog.setCancelable(true);
            progressDialog.setOnCancelListener(this);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog.show();
        }

        @Override
        protected void onPostExecute(ArrayList<MangaPage> mangaPages) {
            super.onPostExecute(mangaPages);
            progressDialog.dismiss();
            if (mangaPages == null) {
                new AlertDialog.Builder(ReadActivity.this).setMessage(R.string.loading_error).setTitle(R.string.app_name)
                        .setOnCancelListener(this).setPositiveButton(R.string.close, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ReadActivity.this.finish();
                    }
                }).create().show();
                return;
            }
            if (pageId == -1) {
                pageId = mangaPages.size() - 1;
            }
            mPager.setPages(mangaPages, pageId);
        }

        @Override
        protected ArrayList<MangaPage> doInBackground(Void... params) {
            try {
                MangaProvider provider;
                if (mangaSummary.provider.equals(LocalMangaProvider.class)) {
                    provider = LocalMangaProvider.getInstacne(ReadActivity.this);
                } else {
                    provider = (MangaProvider) mangaSummary.provider.newInstance();
                }
                return provider.getPages(chapter.readLink);
            } catch (Exception ignored) {
                return null;
            }
        }

        @Override
        public void onCancel(DialogInterface dialog) {
            this.cancel(true);
            ReadActivity.this.finish();
        }
    }

    private abstract class GetImageTask extends AsyncTask<MangaPage,Void,File> implements DialogInterface.OnCancelListener {
        private final ProgressDialog mProgressDialog;

        public GetImageTask() {
            mProgressDialog = new ProgressDialog(ReadActivity.this);
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
        protected File doInBackground(MangaPage... params) {
            try {
                MangaProvider provider;
                if (mangaSummary.provider.equals(LocalMangaProvider.class)) {
                    provider = LocalMangaProvider.getInstacne(ReadActivity.this);
                } else {
                    provider = (MangaProvider) mangaSummary.provider.newInstance();
                }
                String url = provider.getPageImage(params[0]);
                File file = new File(getExternalCacheDir(), String.valueOf(url.hashCode()));
                if (!file.exists()) {
                    //// TODO: 26.01.16
                }
                File dest = new File(getExternalFilesDir("temp"), params[0].getPath()
                        .substring(params[0].getPath().lastIndexOf('/') + 1));
                LocalMangaProvider.CopyFile(file, dest);
                return dest;
            } catch (Exception ignored) {
                return null;
            }
        }

        @Override
        protected void onPostExecute(File file) {
            super.onPostExecute(file);
            mProgressDialog.dismiss();
            onFileReady(file);
        }

        protected abstract void onFileReady(@Nullable File file);

        @Override
        public void onCancel(DialogInterface dialog) {
            this.cancel(true);
        }
    }
}
