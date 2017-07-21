package org.nv95.openmanga.activities;

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
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
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
import org.nv95.openmanga.components.ReaderMenu;
import org.nv95.openmanga.components.reader.MangaReader;
import org.nv95.openmanga.components.reader.OnOverScrollListener;
import org.nv95.openmanga.components.reader.PageWrapper;
import org.nv95.openmanga.components.reader.ReaderAdapter;
import org.nv95.openmanga.dialogs.HintDialog;
import org.nv95.openmanga.dialogs.NavigationListener;
import org.nv95.openmanga.dialogs.ThumbnailsDialog;
import org.nv95.openmanga.helpers.BrightnessHelper;
import org.nv95.openmanga.helpers.ContentShareHelper;
import org.nv95.openmanga.helpers.PermissionsHelper;
import org.nv95.openmanga.helpers.ReaderConfig;
import org.nv95.openmanga.items.MangaChapter;
import org.nv95.openmanga.items.MangaInfo;
import org.nv95.openmanga.items.MangaPage;
import org.nv95.openmanga.items.MangaSummary;
import org.nv95.openmanga.items.SimpleDownload;
import org.nv95.openmanga.lists.ChaptersList;
import org.nv95.openmanga.providers.BookmarksProvider;
import org.nv95.openmanga.providers.HistoryProvider;
import org.nv95.openmanga.providers.LocalMangaProvider;
import org.nv95.openmanga.providers.MangaProvider;
import org.nv95.openmanga.providers.staff.MangaProviderManager;
import org.nv95.openmanga.services.DownloadService;
import org.nv95.openmanga.utils.ChangesObserver;
import org.nv95.openmanga.utils.InternalLinkMovement;
import org.nv95.openmanga.utils.LayoutUtils;
import org.nv95.openmanga.utils.NetworkUtils;
import org.nv95.openmanga.utils.StorageUtils;
import org.nv95.openmanga.utils.WeakAsyncTask;

import java.io.File;
import java.util.List;

/**
 * Created by nv95 on 16.11.16.
 */

public class ReadActivity2 extends BaseAppActivity implements View.OnClickListener, ReaderMenu.Callback, OnOverScrollListener, NavigationListener, InternalLinkMovement.OnLinkClickListener {

    private static final int REQUEST_SETTINGS = 1299;

    private FrameLayout mContainer;
    private FrameLayout mProgressFrame;
    private MangaReader mReader;
    private ReaderAdapter mAdapter;
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
        mContainer = (FrameLayout) findViewById(R.id.container);
        mProgressFrame = (FrameLayout) findViewById(R.id.loader);
        mMenuPanel = (ReaderMenu) findViewById(R.id.menuPanel);
        mReader = (MangaReader) findViewById(R.id.reader);
        mMenuButton = (ImageView) findViewById(R.id.imageView_menu);
        mOverScrollFrame = (FrameLayout) findViewById(R.id.overscrollFrame);
        mOverScrollArrow = (ImageView) findViewById(R.id.imageView_arrow);
        mOverScrollText = (TextView) findViewById(R.id.textView_title);

        if (isDarkTheme()) {
            mMenuButton.setColorFilter(ContextCompat.getColor(this, R.color.white_overlay_85));
        }
        mMenuButton.setOnClickListener(this);

