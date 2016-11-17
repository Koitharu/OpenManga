package org.nv95.openmanga.activities;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import android.widget.Toast;

import org.nv95.openmanga.R;
import org.nv95.openmanga.components.pager.MangaPager;
import org.nv95.openmanga.components.pager.OverScrollDetector;
import org.nv95.openmanga.components.reader.imagecontroller.PageHolder;
import org.nv95.openmanga.dialogs.NavigationDialog;
import org.nv95.openmanga.dialogs.NavigationListener;
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
import org.nv95.openmanga.utils.ChangesObserver;
import org.nv95.openmanga.utils.StorageUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by nv95 on 30.09.15.
 */
public class ReadActivity extends BaseAppActivity implements View.OnClickListener,
        ViewPager.OnPageChangeListener, NavigationListener,
        MangaPager.OverScrollListener, ValueAnimator.AnimatorUpdateListener {

    private static final int REQUEST_SETTINGS = 1299;
    //views
    private MangaPager mPager;
    private View mLoader;
    private View mSwipeFrame;
    private TextView mTextViewNext;
    private ImageView mImageViewArrow;
    private View[] mClickAreas;
    //data
    private MangaSummary mMangaSummary;
    private MangaChapter mChapter;
    private int mChapterId;
    private int mPageId;
    private boolean mScrollVolumeKeys = false;
    private int mOverscrollSize;
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
        mClickAreas = new View[] {
                findViewById(R.id.area_left),
                findViewById(R.id.area_right),
                findViewById(R.id.area_bottom),
                findViewById(R.id.area_bottom_left),
                findViewById(R.id.area_bottom_right)
        };
        ImageView imageViewMenu = (ImageView) findViewById(R.id.imageView_menu);
        assert imageViewMenu != null;
        if (isDarkTheme()) {
            imageViewMenu.setColorFilter(ContextCompat.getColor(this, R.color.white_overlay_85));
        }
        imageViewMenu.setOnClickListener(this);
        mClickAreas[0].setOnClickListener(this);
        mClickAreas[1].setOnClickListener(this);
        mClickAreas[3].setOnClickListener(this);
        mClickAreas[4].setOnClickListener(this);
        mPager.addOnPageChangeListener(this);
        mPager.setOverScrollListener(this);
        mPager.onConfigurationChange(this);
        onOptionsChanged();
        mMangaSummary = new MangaSummary(getIntent().getExtras());
        if (mMangaSummary.getChapters().size() == 0) {
            Snackbar.make(mPager, R.string.loading_error, Snackbar.LENGTH_SHORT).show();
            finish();
            return;
        }

        initParams(savedInstanceState != null ? savedInstanceState : getIntent().getExtras());
        mOverscrollSize = getResources().getDimensionPixelSize(R.dimen.overscroll_size);
        mChapter = mMangaSummary.getChapters().get(mChapterId);
        mPager.setOffscreenPageLimit(2);
        new LoadPagesTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_SETTINGS:
                onOptionsChanged();
                break;
        }
    }

    private void initParams(Bundle b){
        mChapterId = b.getInt("chapter", 0);
        mPageId = b.getInt("page", 0);
    }

    @Override
    protected void onPause() {
        saveHistory();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        ChangesObserver.getInstance().emitOnHistoryChanged(mMangaSummary);
        super.onDestroy();
    }

    private void saveHistory() {
        if (mChapterId >= 0 && mChapterId < mMangaSummary.chapters.size()) {
            HistoryProvider.getInstance(this).add(mMangaSummary, mMangaSummary.chapters.get(mChapterId).number, mPager.getCurrentPageIndex());
        }
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
        outState.putInt("page", mPageId);
        outState.putInt("chapter", mChapterId);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.imageView_menu:
                if (mLoader.getVisibility() == View.VISIBLE) {
                    return;
                }
                int favId = FavouritesProvider.getInstance(this)
                        .getCategory(mMangaSummary);
                String fav;
                switch (favId) {
                    case -1:
                        fav = null;
                        break;
                    case 0:
                        fav = getString(R.string.category_no);
                        break;
                    default:
                        String[] titles = FavouritesProvider.getInstance(this)
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
                        .progress(
                                mPager.getCurrentPageIndex(),
                                mPager.getCount()
                        )
                        .chapter(mMangaSummary.getChapters().get(mChapterId).name)
                        .title(mMangaSummary.name)
                        .show();
                break;
            case R.id.button_save:
                if (!LocalMangaProvider.class.equals(mMangaSummary.provider)) {
                    DownloadService.start(this, mMangaSummary);
                } else {
                    Snackbar.make(mPager, R.string.already_saved, Snackbar.LENGTH_SHORT).show();
                }
                break;
            case R.id.button_opt:
                startActivityForResult(new Intent(this, SettingsActivity.class)
                        .putExtra("section", SettingsActivity.SECTION_READER), REQUEST_SETTINGS);
                break;
            case R.id.button_fav:
                FavouritesProvider favouritesProvider = FavouritesProvider.getInstance(this);
                if (favouritesProvider.has(mMangaSummary)) {
                    if (favouritesProvider.remove(mMangaSummary)) {
                        Snackbar.make(mPager, R.string.unfavourited, Snackbar.LENGTH_SHORT).show();
                        ChangesObserver.getInstance().emitOnFavouritesChanged(mMangaSummary, -1);
                    }
                } else {
                    FavouritesProvider.dialog(this, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            NewChaptersProvider.getInstance(ReadActivity.this)
                                    .storeChaptersCount(mMangaSummary.id, mMangaSummary.getChapters().size());
                            ChangesObserver.getInstance().emitOnFavouritesChanged(mMangaSummary, which);
                        }
                    }, mMangaSummary);
                }
                break;
            case R.id.button_share:
                new ContentShareHelper(this).share(mMangaSummary);
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
            case R.id.area_left:
            case R.id.area_bottom_left:
                if (mPager.getCurrentItem() > 0) {
                    mPager.setCurrentItem(mPager.getCurrentItem() - 1);
                } else if (mPager.isReverse() ? mChapterId < mMangaSummary.chapters.size() - 1 : mChapterId > 0){
                    mChapterId += mPager.isReverse() ? 1 : -1;
                    mPageId = mPager.isReverse() ? 0 : -1;
                    loadChapter();
                    showToast(mChapter.name, Gravity.TOP, Toast.LENGTH_LONG);
                }
                break;
            case R.id.area_right:
            case R.id.area_bottom_right:
                if (mPager.getCurrentItem() < mPager.getCount() - 1) {
                    mPager.setCurrentItem(mPager.getCurrentItem() + 1);
                } else if (mPager.isReverse() ? mChapterId > 0 : mChapterId < mMangaSummary.chapters.size() - 1) {
                    mChapterId += mPager.isReverse() ? -1 : 1;
                    mPageId = mPager.isReverse() ? -1 : 0;
                    loadChapter();
                    showToast(mChapter.name, Gravity.TOP, Toast.LENGTH_LONG);
                }
                break;
        }
    }

    private void saveImage(final File file) {
        File dest = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), file.getName());
        try {
            StorageUtils.copyFile(file, dest);
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
        if (mScrollVolumeKeys) {
            int page = mPager.getCurrentPageIndex();
            if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
                if (page < mPager.getCount() - 1) {
                    mPager.scrollToPage(page + 1);
                } else if (mChapterId < mMangaSummary.chapters.size() - 1) {
                    mChapterId += 1;
                    mPageId = 0;
                    loadChapter();
                    showToast(mChapter.name, Gravity.TOP, Toast.LENGTH_LONG);
                }
                return true;
            }
            if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
                if (page > 0) {
                    mPager.scrollToPage(page - 1);
                } else if (mChapterId > 0) {
                    mChapterId -= 1;
                    mPageId = -1;
                    loadChapter();
                    showToast(mChapter.name, Gravity.TOP, Toast.LENGTH_LONG);
                }
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

        @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        return mScrollVolumeKeys &&
                (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN || keyCode == KeyEvent.KEYCODE_VOLUME_UP) ||
                super.onKeyUp(keyCode, event);
    }

    @Override
    public void onPageSelected(int position) {
        if (mLoader.getVisibility() != View.VISIBLE && !mPager.getReaderAdapter().isFreezed()) {
            mPageId = mPager.getCurrentPageIndex();
        }
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
        mPager.setBehavior(
                direction == 1,
                direction > 1,
                Integer.parseInt(prefs.getString("animation", String.valueOf(MangaPager.TRANSFORM_MODE_SCROLL))),
                Integer.parseInt(prefs.getString("scalemode", String.valueOf(PageHolder.SCALE_FIT)))
        );
        if (prefs.getBoolean("keep_screen", true)) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        } else {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
        mScrollVolumeKeys = prefs.getBoolean("volkeyscroll", false);
        if (prefs.getBoolean("brightness", false)) {
            mBrightnessHelper.setBrightness(prefs.getInt("brightness_value", 20));
        } else {
            mBrightnessHelper.reset();
        }
        int areas = Integer.parseInt(prefs.getString("clickareas", "0"));
        mClickAreas[0].setVisibility(areas == 2 ? View.VISIBLE : View.GONE);
        mClickAreas[1].setVisibility(areas == 2 ? View.VISIBLE : View.GONE);
        mClickAreas[2].setVisibility(areas == 1 ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onPageChange(int page) {
        mPager.getReaderAdapter().freeze();
        mPager.setCurrentPageIndex(page);
        mPager.getReaderAdapter().unfreeze();
    }

    private boolean hasNextChapter() {
        if (mPager.isReverse()) {
            return mChapterId > 0;
        } else {
            return mChapterId < mMangaSummary.getChapters().size() - 1;
        }
    }

    private boolean hasPrevChapter() {
        if (mPager.isReverse()) {
            return mChapterId < mMangaSummary.getChapters().size() - 1;
        } else {
            return mChapterId > 0;
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
        mChapter = mMangaSummary.getChapters().get(mChapterId);
        new LoadPagesTask().execute();
    }

    private void showChaptersList() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setSingleChoiceItems(mMangaSummary.getChapters().getNames(), mChapterId, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mChapterId = which;
                mPageId = 0;
                mChapter = mMangaSummary.getChapters().get(mChapterId);
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
                    mChapterId += mPager.isReverse() ? 1 : -1;
                    mPageId = mPager.isReverse() ? 0 : -1;
                    loadChapter();
                }
                break;
            case OverScrollDetector.DIRECTION_RIGHT:
                if(hasNextChapter()){
                    mChapterId += mPager.isReverse() ? -1 : 1;
                    mPageId = mPager.isReverse() ? -1 : 0;
                    loadChapter();
                }
                break;
        }
    }

    @Override
    public void onOverScroll(int direction, float deltaX, float deltaY) {
        switch (direction) {
            case OverScrollDetector.DIRECTION_LEFT:
                mImageViewArrow.setTranslationX(Math.abs(deltaX) < mOverscrollSize ? -deltaX : mOverscrollSize);
//                mImageViewArrow.setTranslationX(overscrollSize);
                break;
            case OverScrollDetector.DIRECTION_RIGHT:
                mImageViewArrow.setTranslationX(Math.abs(deltaX) < mOverscrollSize ? -deltaX : -mOverscrollSize);
//                mImageViewArrow.setTranslationX(-overscrollSize);
                break;
            default:
                return;
        }
        double delta = Math.sqrt(deltaX * deltaX + deltaY * deltaY);
        delta = delta * mOverscrollSize / 240;
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
        animateSwipeViews(-mOverscrollSize);
    }
    //Triggered when overscroll swipe
    @Override
    public void onSwipeRight() {
        animateSwipeViews(mOverscrollSize);
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

    @SuppressLint("RtlHardcoded")
    @Override
    public void onPreOverScroll(int direction) {
        switch (direction) {
            case OverScrollDetector.DIRECTION_LEFT:
                setArrowPosition(Gravity.LEFT);
                mImageViewArrow.setRotation(-90);
                mTextViewNext.setText(getString(mPager.isReverse() ? R.string.next_chapter : R.string.prev_chapter,
                        mMangaSummary.chapters.get(mChapterId + (mPager.isReverse() ? 1 : -1)).name));
                break;
            case OverScrollDetector.DIRECTION_RIGHT:
                setArrowPosition(Gravity.RIGHT);
                mImageViewArrow.setRotation(90);
                mTextViewNext.setText(getString(mPager.isReverse() ? R.string.prev_chapter : R.string.next_chapter,
                        mMangaSummary.chapters.get(mChapterId + (mPager.isReverse() ? -1 : 1)).name));
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

    private class LoadPagesTask extends LoaderTask<Void, Void, ArrayList<MangaPage>> implements DialogInterface.OnCancelListener {

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
                        .setPositiveButton(R.string.retry, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                new LoadPagesTask().startLoading();
                            }
                        })
                        .setNegativeButton(R.string.close, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ReadActivity.this.finish();
                            }
                        }).create().show();
                return;
            }
            if (mPageId == -1) {
                mPageId = mangaPages.size() - 1;
            } else if (mPageId >= mangaPages.size()) {
                mPageId = 0;
            }
            mPager.getReaderAdapter().freeze();
            mPager.setPages(mangaPages, mPageId);
            mPager.getReaderAdapter().unfreeze();
            showcase(findViewById(R.id.imageView_menu), R.string.tip_reader_menu);
        }

        @Override
        protected ArrayList<MangaPage> doInBackground(Void... params) {
            try {
                MangaProvider provider;
                if (mMangaSummary.provider.equals(LocalMangaProvider.class)) {
                    provider = LocalMangaProvider.getInstance(ReadActivity.this);
                } else {
                    provider = (MangaProvider) mMangaSummary.provider.newInstance();
                }
                return provider.getPages(mChapter.readLink);
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

    private class SaveImageTask extends LoaderTask<MangaPage, Void, File> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mLoader.setVisibility(View.VISIBLE);
        }

        @Override
        protected File doInBackground(MangaPage... params) {
            try {
                MangaProvider provider;
                if (mMangaSummary.provider.equals(LocalMangaProvider.class)) {
                    provider = LocalMangaProvider.getInstance(ReadActivity.this);
                } else {
                    provider = (MangaProvider) mMangaSummary.provider.newInstance();
                }
                String url = provider.getPageImage(params[0]);
                File dest;
                if (url.startsWith("/") || url.startsWith("file://")) {
                    Bitmap bmp = BitmapFactory.decodeFile(url);
                    dest = new File(getExternalFilesDir("temp"), url.hashCode() + ".png");
                    FileOutputStream fos = new FileOutputStream(dest);
                    bmp.compress(Bitmap.CompressFormat.PNG, 100, fos);
                    fos.close();
                    bmp.recycle();
                    return dest;
                }
                dest = new File(getExternalFilesDir("temp"), url.hashCode() + "." + MimeTypeMap.getFileExtensionFromUrl(url));
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
            if (file != null && file.exists()) {
                saveImage(file);
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

    private class SwipeAnimationListener implements Animator.AnimatorListener {

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
