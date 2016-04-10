package org.nv95.openmanga.activities;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.webkit.MimeTypeMap;
import android.widget.ImageView;

import org.nv95.openmanga.NavigationDialog;
import org.nv95.openmanga.R;
import org.nv95.openmanga.ReaderMenuDialog;
import org.nv95.openmanga.components.MangaPager;
import org.nv95.openmanga.components.OverScrollDetector;
import org.nv95.openmanga.helpers.BrightnessHelper;
import org.nv95.openmanga.helpers.ContentShareHelper;
import org.nv95.openmanga.items.MangaChapter;
import org.nv95.openmanga.items.MangaPage;
import org.nv95.openmanga.items.MangaSummary;
import org.nv95.openmanga.items.SimpleDownload;
import org.nv95.openmanga.providers.FavouritesProvider;
import org.nv95.openmanga.providers.HistoryProvider;
import org.nv95.openmanga.providers.LocalMangaProvider;
import org.nv95.openmanga.providers.MangaProvider;
import org.nv95.openmanga.providers.NewChaptersProvider;
import org.nv95.openmanga.services.DownloadService;
import org.nv95.openmanga.utils.AppHelper;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by nv95 on 30.09.15.
 */
public class ReadActivity extends AppCompatActivity implements View.OnClickListener,
        ViewPager.OnPageChangeListener, NavigationDialog.NavigationListener,
        MangaPager.OverScrollListener {
    //views
    private MangaPager mPager;
    private ImageView mOversrollImageView;
    private View mLoader;
    //data
    private MangaSummary mangaSummary;
    private MangaChapter chapter;
    private int chapterId;
    private int pageId;
    private boolean scrollWithVolkeys = false;
    private int overscrollSize;
    private BrightnessHelper mBrightnessHelper;
    private GestureDetectorCompat mDetector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reader);
        mLoader = findViewById(R.id.loader);
        mBrightnessHelper = new BrightnessHelper(getWindow());
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

        initParams(savedInstanceState != null ? savedInstanceState : getIntent().getExtras());
        overscrollSize = getResources().getDimensionPixelSize(R.dimen.overscroll_size);
        chapter = mangaSummary.getChapters().get(chapterId);
        mPager.setOffscreenPageLimit(3);
        new LoadPagesTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void initParams(Bundle b){
        chapterId = b.getInt("chapter", 0);
        pageId = b.getInt("page", 0);
    }

    @Override
    protected void onPause() {
        HistoryProvider.addToHistory(this, mangaSummary, chapterId, mPager.getCurrentPageIndex());
        super.onPause();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
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
                int favId = FavouritesProvider.getInstacne(this)
                        .getCategory(mangaSummary);
                String fav;
                switch (favId) {
                    case -1:
                        fav = null;
                        break;
                    case 0:
                        fav = getString(R.string.category_no);
                        break;
                    default:
                        String[] titles = FavouritesProvider.getInstacne(this)
                                .getGenresTitles(this);
                        fav = (titles != null && favId < titles.length) ?
                                titles[favId] : getString(R.string.category_no);
                        break;
                }
                new ReaderMenuDialog(this)
                        .callback(this)
                        .favourites(fav)
                        .setOnDismissListener(new ReaderMenuDialog.OnDismissListener() {
                            @Override
                            public void settingsDialogDismiss() {
                                setFullScreen();
                            }
                        })
                        .brightnessHelper(mBrightnessHelper)
                        .progress(
                                mPager.getCurrentPageIndex(),
                                mPager.getCount()
                        )
                        .chapter(mangaSummary.getChapters().get(chapterId).name)
                        .title(mangaSummary.name)
                        .show();
                break;
            case R.id.button_save:
                if (!LocalMangaProvider.class.equals(mangaSummary.provider)) {
                    DownloadService.start(this, mangaSummary);
                } else {
                    Snackbar.make(mPager, R.string.already_saved, Snackbar.LENGTH_SHORT).show();
                }
                break;
            case R.id.button_fav:
                FavouritesProvider favouritesProvider = FavouritesProvider.getInstacne(this);
                if (favouritesProvider.has(mangaSummary)) {
                    if (favouritesProvider.remove(mangaSummary)) {
                        Snackbar.make(mPager, R.string.unfavourited, Snackbar.LENGTH_SHORT).show();
                    }
                } else {
                    FavouritesProvider.AddDialog(this, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            NewChaptersProvider.getInstance(ReadActivity.this)
                                    .storeChaptersCount(mangaSummary.hashCode(), mangaSummary.getChapters().size(), 0);

                        }
                    }, mangaSummary);
                }
                break;
            case R.id.button_share:
                new ContentShareHelper(this).share(mangaSummary);
                break;
            case R.id.button_img:
                new SaveImageTask()
                        .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, mPager.getCurrentPage());
                break;
            case R.id.textView_subtitle:
                showChaptersList();
                break;
            case R.id.textView_goto:
                new NavigationDialog(this, mPager.getCount(), mPager.getCurrentPageIndex())
                        .setNavigationListener(this).show();
                break;
            case R.id.button_positive:
                onOptionsChanged();
                break;
        }
    }

    private void SaveImage(final File file) {
        File dest = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), file.getName());
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
            Snackbar.make(mPager, R.string.image_saved, Snackbar.LENGTH_LONG)
                    .setAction(R.string.action_share, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            new ContentShareHelper(ReadActivity.this).shareImage(file);
                        }
                    })
                    .show();
        } catch (IOException e) {
            Snackbar.make(mPager, R.string.unable_to_save_image, Snackbar.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_MENU) {
            onClick(findViewById(R.id.imageView_menu));
            return super.onKeyDown(keyCode, event);
        }
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

    void setFullScreen(){
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        setFullScreen();
    }

    public void onOptionsChanged() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        int direction = Integer.parseInt(prefs.getString("direction", "0"));
        mPager.setReverse(direction > 1);
        mPager.setVertical(direction == 1);
        if (prefs.getBoolean("keep_screen", false)) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        } else {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
        scrollWithVolkeys = prefs.getBoolean("volkeyscroll", false);
        if (prefs.getBoolean("brightness", false)) {
            mBrightnessHelper.setBrightness(prefs.getInt("brightness_value", 20));
        } else {
            mBrightnessHelper.reset();
        }

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

    void setArrowPosition(CoordinatorLayout.LayoutParams params, int gravity){
        params.gravity = Gravity.CENTER_VERTICAL | gravity;
        params.leftMargin = gravity == GravityCompat.START ? -mOversrollImageView.getWidth() : 0;
        params.rightMargin = gravity == GravityCompat.START ? 0 : -mOversrollImageView.getWidth();
        mOversrollImageView.setTranslationX(0);
        mOversrollImageView.setVisibility(View.VISIBLE);
        mOversrollImageView.getParent().requestLayout();
    }

    void loadChapter(){
        chapter = mangaSummary.getChapters().get(chapterId);
        new LoadPagesTask().execute();
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
    public void onOverScrollDone(int direction) {
        switch (direction) {
            case OverScrollDetector.DIRECTION_LEFT:
                if (hasPrevChapter()){
                    chapterId += mPager.isReverse() ? 1 : -1;
                    pageId = mPager.isReverse() ? 0 : -1;
                    loadChapter();
                }
                break;
            case OverScrollDetector.DIRECTION_RIGHT:
                if(hasNextChapter()){
                    chapterId += mPager.isReverse() ? -1 : 1;
                    pageId = mPager.isReverse() ? -1 : 0;
                    loadChapter();
                }
                break;
        }
    }

    @Override
    public void onFly(int direction, float deltaX, float deltaY) {

    }

    @Override
    public boolean canOverScroll(int direction) {
        switch (direction) {
            case OverScrollDetector.DIRECTION_LEFT:
                return hasPrevChapter();
            case OverScrollDetector.DIRECTION_RIGHT:
                return hasNextChapter();
            default:
                return false;
        }
    }

    private class LoadPagesTask extends AsyncTask<Void, Void, ArrayList<MangaPage>> implements DialogInterface.OnCancelListener {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mLoader.setVisibility(View.VISIBLE);
        }

        @Override
        protected void onPostExecute(ArrayList<MangaPage> mangaPages) {
            super.onPostExecute(mangaPages);
            mLoader.setVisibility(View.GONE);
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
            AppHelper.showcaseTip(ReadActivity.this, R.id.imageView_menu, R.string.tip_reader_menu, "readermenu");
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

    private class SaveImageTask extends AsyncTask<MangaPage, Void, File> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mLoader.setVisibility(View.VISIBLE);
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
                File dest = new File(getExternalFilesDir("temp"), url.hashCode() + "." + MimeTypeMap.getFileExtensionFromUrl(url));
                final SimpleDownload dload = new SimpleDownload(url, dest);
                dload.run();
                return dload.isSuccess() ? dest : null;
            } catch (Exception ignored) {
                return null;
            }
        }

        @Override
        protected void onPostExecute(File file) {
            super.onPostExecute(file);
            mLoader.setVisibility(View.GONE);
            if (file != null) {
                SaveImage(file);
            } else {
                Snackbar.make(mPager, R.string.file_not_found, Snackbar.LENGTH_SHORT).show();
            }

        }
    }
}
