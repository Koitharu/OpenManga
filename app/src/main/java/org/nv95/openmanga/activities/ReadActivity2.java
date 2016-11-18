package org.nv95.openmanga.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.webkit.MimeTypeMap;
import android.widget.FrameLayout;
import android.widget.ImageView;

import org.nv95.openmanga.R;
import org.nv95.openmanga.components.ReaderMenu;
import org.nv95.openmanga.components.reader.MangaReader;
import org.nv95.openmanga.components.reader.PageWrapper;
import org.nv95.openmanga.components.reader.ReaderAdapter;
import org.nv95.openmanga.dialogs.ChaptersSelectDialog;
import org.nv95.openmanga.helpers.BrightnessHelper;
import org.nv95.openmanga.helpers.ContentShareHelper;
import org.nv95.openmanga.helpers.ReaderConfig;
import org.nv95.openmanga.items.MangaChapter;
import org.nv95.openmanga.items.MangaInfo;
import org.nv95.openmanga.items.MangaPage;
import org.nv95.openmanga.items.MangaSummary;
import org.nv95.openmanga.items.SimpleDownload;
import org.nv95.openmanga.lists.ChaptersList;
import org.nv95.openmanga.providers.HistoryProvider;
import org.nv95.openmanga.providers.LocalMangaProvider;
import org.nv95.openmanga.providers.MangaProvider;
import org.nv95.openmanga.providers.staff.MangaProviderManager;
import org.nv95.openmanga.services.DownloadService;
import org.nv95.openmanga.utils.ChangesObserver;
import org.nv95.openmanga.utils.MangaStore;
import org.nv95.openmanga.utils.StorageUtils;

import java.io.File;
import java.util.List;

/**
 * Created by nv95 on 16.11.16.
 */

public class ReadActivity2 extends BaseAppActivity implements View.OnClickListener, ReaderMenu.Callback {

    private static final int REQUEST_SETTINGS = 1299;

    private FrameLayout mContainer;
    private FrameLayout mProgressFrame;
    private MangaReader mReader;
    private ReaderAdapter mAdapter;
    private ReaderMenu mMenuPanel;
    private ImageView mMenuButton;

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

        if (isDarkTheme()) {
            mMenuButton.setColorFilter(ContextCompat.getColor(this, R.color.white_overlay_85));
        }
        mMenuButton.setOnClickListener(this);

        mBrightnessHelper = new BrightnessHelper(getWindow());
        mAdapter = new ReaderAdapter();
        mReader.setAdapter(mAdapter);
        mReader.addOnPageChangedListener(mMenuPanel);

        Bundle extras = savedInstanceState != null ? savedInstanceState : getIntent().getExtras();
        mManga = new MangaSummary(extras);
        mChapter = extras.getInt("chapter", 0);
        int page = extras.getInt("page", 0);

