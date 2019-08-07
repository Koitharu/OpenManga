package org.nv95.openmanga.feature.read;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.snackbar.Snackbar;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AlertDialog;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.nv95.openmanga.R;
import org.nv95.openmanga.core.activities.BaseAppActivity;
import org.nv95.openmanga.feature.settings.main.SettingsActivity2;
import org.nv95.openmanga.feature.read.custom.ReaderMenu;
import org.nv95.openmanga.feature.read.reader.MangaReader;
import org.nv95.openmanga.feature.read.reader.OnOverScrollListener;
import org.nv95.openmanga.feature.read.reader.PageWrapper;
import org.nv95.openmanga.feature.read.dialog.HintDialog;
import org.nv95.openmanga.dialogs.NavigationListener;
import org.nv95.openmanga.feature.read.dialog.ThumbnailsDialog;
import org.nv95.openmanga.feature.read.util.BrightnessHelper;
import org.nv95.openmanga.helpers.ContentShareHelper;
import org.nv95.openmanga.helpers.MangaSaveHelper;
import org.nv95.openmanga.helpers.PermissionsHelper;
import org.nv95.openmanga.feature.read.util.ReaderConfig;
import org.nv95.openmanga.items.MangaChapter;
import org.nv95.openmanga.feature.manga.domain.MangaInfo;
import org.nv95.openmanga.items.MangaPage;
import org.nv95.openmanga.items.MangaSummary;
import org.nv95.openmanga.items.SimpleDownload;
import org.nv95.openmanga.lists.ChaptersList;
import org.nv95.openmanga.providers.BookmarksProvider;
import org.nv95.openmanga.providers.HistoryProvider;
import org.nv95.openmanga.providers.LocalMangaProvider;
import org.nv95.openmanga.providers.MangaProvider;
import org.nv95.openmanga.providers.staff.MangaProviderManager;
import org.nv95.openmanga.utils.ChangesObserver;
import org.nv95.openmanga.utils.InternalLinkMovement;
import org.nv95.openmanga.utils.LayoutUtils;
import org.nv95.openmanga.core.network.NetworkUtils;
import org.nv95.openmanga.utils.StorageUtils;
import org.nv95.openmanga.utils.WeakAsyncTask;

import java.io.File;
import java.util.List;

/**
 * Created by nv95 on 16.11.16.
 */