        mBrightnessHelper = new BrightnessHelper(getWindow());
        mAdapter = new ReaderAdapter(ReadActivity2.this, this);
        mReader.setAdapter(mAdapter);
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
        if (hasFocus && Build.VERSION.SDK_INT>= Build.VERSION_CODES.KITKAT && !mMenuPanel.isVisible()) {
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
    protected void onPause() {
        saveHistory();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        mAdapter.finish();
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
                mAdapter.getLoader().setEnabled(false);
                mReader.scrollToPosition(pageIndex);
                mAdapter.getLoader().setEnabled(true);
                mAdapter.notifyDataSetChanged();
                break;
            case PermissionsHelper.REQUEST_CODE:
                if (resultCode == Activity.RESULT_OK) {
                    new ImageSaveTask(this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,
                            mAdapter.getItem(mReader.getCurrentPosition()));
                } else {
                    Snackbar.make(mReader, R.string.dir_no_access, Snackbar.LENGTH_SHORT).show();
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
                mConfig.mode == ReaderConfig.MODE_PAGES
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
        mAdapter.getLoader().setPreloadEnabled(mConfig.preload == ReaderConfig.PRELOAD_ALWAYS
                || (mConfig.preload == ReaderConfig.PRELOAD_WLAN_ONLY && MangaProviderManager.isWlan(this)));
        mAdapter.setScaleMode(mConfig.scaleMode);
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
                DownloadService.start(this, mManga);
                break;
            case R.id.action_save_more:
                new LoadSourceTask(this).attach(this).start(mManga);
                break;
            case R.id.action_save_image:
                if (PermissionsHelper.accessCommonDir(this, Environment.DIRECTORY_PICTURES)) {
                    new ImageSaveTask(this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, mAdapter.getItem(pos));
                }
                break;
            case R.id.menuitem_thumblist:
                new ThumbnailsDialog(this, mAdapter.getLoader())
                        .setNavigationListener(this)
                        .show(pos);
                break;
            case R.id.action_webmode:
                updateConfig();
                HintDialog.showOnce(this, R.string.tip_webtoon);
                break;
            case R.id.nav_action_settings:
                startActivityForResult(new Intent(this, SettingsActivity.class)
                        .putExtra("section", SettingsActivity.SECTION_READER), REQUEST_SETTINGS);
                break;
            case R.id.menuitem_bookmark:
                mMenuPanel.onBookmarkAdded(
                        BookmarksProvider.getInstance(this)
                        .add(mManga, mManga.chapters.get(mChapter).number, pos, mAdapter.getItem(pos).getFilename())
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
        builder.setSingleChoiceItems(mManga.getChapters().getNames(), mChapter, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mMenuPanel.hide();
                mChapter = which;
                new ChapterLoadTask(ReadActivity2.this,0)
                        .attach(ReadActivity2.this)
                        .start(mManga.getChapters().get(mChapter));
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
    public void onOverScrollFlying(int direction, float factor) {
        mOverScrollFrame.setAlpha(Math.abs(factor / 50f));
    }

    @Override
    public void onOverScrollCancelled(int direction) {
        mOverScrollFrame.setVisibility(View.GONE);
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
        if (direction == TOP) {
            ((FrameLayout.LayoutParams)mOverScrollText.getLayoutParams()).gravity = Gravity.CENTER;
            ((FrameLayout.LayoutParams)mOverScrollArrow.getLayoutParams()).gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;
            mOverScrollArrow.setRotation(0f);
        } else if (direction == LEFT) {
            ((FrameLayout.LayoutParams)mOverScrollText.getLayoutParams()).gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;
            ((FrameLayout.LayoutParams)mOverScrollArrow.getLayoutParams()).gravity = Gravity.LEFT | Gravity.CENTER_VERTICAL;
            mOverScrollArrow.setRotation(-90f);
        } else if (direction == BOTTOM) {
            ((FrameLayout.LayoutParams)mOverScrollText.getLayoutParams()).gravity = Gravity.CENTER;
            ((FrameLayout.LayoutParams)mOverScrollArrow.getLayoutParams()).gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
            mOverScrollArrow.setRotation(180f);
        } else if (direction == RIGHT) {
            ((FrameLayout.LayoutParams)mOverScrollText.getLayoutParams()).gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;
            ((FrameLayout.LayoutParams)mOverScrollArrow.getLayoutParams()).gravity = Gravity.RIGHT | Gravity.CENTER_VERTICAL;
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
                        mAdapter.reload(mReader.getCurrentPosition());
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
            a.mAdapter.getLoader().cancelAll();
            a.mAdapter.getLoader().setEnabled(false);
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
                    .setPositiveButton(R.string.retry, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            new ChapterLoadTask(a, mPageIndex)
                                    .attach(a)
                                    .start(a.mManga.getChapters().get(a.mChapter));
                        }
                    })
                    .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    })
                    .create()
                    .show();
        }

        @Override
        protected void onPostExecute(@NonNull ReadActivity2 a, List<MangaPage> mangaPages) {
            if (mangaPages == null) {
                onFailed(a);
                return;
            }
            a.mAdapter.setPages(mangaPages);
            a.mAdapter.notifyDataSetChanged();
            int pos = mPageIndex == -1 ? a.mAdapter.getItemCount() - 1 : mPageIndex;
            a.mReader.scrollToPosition(pos);
            a.mAdapter.getLoader().setEnabled(true);
            a.mAdapter.notifyDataSetChanged();
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
        protected void onPostExecute(@Nullable final ReadActivity2 a, final File file) {
            if (a == null) {
                return;
            }
            a.mProgressFrame.setVisibility(View.GONE);
            if (file != null && file.exists()) {
                StorageUtils.scanMediaFile(a, file);
                Snackbar.make(a.mContainer, R.string.image_saved, Snackbar.LENGTH_LONG)
                        .setAction(R.string.action_share, new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                new ContentShareHelper(a).shareImage(file);
                            }
                        })
                        .show();
            } else {
                Snackbar.make(a.mContainer, R.string.unable_to_save_image, Snackbar.LENGTH_SHORT).show();
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
                Snackbar.make(a.mContainer, R.string.loading_error, Snackbar.LENGTH_SHORT)
                        .show();
                return;
            }
            ChaptersList newChapters = sourceManga.chapters.complementByName(a.mManga.chapters);
            if (sourceManga.chapters.size() <= a.mManga.chapters.size()) {
                Snackbar.make(a.mContainer, R.string.no_new_chapters, Snackbar.LENGTH_SHORT)
                        .show();
            } else {
                sourceManga.chapters = newChapters;
                DownloadService.start(a, sourceManga, R.string.action_save_add);
            }
        }
    }
}
