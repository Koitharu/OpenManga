
package org.nv95.openmanga;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v4.view.ViewPager;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
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
public class ReadActivity extends Activity implements View.OnClickListener, ViewPager.OnPageChangeListener, ReaderOptionsDialog.OnOptionsChangedListener, NavigationDialog.NavigationListener {
    private AdvancedViewPager pager;
    private MangaSummary mangaSummary;
    private MangaChapter chapter;
    private int chapterId;
    private int pageId;
    private TextView chapterTitleTextView;
    private ProgressBar chapterProgressBar;
    private boolean toolbars = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reader);
        pager = (AdvancedViewPager) findViewById(R.id.pager);
        chapterTitleTextView = (TextView) findViewById(R.id.textView_title);
        chapterProgressBar = (ProgressBar) findViewById(R.id.progressBar_reading);

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
        pager.addOnPageChangeListener(this);
        onOptionsChanged();
        mangaSummary = new MangaSummary(getIntent().getExtras());
        chapterId = getIntent().getIntExtra("chapter", 0);
        pageId = getIntent().getIntExtra("page", 0);
        chapter = mangaSummary.getChapters().get(chapterId);
        pager.setOffscreenPageLimit(3);
        chapterTitleTextView.setText(chapter.getName());
        if (FavouritesProvider.Has(this, mangaSummary)) {
            ((ImageView) findViewById(R.id.toolbutton_fav)).setImageResource(R.drawable.ic_tool_favorite);
            findViewById(R.id.toolbutton_fav).setContentDescription(getString(R.string.action_unfavourite));
        }
        new LoadPagesTask().execute();
    }

    @Override
    protected void onPause() {
        HistoryProvider.addToHistory(this, mangaSummary, chapterId, pager.getCurrentItem());
        super.onPause();
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
                SaveService.Save(this, mangaSummary);
                hideToolbars();
                break;
            case R.id.toolbutton_fav:
                ImageView favbtn = (ImageView) findViewById(R.id.toolbutton_fav);
                FavouritesProvider favouritesProvider = new FavouritesProvider(this);
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
                new AlertDialog.Builder(this).setTitle(R.string.action_image_opts)
                        .setItems(R.array.image_opts, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                File file = new File(page.getPath());
                                if (!file.exists()) {
                                    file = new File(ReadActivity.this.getExternalCacheDir(), String.valueOf(page.getPath().hashCode()));
                                }
                                if (!file.exists()) {
                                    new ErrorReporter(getApplicationContext()).report("# ReadActivity.SaveImg.NotFound\n page.path: " + page.getPath());
                                    return;
                                }
                                switch (which) {
                                    case 0: //save
                                        SaveImage(file, page);
                                        dialog.dismiss();
                                        break;
                                }
                            }
                        })
                        .create().show();
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
                    provider = new LocalMangaProvider(ReadActivity.this);
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
