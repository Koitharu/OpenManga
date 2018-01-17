package org.nv95.openmanga.reader;

import android.annotation.TargetApi;
import android.app.LoaderManager;
import android.content.Loader;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.AppCompatSeekBar;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import org.nv95.openmanga.R;
import org.nv95.openmanga.core.models.MangaChapter;
import org.nv95.openmanga.core.models.MangaDetails;
import org.nv95.openmanga.core.models.MangaHistory;
import org.nv95.openmanga.core.models.MangaPage;
import org.nv95.openmanga.core.storage.db.HistoryRepository;
import org.nv95.openmanga.AppBaseActivity;
import org.nv95.openmanga.preview.ChaptersListAdapter;
import org.nv95.openmanga.reader.pager.PagerReaderFragment;
import org.nv95.openmanga.reader.thumbview.OnThumbnailClickListener;
import org.nv95.openmanga.reader.thumbview.ThumbViewFragment;
import org.nv95.openmanga.common.utils.AnimationUtils;

import java.util.ArrayList;

/**
 * Created by koitharu on 08.01.18.
 */

public final class ReaderActivity extends AppBaseActivity implements View.OnClickListener,
		SeekBar.OnSeekBarChangeListener, ChaptersListAdapter.OnChapterClickListener,
		LoaderManager.LoaderCallbacks<ArrayList<MangaPage>>, View.OnSystemUiVisibilityChangeListener,
		ReaderCallback, OnThumbnailClickListener {

	private ImmersiveFrameLayout mRoot;
	private AppCompatSeekBar mSeekBar;
	private ViewGroup mContentPanel;
	private Toolbar mToolbar;
	private RelativeLayout mBottomBar;
	private TextView mTextViewPage;

	private ReaderFragment mReader;

	private HistoryRepository mHistoryRepository;
	private MangaDetails mManga;
	private MangaChapter mChapter;
	private ArrayList<MangaPage> mPages;

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_reader);
		mToolbar = findViewById(R.id.toolbar);
		setSupportActionBar(mToolbar);
		enableHomeAsUp();

		mRoot = findViewById(R.id.root);
		mSeekBar = findViewById(R.id.seekBar);
		mContentPanel = findViewById(R.id.contentPanel);
		mBottomBar = findViewById(R.id.bottomBar);
		mTextViewPage = findViewById(R.id.textView_page);

		mSeekBar.setOnSeekBarChangeListener(this);
		findViewById(R.id.action_menu).setOnClickListener(this);
		findViewById(R.id.action_thumbnails).setOnClickListener(this);

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
			mRoot.setFitsSystemWindows(true);
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
				Window window = getWindow();
				int color = ContextCompat.getColor(this, R.color.transparent_dark);
				window.setStatusBarColor(color);
				window.setNavigationBarColor(color);
			}
		}
		mManga = getIntent().getParcelableExtra("manga");
		mChapter = getIntent().getParcelableExtra("chapter");
		assert mManga != null && mChapter != null;

		mReader = new PagerReaderFragment();
		getFragmentManager().beginTransaction()
				.replace(R.id.reader, mReader)
				.commit();
		mHistoryRepository = new HistoryRepository(this);
		setTitle(mManga.name);
		setSubtitle(mChapter.name);
		getLoaderManager().initLoader(0, mChapter.toBundle(), this).forceLoad();
	}

	@Override
	protected void onPause() {
		if (mHistoryRepository != null && mPages != null) {
			if (!mHistoryRepository.quickUpdate(mManga, mChapter, mReader.getCurrentPage())) {
				addToHistory();
			}

		}
		super.onPause();
	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
		if (hasFocus && Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && mToolbar.getVisibility() != View.VISIBLE) {
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
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.options_reader, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.action_chapters) {
			final ChaptersDialogFragment dialogFragment = new ChaptersDialogFragment();
			final Bundle args = new Bundle();
			args.putParcelableArrayList("chapters", mManga.chapters);
			args.putLong("current_id", mChapter.id);
			dialogFragment.setArguments(args);
			dialogFragment.show(getSupportFragmentManager(), "chapters_list");
			return true;
		} else return super.onOptionsItemSelected(item);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.action_menu:

				break;
			case R.id.action_thumbnails:
				final ThumbViewFragment dialogFragment = new ThumbViewFragment();
				final Bundle args = new Bundle();
				args.putParcelableArrayList("pages", mPages);
				dialogFragment.setArguments(args);
				dialogFragment.show(getSupportFragmentManager(), "thumb_view");
				break;
		}
	}

	@Override
	public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
		mTextViewPage.setText(getString(R.string.page_x_of_n, progress + 1, seekBar.getMax() + 1));
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {

	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
		mReader.scrollToPage(seekBar.getProgress());
	}

	@Override
	public void onChapterClick(int pos, MangaChapter chapter) {
		android.support.v4.app.Fragment f = getSupportFragmentManager().findFragmentByTag("chapters_list");
		if (f != null && f instanceof DialogFragment) {
			((DialogFragment) f).dismiss();
		}
		AnimationUtils.setVisibility(mContentPanel, View.VISIBLE);
		mChapter = chapter;
		setSubtitle(mChapter.name);

		getLoaderManager().restartLoader(0, mChapter.toBundle(), this).forceLoad();
	}

	@Override
	public boolean onChapterLongClick(int pos, MangaChapter chapter) {
		return false;
	}

	@NonNull
	@Override
	public Loader<ArrayList<MangaPage>> onCreateLoader(int id, Bundle args) {
		return new ChapterLoader(this, MangaChapter.from(args));
	}

	@Override
	public void onLoadFinished(Loader<ArrayList<MangaPage>> loader, ArrayList<MangaPage> data) {
		AnimationUtils.setVisibility(mContentPanel, View.GONE);
		if (data == null) {
			//TODO
			return;
		}
		mPages = data;
		mSeekBar.setMax(mPages.size() - 1);
		mSeekBar.setProgress(0);
		mReader.setPages(mPages);
		mReader.scrollToPage(0);
		addToHistory();
	}

	@Override
	public void onLoaderReset(Loader<ArrayList<MangaPage>> loader) {

	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (isFinishing()) {
			return super.onKeyDown(keyCode, event);
		}
		switch (keyCode) {
			case KeyEvent.KEYCODE_VOLUME_UP:
				//TODO
				return true;
			case KeyEvent.KEYCODE_VOLUME_DOWN:
				//TODO
				return true;
			default:
				return super.onKeyDown(keyCode, event);
		}
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		if (isFinishing()) {
			return super.onKeyUp(keyCode, event);
		}
		switch (keyCode) {
			case KeyEvent.KEYCODE_MENU:
				toggleUi();
				return true;
			case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
				//TODO
				return true;
			case KeyEvent.KEYCODE_VOLUME_UP:
			case KeyEvent.KEYCODE_VOLUME_DOWN:
				return true;
			default:
				return super.onKeyUp(keyCode, event);
		}
	}

	@TargetApi(Build.VERSION_CODES.KITKAT)
	@Override
	public void onSystemUiVisibilityChange(int visibility) {
		if ((visibility & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0) {
			getWindow().getDecorView().setSystemUiVisibility(
					View.SYSTEM_UI_FLAG_LAYOUT_STABLE
							| View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
							| View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
							| View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
							| View.SYSTEM_UI_FLAG_FULLSCREEN
							| View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
		}
	}

	private void showUi() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
			getWindow().getDecorView().setSystemUiVisibility(
					View.SYSTEM_UI_FLAG_LAYOUT_STABLE
							| View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
							| View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
		}
		AnimationUtils.setVisibility(mToolbar, View.VISIBLE);
		AnimationUtils.setVisibility(mBottomBar, View.VISIBLE);
	}

	private void hideUi() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
			getWindow().getDecorView().setSystemUiVisibility(
					View.SYSTEM_UI_FLAG_LAYOUT_STABLE
							| View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
							| View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
							| View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
							| View.SYSTEM_UI_FLAG_FULLSCREEN
							| View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
		}
		AnimationUtils.setVisibility(mToolbar, View.GONE);
		AnimationUtils.setVisibility(mBottomBar, View.GONE);
	}

	private void addToHistory() {
		final MangaHistory history = new MangaHistory(mManga, mChapter, mManga.chapters.size(), mReader.getCurrentPage(), (short) 0);
		if (!mHistoryRepository.add(history)) {
			mHistoryRepository.update(history);
		}
	}

	@Override
	public void onPageChanged(int page) {
		mSeekBar.setProgress(page);
	}

	public void toggleUi() {
		if (mToolbar.getVisibility() == View.VISIBLE) {
			hideUi();
		} else {
			showUi();
		}
	}

	@Override
	public void onThumbnailClick(int position) {
		mReader.scrollToPage(position);
	}
}