public class ReadActivity2 extends BaseAppActivity
        implements View.OnClickListener, ReaderMenu.Callback, OnOverScrollListener, NavigationListener, InternalLinkMovement.OnLinkClickListener {

    private static final int REQUEST_SETTINGS = 1299;

    private FrameLayout mProgressFrame;
    private MangaReader mReader;
    private ReaderMenu mMenuPanel;
    private ImageView mMenuButton;
    private FrameLayout mOverScrollFrame;
    private ImageView mOverScrollArrow;
    private TextView mOverScrollText;

    private MangaSummary mManga;
    private int mChapter;
    private ReaderConfig mConfig;
    private BrightnessHelper mBrightnessHelper;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reader2);
        mProgressFrame = findViewById(R.id.loader);
        mMenuPanel = findViewById(R.id.menuPanel);
        mReader = findViewById(R.id.reader);
        mMenuButton = findViewById(R.id.imageView_menu);
        mOverScrollFrame = findViewById(R.id.overscrollFrame);
        mOverScrollArrow = findViewById(R.id.imageView_arrow);
        mOverScrollText = findViewById(R.id.textView_title);

        if (isDarkTheme()) {
            mMenuButton.setColorFilter(ContextCompat.getColor(this, R.color.white_overlay_85));
        }
        mMenuButton.setOnClickListener(this);

        mBrightnessHelper = new BrightnessHelper(getWindow());
        mReader.initAdapter(this, this);
        mReader.addOnPageChangedListener(mMenuPanel);
        mReader.setOnOverScrollListener(this);

        Bundle extras = savedInstanceState != null ? savedInstanceState : getIntent().getExtras();
        mManga = new MangaSummary(extras);
        mChapter = extras.getInt("chapter", 0);
        int page = extras.getInt("page", 0);

        mMenuPanel.setData(mManga);
        mMenuPanel.setCallback(this);
        if (Build.VERSION.SDK_INT>= Build.VERSION_CODES.KITKAT) {
            mMenuPanel.setFitsSystemWindows(true);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                Window window = getWindow();
                int color = ContextCompat.getColor(this, R.color.transparent_dark);
                window.setStatusBarColor(color);
                window.setNavigationBarColor(color);
            }
        }
        updateConfig();
        new ChapterLoadTask(this, page).attach(this).start(mManga.getChapters().get(mChapter));
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            boolean panelIsVisible = mMenuPanel.isVisible();
            mMenuPanel.hide();
            if (!panelIsVisible) {
                onVisibilityChanged(false);
            }
        }
    }

    @Override
    protected void onPause() {
        saveHistory();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        mReader.finish();
        ChangesObserver.getInstance().emitOnHistoryChanged(mManga);
        super.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putAll(mManga.toBundle());
        outState.putInt("page", mReader.getCurrentPosition());
        outState.putInt("chapter", mChapter);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_SETTINGS:
                int pageIndex = mReader.getCurrentPosition();
                updateConfig();
                mReader.getLoader().setEnabled(false);
                mReader.scrollToPosition(pageIndex);
                mReader.getLoader().setEnabled(true);
                mReader.notifyDataSetChanged();
                break;
            case PermissionsHelper.REQUEST_CODE:
                if (resultCode == Activity.RESULT_OK) {
                    new ImageSaveTask(this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,
                            mReader.getItem(mReader.getCurrentPosition()));
                } else {
                    Snackbar.make((View) mReader, R.string.dir_no_access, Snackbar.LENGTH_SHORT).show();
                }
                break;
        }
    }

    private void saveHistory() {
        if (mChapter >= 0 && mChapter < mManga.chapters.size()) {
            HistoryProvider.getInstance(this).add(mManga, mManga.chapters.get(mChapter).number, mReader.getCurrentPosition());
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.imageView_menu:
                mMenuPanel.show();
                break;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_MENU) {
            if (mProgressFrame.getVisibility() != View.VISIBLE) {
                if (mMenuPanel.isShown()) {
                    mMenuPanel.hide();
                } else {
                    mMenuPanel.show();
                }
            }
            return super.onKeyDown(keyCode, event);
        }
        if (mConfig.scrollByVolumeKeys) {
            if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
                if (!mReader.scrollToNext(true)) {
                    if (mChapter < mManga.getChapters().size() - 1) {
                        mChapter++;
                        Toast t = Toast.makeText(this, mManga.getChapters().get(mChapter).name, Toast.LENGTH_SHORT);
                        t.setGravity(Gravity.TOP, 0, 0);
                        t.show();
                        new ChapterLoadTask(ReadActivity2.this,0)
                                .attach(ReadActivity2.this)
                                .start(mManga.getChapters().get(mChapter));
                        return true;
                    }
                } else {
                    return true;
                }
            } else if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
                if (!mReader.scrollToPrevious(true)) {
                    if (mChapter > 0) {
                        mChapter--;
                        Toast t = Toast.makeText(this, mManga.getChapters().get(mChapter).name, Toast.LENGTH_SHORT);
                        t.setGravity(Gravity.TOP, 0, 0);
                        t.show();
                        new ChapterLoadTask(ReadActivity2.this, -1)
                                .attach(ReadActivity2.this)
                                .start(mManga.getChapters().get(mChapter));
                        return true;
                    }
                } else {
                    return true;
                }
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        return mConfig.scrollByVolumeKeys &&
                (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN || keyCode == KeyEvent.KEYCODE_VOLUME_UP) ||
                super.onKeyUp(keyCode, event);
    }

    private void updateConfig() {
        mConfig = ReaderConfig.load(this, mManga);
        mReader.applyConfig(
                mConfig.scrollDirection == ReaderConfig.DIRECTION_VERTICAL,
                mConfig.scrollDirection == ReaderConfig.DIRECTION_REVERSED,
                mConfig.mode == ReaderConfig.MODE_PAGES,
                mConfig.showNumbers
        );
        if (mConfig.keepScreenOn) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        } else {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
        if (mConfig.hideMenuButton) {
            mMenuButton.setImageDrawable(null);
        } else {
            mMenuButton.setImageResource(R.drawable.ic_action_navigation_more_vert);
        }
        mReader.getLoader().setPreloadEnabled(mConfig.preload == ReaderConfig.PRELOAD_ALWAYS
                || (mConfig.preload == ReaderConfig.PRELOAD_WLAN_ONLY && MangaProviderManager.isWlan(this)));
        mReader.setScaleMode(mConfig.scaleMode);
        if (mConfig.adjustBrightness) {
            mBrightnessHelper.setBrightness(mConfig.brightnessValue);
        } else {
            mBrightnessHelper.reset();
        }
        mReader.setTapNavs(mConfig.tapNavs);
    }

    @Override
    public void onActionClick(int id) {
        final int pos = mReader.getCurrentPosition();
        switch (id) {
            case android.R.id.home:
                finish();
                break;
            case R.id.progressBar:
            case android.R.id.title:
                showChaptersList();
                break;
            case R.id.action_save:
                new MangaSaveHelper(this).confirmSave(mManga);
                break;
            case R.id.action_save_more:
                new LoadSourceTask(this).attach(this).start(mManga);
                break;
            case R.id.action_save_image:
                if (PermissionsHelper.accessCommonDir(this, Environment.DIRECTORY_PICTURES)) {
                    new ImageSaveTask(this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, mReader.getItem(pos));
                }
                break;
            case R.id.menuitem_thumblist:
                new ThumbnailsDialog(this, mReader.getLoader())
                        .setNavigationListener(this)
                        .show(pos);
                break;
            case R.id.action_webmode:
                updateConfig();
                HintDialog.showOnce(this, R.string.tip_webtoon);
                break;
            case R.id.nav_action_settings:
                SettingsActivity2.openReaderSettings(this, REQUEST_SETTINGS);
                break;
            case R.id.menuitem_bookmark:
                mMenuPanel.onBookmarkAdded(
                        BookmarksProvider.getInstance(this)
                                .add(mManga, mManga.chapters.get(mChapter).number, pos, mReader.getItem(pos).getFilename())
                );
                LayoutUtils.centeredToast(this, R.string.bookmark_added);
                break;
            case R.id.menuitem_unbookmark:
                if (BookmarksProvider.getInstance(this)
                        .remove(mManga, mManga.chapters.get(mChapter).number, pos)) {
                    mMenuPanel.onBookmarkRemoved(pos);
                    LayoutUtils.centeredToast(this, R.string.bookmark_removed);
                }
                break;
            case R.id.menuitem_rotation:
                int orientation = getResources().getConfiguration().orientation;
                setRequestedOrientation(orientation == Configuration.ORIENTATION_LANDSCAPE ?
                        ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT : ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
                break;
			/*case R.id.nav_left:
			case R.id.nav_right:
				int rd = getRealDirection(id == R.id.nav_left ? OnOverScrollListener.LEFT : OnOverScrollListener.RIGHT);
				if (rd == -1) {
					if (!mReader.scrollToPrevious(true)) {
						if (mChapter > 0) {
							mChapter--;
							Toast t = Toast.makeText(this, mManga.getChapters().get(mChapter).name, Toast.LENGTH_SHORT);
							t.setGravity(Gravity.TOP, 0, 0);
							t.show();
							new ChapterLoadTask(-1).startLoading(mManga.getChapters().get(mChapter));
						}
					}
				} else {
					if (!mReader.scrollToNext(true)) {
						if (mChapter < mManga.getChapters().size() - 1) {
							mChapter++;
							Toast t = Toast.makeText(this, mManga.getChapters().get(mChapter).name, Toast.LENGTH_SHORT);
							t.setGravity(Gravity.TOP, 0, 0);
							t.show();
							new ChapterLoadTask(0).startLoading(mManga.getChapters().get(mChapter));
						}
					}
				}
				break;*/
        }
    }

    @Override
    public void onPageChanged(int index) {
        mReader.scrollToPosition(index);
    }

    @Override
    public void onVisibilityChanged(boolean visible) {
        mMenuButton.setVisibility(visible ? View.INVISIBLE : View.VISIBLE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            View decorView = getWindow().getDecorView();
            if (visible) {
                decorView.setSystemUiVisibility(
                        View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
            } else {
                decorView.setSystemUiVisibility(
                        View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // прячем панель навигации
                                | View.SYSTEM_UI_FLAG_FULLSCREEN // прячем строку состояния
                                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
            }
        }
    }


    private void showChaptersList() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setSingleChoiceItems(mManga.getChapters().getNames(), mChapter, (dialog, which) -> {
            mMenuPanel.hide();
            mChapter = which;
            new ChapterLoadTask(ReadActivity2.this,0)
                    .attach(ReadActivity2.this)
                    .start(mManga.getChapters().get(mChapter));
            dialog.dismiss();
        });
        builder.setTitle(R.string.chapters_list);
        builder.create().show();
    }

    @Override
    public void onOverScrollFlying(int direction, int distance) {
        mOverScrollFrame.setAlpha(Math.abs(distance / 50f));
    }

    @Override
    public boolean onOverScrollFinished(int direction, int distance) {
        mOverScrollFrame.setVisibility(View.GONE);
        return true;
    }

    @SuppressLint("RtlHardcoded")
    @Override
    public void onOverScrollStarted(int direction) {
        if (getRealDirection(direction) == -1) {
            //prev chapter
            if (mChapter > 0) {
                mOverScrollText.setText(getString(R.string.prev_chapter, mManga.getChapters().get(mChapter - 1).name));
            } else {
                return;
            }
        } else {
            //next chapter
            if (mChapter < mManga.getChapters().size() - 1) {
                mOverScrollText.setText(getString(R.string.next_chapter, mManga.getChapters().get(mChapter + 1).name));
            } else {
                return;
            }
        }
        mOverScrollFrame.setAlpha(0f);
        mOverScrollFrame.setVisibility(View.VISIBLE);
        FrameLayout.LayoutParams overScrollTextLayoutParams = (FrameLayout.LayoutParams) mOverScrollText.getLayoutParams();
        FrameLayout.LayoutParams overScrollArrowLayoutParams = (FrameLayout.LayoutParams) mOverScrollArrow.getLayoutParams();
        if (direction == TOP) {
            overScrollTextLayoutParams.gravity = Gravity.CENTER;
            overScrollArrowLayoutParams.gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;
            mOverScrollArrow.setRotation(0f);
        } else if (direction == LEFT) {
            overScrollTextLayoutParams.gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;
            overScrollArrowLayoutParams.gravity = Gravity.LEFT | Gravity.CENTER_VERTICAL;
            mOverScrollArrow.setRotation(-90f);
        } else if (direction == BOTTOM) {
            overScrollTextLayoutParams.gravity = Gravity.CENTER;
            overScrollArrowLayoutParams.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
            mOverScrollArrow.setRotation(180f);
        } else if (direction == RIGHT) {
            overScrollTextLayoutParams.gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;
            overScrollArrowLayoutParams.gravity = Gravity.RIGHT | Gravity.CENTER_VERTICAL;
            mOverScrollArrow.setRotation(90f);
        }
    }

    @Override
    public void onOverScrolled(int direction) {
        if (mOverScrollFrame.getVisibility() != View.VISIBLE) {
            return;
        }
        mOverScrollFrame.setVisibility(View.GONE);
        int rd = getRealDirection(direction);
        mChapter += rd;
        new ChapterLoadTask(this, rd == -1 ? -1 : 0)
                .attach(this)
                .start(mManga.getChapters().get(mChapter));
    }

    private int getRealDirection(int direction) {
        return direction == TOP || direction == LEFT ? mReader.isReversed() ? 1 : -1 : mReader.isReversed() ? -1 : 1;
    }

    @Override
    public void onPageChange(int page) {
        mReader.scrollToPosition(page);
    }

    @Override
    public void onLinkClicked(TextView view, String scheme, String url) {
        switch (scheme) {
            case "app":
                switch (url) {
                    case "retry":
                        mReader.reload(mReader.getCurrentPosition());
                        break;
                }
                break;
        }
    }

    private static class ChapterLoadTask extends WeakAsyncTask<ReadActivity2, MangaChapter,Void,List<MangaPage>> implements DialogInterface.OnCancelListener {

        private final int mPageIndex;

        ChapterLoadTask(ReadActivity2 object, int page) {
            super(object);
            mPageIndex = page;
        }

        @Override
        protected void onPreExecute(@NonNull ReadActivity2 a) {
            a.mProgressFrame.setVisibility(View.VISIBLE);
            a.mReader.getLoader().cancelAll();
            a.mReader.getLoader().setEnabled(false);
        }

        @SuppressWarnings("ConstantConditions")
        @Override
        protected List<MangaPage> doInBackground(MangaChapter... mangaChapters) {
            try {
                MangaProvider provider = MangaProviderManager.instanceProvider(getObject(), mangaChapters[0].provider);
                return provider.getPages(mangaChapters[0].readLink);
            } catch (Exception ignored) {
                return null;
            }
        }

        private void onFailed(@NonNull final ReadActivity2 a) {
            new AlertDialog.Builder(a)
                    .setMessage(NetworkUtils.checkConnection(a) ? R.string.loading_error : R.string.no_network_connection)
                    .setTitle(R.string.app_name)
                    .setOnCancelListener(this)
                    .setPositiveButton(R.string.retry, (dialog, which) -> new ChapterLoadTask(a, mPageIndex)
                            .attach(a)
                            .start(a.mManga.getChapters().get(a.mChapter)))
                    .setNegativeButton(android.R.string.cancel, (dialog, which) -> dialog.cancel())
                    .create()
                    .show();
        }

        @Override
        protected void onPostExecute(@NonNull ReadActivity2 a, List<MangaPage> mangaPages) {
            if (mangaPages == null) {
                onFailed(a);
                return;
            }
            a.mReader.setPages(mangaPages);
            a.mReader.notifyDataSetChanged();
            int pos = mPageIndex == -1 ? a.mReader.getItemCount() - 1 : mPageIndex;
            a.mReader.scrollToPosition(pos);
            a.mReader.getLoader().setEnabled(true);
            a.mReader.notifyDataSetChanged();
            a.mMenuPanel.onChapterChanged(a.mManga.chapters.get(a.mChapter) ,mangaPages.size());
            a.mProgressFrame.setVisibility(View.GONE);
            a.showcase(a.mMenuButton, R.string.menu, R.string.tip_reader_menu);
        }

        @Override
        public void onCancel(DialogInterface dialogInterface) {
            ReadActivity2 a = getObject();
            if (a != null && a.mReader.getItemCount() == 0) {
                a.finish();
            }
        }
    }

    private static class ImageSaveTask extends WeakAsyncTask<ReadActivity2, PageWrapper,Void,File> {

        ImageSaveTask(ReadActivity2 object) {
            super(object);
        }

        @Override
        protected void onPreExecute(@Nullable ReadActivity2 a) {
            if (a != null) {
                a.mProgressFrame.setVisibility(View.VISIBLE);
                a.mMenuPanel.hide();
            }
        }

        @SuppressWarnings("ConstantConditions")
        @Override
        protected File doInBackground(PageWrapper... pageWrappers) {
            File dest;
            try {
                MangaProvider provider;
                provider = MangaProviderManager.instanceProvider(getObject(), pageWrappers[0].page.provider);
                String url = provider.getPageImage(pageWrappers[0].page);
                if (pageWrappers[0].isLoaded()) {
                    //noinspection ConstantConditions
                    dest = new File(pageWrappers[0].getFilename());
                } else {
                    dest = new File(getObject().getExternalFilesDir("temp"), String.valueOf(url.hashCode()));

                    final SimpleDownload dload = new SimpleDownload(url, dest);
                    dload.run();
                    if (!dload.isSuccess()) {
                        return null;
                    }
                }
                File dest2 = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), String.valueOf(url.hashCode()) + ".png");
                Bitmap b = BitmapFactory.decodeFile(dest.getPath());
                if (!StorageUtils.saveBitmap(b, dest2.getPath())) {
                    dest2 = null;
                }
                b.recycle();
                return dest2;
            } catch (Exception ignored) {
                return null;
            }
        }

        @Override
        protected void onPostExecute(@NonNull final ReadActivity2 a, final File file) {
            a.mProgressFrame.setVisibility(View.GONE);
            if (file != null && file.exists()) {
                StorageUtils.scanMediaFile(a, file);
                Snackbar.make(a.mMenuPanel, R.string.image_saved, Snackbar.LENGTH_LONG)
                        .setAction(R.string.action_share, v -> new ContentShareHelper(a).shareImage(file))
                        .show();
            } else {
                Snackbar.make(a.mMenuPanel, R.string.unable_to_save_image, Snackbar.LENGTH_SHORT).show();
            }

        }
    }

    private static class LoadSourceTask extends WeakAsyncTask<ReadActivity2, MangaInfo,Void,MangaSummary> {

        LoadSourceTask(ReadActivity2 object) {
            super(object);
        }

        @Override
        protected void onPreExecute(@NonNull ReadActivity2 a) {
            a.mProgressFrame.setVisibility(View.VISIBLE);
        }

        @Override
        protected MangaSummary doInBackground(MangaInfo... params) {
            try {
                return LocalMangaProvider.getInstance(getObject())
                        .getSource(params[0]);
            } catch (Exception e) {
                return null;
            }
        }

        @Override
        protected void onPostExecute(@NonNull ReadActivity2 a, MangaSummary sourceManga) {
            a.mProgressFrame.setVisibility(View.GONE);
            if (sourceManga == null) {
                Snackbar.make(a.mMenuPanel, R.string.loading_error, Snackbar.LENGTH_SHORT)
                        .show();
                return;
            }
            ChaptersList newChapters = sourceManga.chapters.complementByName(a.mManga.chapters);
            if (sourceManga.chapters.size() <= a.mManga.chapters.size()) {
                Snackbar.make(a.mMenuPanel, R.string.no_new_chapters, Snackbar.LENGTH_SHORT)
                        .show();
            } else {
                sourceManga.chapters = newChapters;
                new MangaSaveHelper(a).confirmSave(sourceManga, R.string.action_save_add);
            }
        }
    }
}