        mMenuPanel.setData(mManga);
        mMenuPanel.setCallback(this);
        updateConfig();
        new ChapterLoadTask(page).startLoading(mManga.getChapters().get(mChapter));
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        /*if (hasFocus && Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }*/
    }

    @Override
    protected void onPause() {
        saveHistory();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
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
            onClick(mMenuButton);
            return super.onKeyDown(keyCode, event);
        }
        if (mConfig.scrollByVolumeKeys) {
            if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
                mReader.scrollToNext(true);
                return true;
            } else if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
                mReader.scrollToPrevious(true);
                return true;
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
        mConfig = ReaderConfig.load(this);
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
        mAdapter.getLoader().setPreloadEnabled(mConfig.preload == ReaderConfig.PRELOAD_ALWAYS
                || (mConfig.preload == ReaderConfig.PRELOAD_WLAN_ONLY && MangaProviderManager.isWlan(this)));
        mAdapter.setScaleMode(mConfig.scaleMode);
        if (mConfig.adjustBrightness) {
            mBrightnessHelper.setBrightness(mConfig.brightnessValue);
        } else {
            mBrightnessHelper.reset();
        }
    }

    @Override
    public void onActionClick(int id) {
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
                new LoadSourceTask().startLoading(mManga);
                break;
            case R.id.action_save_image:
                new ImageSaveTask().startLoading(mAdapter.getItem(mReader.getCurrentPosition()));
                break;
            case R.id.menuitem_settings:
                startActivityForResult(new Intent(this, SettingsActivity.class)
                        .putExtra("section", SettingsActivity.SECTION_READER), REQUEST_SETTINGS);
                break;
            case R.id.menuitem_rotation:
                int orientation = getResources().getConfiguration().orientation;
                setRequestedOrientation(orientation == Configuration.ORIENTATION_LANDSCAPE ?
                        ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT : ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
                break;
        }
    }

    @Override
    public void onPageChanged(int index) {
        mReader.scrollToPosition(index);
    }

    @Override
    public void onVisibilityChanged(boolean visible) {
        mMenuButton.setVisibility(visible ? View.INVISIBLE : View.VISIBLE);
    }


    private void showChaptersList() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setSingleChoiceItems(mManga.getChapters().getNames(), mChapter, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mMenuPanel.hide();
                mChapter = which;
                new ChapterLoadTask(0).startLoading(mManga.getChapters().get(mChapter));
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

    private class ChapterLoadTask extends LoaderTask<MangaChapter,Void,List<MangaPage>> {

        private final int mPageIndex;

        private ChapterLoadTask(int page) {
            mPageIndex = page;
        }

        @Override
        protected void onPreExecute() {
            mProgressFrame.setVisibility(View.VISIBLE);
            super.onPreExecute();
        }

        @Override
        protected List<MangaPage> doInBackground(MangaChapter... mangaChapters) {
            try {
                MangaProvider provider;
                if (mangaChapters[0].provider.equals(LocalMangaProvider.class)) {
                    provider = LocalMangaProvider.getInstance(ReadActivity2.this);
                } else {
                    provider = (MangaProvider) mangaChapters[0].provider.newInstance();
                }
                return provider.getPages(mangaChapters[0].readLink);
            } catch (Exception ignored) {
                return null;
            }
        }

        @Override
        protected void onPostExecute(List<MangaPage> mangaPages) {
            super.onPostExecute(mangaPages);
            mAdapter.getLoader().setEnabled(false);
            mAdapter.setPages(mangaPages);
            mAdapter.notifyDataSetChanged();
            int pos = mPageIndex == -1 ? mAdapter.getItemCount() : mPageIndex;
            mReader.scrollToPosition(pos);
            mAdapter.getLoader().setEnabled(true);
            mAdapter.notifyItemChanged(pos);
            mMenuPanel.setChapterSize(mangaPages.size());
            mProgressFrame.setVisibility(View.GONE);
        }
    }

    private class ImageSaveTask extends LoaderTask<PageWrapper,Void,File> {

        @Override
        protected void onPreExecute() {
            mProgressFrame.setVisibility(View.VISIBLE);
            super.onPreExecute();
        }

        @Override
        protected File doInBackground(PageWrapper... pageWrappers) {
            if (pageWrappers[0].isLoaded()) {
                //noinspection ConstantConditions
                return new File(pageWrappers[0].getFilename());
            }
            try {
                MangaProvider provider;
                provider = pageWrappers[0].page.provider.newInstance();
                String url = provider.getPageImage(pageWrappers[0].page);
                File dest;
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
            mProgressFrame.setVisibility(View.GONE);
            if (file != null && file.exists()) {
                final File destFile = StorageUtils.saveToGallery(ReadActivity2.this, file);
                if (destFile != null) {
                    Snackbar.make(mContainer, R.string.image_saved, Snackbar.LENGTH_LONG)
                            .setAction(R.string.action_share, new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    new ContentShareHelper(ReadActivity2.this).shareImage(destFile);
                                }
                            })
                            .show();
                } else {
                    Snackbar.make(mContainer, R.string.unable_to_save_image, Snackbar.LENGTH_SHORT).show();
                }
            } else {
                Snackbar.make(mContainer, R.string.image_loading_error, Snackbar.LENGTH_SHORT).show();
            }

        }
    }

    private class LoadSourceTask extends LoaderTask<MangaInfo,Void,MangaSummary> {

        @Override
        protected void onPreExecute() {
            mProgressFrame.setVisibility(View.VISIBLE);
            super.onPreExecute();
        }

        @Override
        protected MangaSummary doInBackground(MangaInfo... params) {
            return LocalMangaProvider.getInstance(ReadActivity2.this)
                    .getSource(params[0]);
        }

        @Override
        protected void onPostExecute(MangaSummary sourceManga) {
            super.onPostExecute(sourceManga);
            mProgressFrame.setVisibility(View.GONE);
            if (sourceManga == null) {
                Snackbar.make(mContainer, R.string.loading_error, Snackbar.LENGTH_SHORT)
                        .show();
                return;
            }
            ChaptersList newChapters = sourceManga.chapters.complementByName(mManga.chapters);
            if (sourceManga.chapters.size() <= mManga.chapters.size()) {
                Snackbar.make(mContainer, R.string.no_new_chapters, Snackbar.LENGTH_SHORT)
                        .show();
            } else {
                sourceManga.chapters = newChapters;
                DownloadService.start(ReadActivity2.this, sourceManga, R.string.action_save_add);
            }
        }

    }
}
