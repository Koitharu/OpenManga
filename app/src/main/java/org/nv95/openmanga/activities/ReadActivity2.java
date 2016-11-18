package org.nv95.openmanga.activities;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import org.nv95.openmanga.R;
import org.nv95.openmanga.components.ReaderMenuPanel;
import org.nv95.openmanga.components.reader.MangaReader;
import org.nv95.openmanga.components.reader.ReaderAdapter;
import org.nv95.openmanga.items.MangaChapter;
import org.nv95.openmanga.items.MangaPage;
import org.nv95.openmanga.items.MangaSummary;
import org.nv95.openmanga.providers.HistoryProvider;
import org.nv95.openmanga.providers.LocalMangaProvider;
import org.nv95.openmanga.providers.MangaProvider;
import org.nv95.openmanga.utils.ChangesObserver;

import java.util.List;

/**
 * Created by nv95 on 16.11.16.
 */

public class ReadActivity2 extends BaseAppActivity implements View.OnClickListener {

    private FrameLayout mContainer;
    private FrameLayout mProgressFrame;
    private MangaReader mReader;
    private ReaderAdapter mAdapter;
    private ReaderMenuPanel mMenuPanel;

    private MangaSummary mManga;
    private int mChapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reader2);
        enableTransparentStatusBar(android.R.color.transparent);
        mContainer = (FrameLayout) findViewById(R.id.container);
        mProgressFrame = (FrameLayout) findViewById(R.id.loader);
        mMenuPanel = (ReaderMenuPanel) findViewById(R.id.menuPanel);
        mReader = (MangaReader) findViewById(R.id.reader);

        ImageView imageViewMenu = (ImageView) findViewById(R.id.imageView_menu);
        assert imageViewMenu != null;
        if (isDarkTheme()) {
            imageViewMenu.setColorFilter(ContextCompat.getColor(this, R.color.white_overlay_85));
        }
        imageViewMenu.setOnClickListener(this);

        mAdapter = new ReaderAdapter();
        mReader.applyConfig(false, false, true);
        mReader.setAdapter(mAdapter);
        mReader.addOnPageChangedListener(mMenuPanel);
        mMenuPanel.setOnClickListener(this);

        Bundle extras = savedInstanceState != null ? savedInstanceState : getIntent().getExtras();
        mManga = new MangaSummary(extras);
        mChapter = extras.getInt("chapter", 0);
        int page = extras.getInt("page", 0);

        mMenuPanel.setData(mManga);
        new ChapterLoadTask(page).startLoading(mManga.getChapters().get(mChapter));
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
            case R.id.menuitem_rotation:
                setRequestedOrientation(
                        getRequestedOrientation() != ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE ? ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE : ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                break;
        }
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
            mReader.scrollToPosition(mPageIndex == -1 ? mAdapter.getItemCount() : mPageIndex);
            mAdapter.getLoader().setEnabled(true);
            mProgressFrame.setVisibility(View.GONE);
        }
    }
}
