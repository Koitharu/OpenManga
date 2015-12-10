
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
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.PopupMenu;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.nv95.openmanga.components.AdvancedViewPager;
import org.nv95.openmanga.components.ErrorReporter;
import org.nv95.openmanga.components.SimpleAnimator;
import org.nv95.openmanga.providers.FavouritesProvider;
import org.nv95.openmanga.providers.HistoryProvider;
import org.nv95.openmanga.providers.LocalMangaProvider;
import org.nv95.openmanga.providers.MangaChapter;
import org.nv95.openmanga.providers.MangaPage;
import org.nv95.openmanga.providers.MangaProvider;
import org.nv95.openmanga.providers.MangaSummary;
import org.nv95.openmanga.providers.SaveService;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by nv95 on 30.09.15.
 *
 */
public class ReadActivity extends AppCompatActivity implements View.OnClickListener,
        ViewPager.OnPageChangeListener, ReaderOptionsDialog.OnOptionsChangedListener,
        NavigationDialog.NavigationListener, AdvancedViewPager.OnScrollListener {

    private AdvancedViewPager pager;
    private MangaSummary mangaSummary;
    private MangaChapter chapter;
    private int chapterId;
    private int pageId;
    private TextView chapterTitleTextView;
    private ProgressBar chapterProgressBar;
    private ImageView oversrollImageView;
    private boolean toolbars = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reader);
        pager = (AdvancedViewPager) findViewById(R.id.pager);
        chapterTitleTextView = (TextView) findViewById(R.id.textView_title);
        chapterProgressBar = (ProgressBar) findViewById(R.id.progressBar_reading);
        oversrollImageView = (ImageView) findViewById(R.id.imageViewOverscroll);

        findViewById(R.id.imageView_menu).setOnClickListener(this);
        findViewById(R.id.block_chapters).setOnClickListener(this);
        findViewById(R.id.toolbutton_fav).setOnClickListener(this);
        findViewById(R.id.toolbutton_img).setOnClickListener(this);
        findViewById(R.id.toolbutton_nav).setOnClickListener(this);
        findViewById(R.id.toolbutton_back).setOnClickListener(this);
        findViewById(R.id.toolbutton_opt).setOnClickListener(this);
        findViewById(R.id.toolbutton_save).setOnClickListener(this);
        findViewById(R.id.toolbutton_share).setOnClickListener(this);

        pager.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                hideToolbars();
                return false;
            }
        });
        pager.setOnScrollListener(this);
        pager.addOnPageChangeListener(this);
        onOptionsChanged();
        mangaSummary = new MangaSummary(getIntent().getExtras());
        if (savedInstanceState != null) {
            chapterId = savedInstanceState.getInt("chapter");
            pageId = savedInstanceState.getInt("page");
        } else {
            chapterId = getIntent().getIntExtra("chapter", 0);
            pageId = getIntent().getIntExtra("page", 0);
        }
        chapter = mangaSummary.getChapters().get(chapterId);
        pager.setOffscreenPageLimit(3);
        chapterTitleTextView.setText(chapter.getName());
        if (FavouritesProvider.Has(this, mangaSummary)) {
            ((ImageView) findViewById(R.id.toolbutton_fav)).setImageResource(R.drawable.ic_tool_favorite);
            findViewById(R.id.toolbutton_fav).setContentDescription(getString(R.string.action_unfavourite));
        }
        new LoadPagesTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    @Override
    protected void onPause() {
        HistoryProvider.addToHistory(this, mangaSummary, chapterId, pager.getCurrentItem());
        super.onPause();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("page", pageId);
        outState.putInt("chapter", chapterId);
        outState.putBoolean("toolbars", toolbars);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState.getBoolean("toolbars")) {
            showToolbars();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.imageView_menu:
                showToolbars();
                break;
            case R.id.block_chapters:
                showChaptersList();
                break;
            case R.id.toolbutton_nav:
                new NavigationDialog(this, pager.getAdapter().getCount(), pager.getCurrentItem())
                        .setNavigationListener(this).show();
                break;
            case R.id.toolbutton_back:
                finish();
                break;
            case R.id.toolbutton_save:
                if (LocalMangaProvider.class.equals(mangaSummary.getProvider())) {
                    Toast toast = Toast.makeText(this, R.string.already_saved, Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                    break;
                }
                SaveService.Save(this, mangaSummary);
                hideToolbars();
                break;
            case R.id.toolbutton_fav:
                ImageView favbtn = (ImageView) findViewById(R.id.toolbutton_fav);
                FavouritesProvider favouritesProvider = FavouritesProvider.getInstacne(this);
                if (favouritesProvider.has(mangaSummary)) {
                    if (favouritesProvider.remove(mangaSummary)) {
                        favbtn.setImageResource(R.drawable.ic_tool_favorite_outline);
                        favbtn.setContentDescription(getString(R.string.action_unfavourite));
                    }
                } else {
                    if (favouritesProvider.add(mangaSummary)) {
                        favbtn.setImageResource(R.drawable.ic_tool_favorite);
                        favbtn.setContentDescription(getString(R.string.action_favourite));
                    }
                }
                break;
            case R.id.toolbutton_share:
                hideToolbars();
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("text/plain");
                intent.putExtra(Intent.EXTRA_TEXT, mangaSummary.getReadLink());
                intent.putExtra(android.content.Intent.EXTRA_SUBJECT, mangaSummary.getName());
                startActivity(Intent.createChooser(intent, getString(R.string.action_share)));
                break;
            case R.id.toolbutton_opt:
                new ReaderOptionsDialog(this).setOptionsChangedListener(this).show();
                break;
            case R.id.toolbutton_img:
                PagerReaderAdapter adapter = (PagerReaderAdapter) pager.getAdapter();
                if (adapter == null)
                    break;
                final MangaPage page = adapter.getItem(pager.getCurrentItem());
                PopupMenu popupMenu = new PopupMenu(this,v);
                popupMenu.inflate(R.menu.popup_image);
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        File file = new File(page.getPath());
                        if (!file.exists()) {
                            file = new File(ReadActivity.this.getExternalCacheDir(), String.valueOf(page.getPath().hashCode()));
                        }
                        if (!file.exists()) {
                            new ErrorReporter(getApplicationContext()).report("# ReadActivity.SaveImg.NotFound\n page.path: " + page.getPath());
                            Toast.makeText(getApplicationContext(), R.string.file_not_found, Toast.LENGTH_SHORT).show();
                            return true;
                        }
                        switch (item.getItemId()) {
                            case R.id.action_img_save: //save
                                SaveImage(file, page);
                                return true;
                            case R.id.action_img_share: //share
                                try {
                                    File dest = new File(getExternalFilesDir("temp"), page.getPath().substring(page.getPath().lastIndexOf('/') + 1));
                                    LocalMangaProvider.CopyFile(file, dest);
                                    Intent shareIntent = new Intent();
                                    shareIntent.setAction(Intent.ACTION_SEND);
                                    shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(dest));
                                    shareIntent.setType("image/jpeg");
                                    startActivity(Intent.createChooser(shareIntent, getString(R.string.action_share)));
                                } catch (Exception e) {
                                    new ErrorReporter(getApplicationContext()).report(e);
                                }
                                return true;
                            default:
                                return false;
                        }
                    }
                });
                popupMenu.show();
                break;
        }
    }

    private void SaveImage(File file, MangaPage page) {
        File dest = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),page.getPath().substring(page.getPath().lastIndexOf('/') + 1));
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
            Toast.makeText(ReadActivity.this, R.string.image_saved, Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            Toast.makeText(ReadActivity.this, R.string.unable_to_save_image, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        chapterProgressBar.setProgress(position);
        pageId = position;
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    @Override
    public void onOptionsChanged() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        pager.setOrientation(prefs.getInt("scroll_direction", 0) == 0 ? AdvancedViewPager.HORIZONTAL : AdvancedViewPager.VERTICAL);
        if (prefs.getBoolean("keep_screen", false)) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        } else {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
    }

    @Override
    public void onPageChange(int page) {
        hideToolbars();
        pager.setCurrentItem(page, false);
    }

    @Override
    public void OnScroll(AdvancedViewPager viewPager, int x, int y, int oldx, int oldy) {

    }

    @Override
    public boolean OnOverScroll(AdvancedViewPager viewPager, int deltaX, int direction) {
        if (deltaX == 0) {
            FrameLayout.LayoutParams params = ((FrameLayout.LayoutParams)oversrollImageView.getLayoutParams());
            if (direction == -1 && chapterId > 0) {
                params.gravity = Gravity.CENTER_VERTICAL | Gravity.LEFT;
                params.leftMargin = -oversrollImageView.getWidth();
                oversrollImageView.setTranslationX(0);
                oversrollImageView.setVisibility(View.VISIBLE);
                oversrollImageView.getParent().requestLayout();
                return true;
            } else if (direction == 1 && chapterId < mangaSummary.getChapters().size() - 1) {
                params.gravity = Gravity.CENTER_VERTICAL | Gravity.RIGHT;
                params.rightMargin = -oversrollImageView.getWidth();
                oversrollImageView.setTranslationX(0);
                oversrollImageView.setVisibility(View.VISIBLE);
                oversrollImageView.getParent().requestLayout();
                return true;
            } else if (direction == 0){
                if (oversrollImageView.getTag() != null) {
                    float scrollFactor = (float) oversrollImageView.getTag();
                    if (scrollFactor > 0.4) {
                        new SimpleAnimator(oversrollImageView).forceGravity(Gravity.CENTER).hide();
                        if (params.gravity == Gravity.CENTER_VERTICAL + Gravity.RIGHT) {
                            chapterId++;
                        } else {
                            chapterId--;
                        }
                        //TODO::switchpage
                        pageId = 0;
                        chapter = mangaSummary.getChapters().get(chapterId);
                        chapterTitleTextView.setText(chapter.getName());
                        hideToolbars();
                        new LoadPagesTask().execute();
                        return true;
                    }
                }
                new SimpleAnimator(oversrollImageView).hide();
                return false;
            } else {
                return false;
            }
        }
        float scrollFactor = deltaX/2;
        oversrollImageView.setTranslationX(-direction * scrollFactor);
        scrollFactor = scrollFactor / viewPager.getWidth() + 0.1f;
        if (scrollFactor > 0.5f) {
            scrollFactor = 0.5f;
        }
        oversrollImageView.setColorFilter(Color.argb((int) (500*(0.5f-scrollFactor)),57,73,171));
        oversrollImageView.setRotation(scrollFactor * 360 - (direction == 1 ? 180 : 0));
        oversrollImageView.setTag(scrollFactor);
        return true;
    }


    private class LoadPagesTask extends AsyncTask<Void,Void,ArrayList<MangaPage>> implements DialogInterface.OnCancelListener {
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
            chapterProgressBar.setMax(mangaPages.size());
            chapterProgressBar.setProgress(pageId);
            pager.setAdapter(new PagerReaderAdapter(ReadActivity.this, mangaPages));
            pager.setCurrentItem(pageId, false);
        }

        @Override
        protected ArrayList<MangaPage> doInBackground(Void... params) {
            try {
                MangaProvider provider;
                if (mangaSummary.getProvider().equals(LocalMangaProvider.class)) {
                    provider = LocalMangaProvider.getInstacne(ReadActivity.this);
                } else {
                    provider = (MangaProvider) mangaSummary.getProvider().newInstance();
                }
                return provider.getPages(chapter.getReadLink());
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

    private void showChaptersList() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, mangaSummary.getChapters().getNames()), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                chapterId = which;
                pageId = 0;
                chapter = mangaSummary.getChapters().get(chapterId);
                chapterTitleTextView.setText(chapter.getName());
                hideToolbars();
                new LoadPagesTask().execute();
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

    protected void showToolbars() {
        if (!toolbars) {
            toolbars = true;
            findViewById(R.id.imageView_menu).setVisibility(View.GONE);
            new SimpleAnimator(findViewById(R.id.toolbar_top)).show();
            new SimpleAnimator(findViewById(R.id.toolbar_bottom)).show();
        }
    }

    protected void hideToolbars() {
        if (toolbars) {
            toolbars = false;
            findViewById(R.id.imageView_menu).setVisibility(View.VISIBLE);
            new SimpleAnimator(findViewById(R.id.toolbar_top)).hide();
            new SimpleAnimator(findViewById(R.id.toolbar_bottom)).hide();
        }
    }
}
