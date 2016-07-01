package org.nv95.openmanga.activities;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.DecelerateInterpolator;
import android.webkit.MimeTypeMap;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import org.nv95.openmanga.R;
import org.nv95.openmanga.components.MangaPager;
import org.nv95.openmanga.components.OverScrollDetector;
import org.nv95.openmanga.dialogs.NavigationDialog;
import org.nv95.openmanga.dialogs.ReaderMenuDialog;
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
import org.nv95.openmanga.utils.imagecontroller.TouchGridDetector;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by nv95 on 30.09.15.
 */
public class ReadActivity extends BaseAppActivity implements View.OnClickListener,
        ViewPager.OnPageChangeListener, NavigationDialog.NavigationListener,
        MangaPager.OverScrollListener, ValueAnimator.AnimatorUpdateListener {
    //views
    private MangaPager mPager;
    private View mLoader;
    private View mSwipeFrame;
    private TextView mTextViewNext;
    private ImageView mImageViewArrow;
    private TouchGridDetector mGridDetector;
    //data
    private MangaSummary mangaSummary;
    private MangaChapter chapter;
    private int chapterId;
    private int pageId;
    private boolean scrollWithVolkeys = false;
    private int overscrollSize;
    private BrightnessHelper mBrightnessHelper;
    private SwipeAnimationListener mSwipeAnimationListener = new SwipeAnimationListener();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reader);
        enableTransparentStatusBar(android.R.color.transparent);
        mLoader = findViewById(R.id.loader);
        mBrightnessHelper = new BrightnessHelper(getWindow());
        mPager = (MangaPager) findViewById(R.id.pager);
        mSwipeFrame = findViewById(R.id.swipeFrame);
        mTextViewNext = (TextView) findViewById(R.id.textView_title);
        mImageViewArrow = (ImageView) findViewById(R.id.imageView_arrow);
        mGridDetector = (TouchGridDetector) findViewById(R.id.gridDetector);
        ImageView imageViewMenu = (ImageView) findViewById(R.id.imageView_menu);
        assert imageViewMenu != null;
        if (isDarkTheme()) {
            imageViewMenu.setColorFilter(ContextCompat.getColor(this, R.color.white_overlay_85));
        }
        imageViewMenu.setOnClickListener(this);
        mPager.addOnPageChangeListener(this);
        mPager.setOverScrollListener(this);
        mPager.onConfigurationChange(this);
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
        mPager.setOffscreenPageLimit(2);
        new LoadPagesTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case ReaderMenuDialog.REQUEST_SETTINGS:
                onOptionsChanged();
                break;
        }
    }

    private void initParams(Bundle b){
        chapterId = b.getInt("chapter", 0);
        pageId = b.getInt("page", 0);
    }

    @Override
    protected void onPause() {
        saveHistory();
        super.onPause();
    }

    private void saveHistory() {
        HistoryProvider.getInstacne(this).add(mangaSummary, chapterId, mPager.getCurrentPageIndex());
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
                saveHistory();
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
                new ReaderMenuDialog(this, isDarkTheme())
                        .callback(this)
                        .favourites(fav)
                        .setOnDismissListener(new ReaderMenuDialog.OnDismissListener() {
                            @Override
                            public void settingsDialogDismiss() {
                                setFullScreen();
                            }
                        })
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
                                    .storeChaptersCount(mangaSummary.hashCode(), mangaSummary.getChapters().size());

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

    private void setFullScreen(){
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
        mPager.setBehavior(direction == 1, direction > 1, MangaPager.TRANSFORM_MODE_SLIDE);
        if (prefs.getBoolean("keep_screen", true)) {
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

    private void setArrowPosition(int gravity){
        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) mImageViewArrow.getLayoutParams();
        params.gravity = Gravity.CENTER_VERTICAL | gravity;
        params.leftMargin = gravity == GravityCompat.START ? -mImageViewArrow.getWidth() : 0;
        params.rightMargin = gravity == GravityCompat.START ? 0 : -mImageViewArrow.getWidth();
        mImageViewArrow.setTranslationX(0);
        mImageViewArrow.setVisibility(View.VISIBLE);
        mImageViewArrow.getParent().requestLayout();
    }

    private void loadChapter(){
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
        onCancelled(direction);
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
    public void onOverScroll(int direction, float deltaX, float deltaY) {
        switch (direction) {
            case OverScrollDetector.DIRECTION_LEFT:
                mImageViewArrow.setTranslationX(Math.abs(deltaX) < overscrollSize ? -deltaX : overscrollSize);
//                mImageViewArrow.setTranslationX(overscrollSize);
                break;
            case OverScrollDetector.DIRECTION_RIGHT:
                mImageViewArrow.setTranslationX(Math.abs(deltaX) < overscrollSize ? -deltaX : -overscrollSize);
//                mImageViewArrow.setTranslationX(-overscrollSize);
                break;
            default:
                return;
        }
        double delta = Math.sqrt(deltaX * deltaX + deltaY * deltaY);
        delta = delta * overscrollSize / 240;
        if (delta > 180) {
            delta = 180;
        }
        mSwipeFrame.setBackgroundColor(Color.argb((int) delta, 0, 0, 0));
    }

    /**
     * Animate arrow and swipe background when overscroll swipe
     * @param direction
     */
    private void animateSwipeViews(float direction) {
        mSwipeAnimationListener.setDirection(direction < 0 ?
                OverScrollDetector.DIRECTION_RIGHT : OverScrollDetector.DIRECTION_LEFT);
        mImageViewArrow.animate()
                .setInterpolator(new DecelerateInterpolator())
                .setListener(mSwipeAnimationListener)
                .translationX(direction);
        mSwipeFrame.animate()
                .alpha(180);
    }

    // Triggered when overscroll swipe
    @Override
    public void onSwipeLeft() {
        animateSwipeViews(-overscrollSize);
    }
    //Triggered when overscroll swipe
    @Override
    public void onSwipeRight() {
        animateSwipeViews(overscrollSize);
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

    @Override
    public void onPreOverScroll(int direction) {
        switch (direction) {
            case OverScrollDetector.DIRECTION_LEFT:
                setArrowPosition(Gravity.LEFT);
                mImageViewArrow.setRotation(-90);
                mTextViewNext.setText(getString(mPager.isReverse() ? R.string.next_chapter : R.string.prev_chapter,
                        mangaSummary.chapters.get(chapterId + (mPager.isReverse() ? 1 : -1)).name));
                break;
            case OverScrollDetector.DIRECTION_RIGHT:
                setArrowPosition(Gravity.RIGHT);
                mImageViewArrow.setRotation(90);
                mTextViewNext.setText(getString(mPager.isReverse() ? R.string.prev_chapter : R.string.next_chapter,
                        mangaSummary.chapters.get(chapterId + (mPager.isReverse() ? -1 : 1)).name));
                break;
            default:
                return;
        }
        mSwipeFrame.setAlpha(1);
        mSwipeFrame.setBackgroundColor(Color.TRANSPARENT);
        mSwipeFrame.setVisibility(View.VISIBLE);
    }

    @Override
    public void onCancelled(int direction) {
        final ValueAnimator animator = ValueAnimator.ofFloat(1, 0);
        animator.addUpdateListener(this);
        animator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                mSwipeFrame.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        animator.setRepeatCount(0);
        animator.setDuration(300);
        animator.start();
    }

    @Override
    public void onAnimationUpdate(ValueAnimator animation) {
        mSwipeFrame.setAlpha((Float) animation.getAnimatedValue());
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
                new AlertDialog.Builder(ReadActivity.this).setMessage(checkConnection() ? R.string.loading_error : R.string.no_network_connection)
                        .setTitle(R.string.app_name)
                        .setOnCancelListener(this)
                        .setPositiveButton(R.string.close, new DialogInterface.OnClickListener() {
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

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mPager.onConfigurationChange(this);
    }

    class SwipeAnimationListener implements Animator.AnimatorListener {

        private int direction;

        public void setDirection(int direction) {
            this.direction = direction;
        }

        @Override
        public void onAnimationStart(Animator animation) {

        }

        @Override
        public void onAnimationEnd(Animator animation) {
            onOverScrollDone(direction);
        }

        @Override
        public void onAnimationCancel(Animator animation) {

        }

        @Override
        public void onAnimationRepeat(Animator animation) {

        }
    }
}
