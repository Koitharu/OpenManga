package org.nv95.openmanga;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.nv95.openmanga.components.ReaderViewPager;
import org.nv95.openmanga.components.SimpleAnimator;
import org.nv95.openmanga.providers.HistoryProvider;
import org.nv95.openmanga.providers.MangaChapter;
import org.nv95.openmanga.providers.MangaPage;
import org.nv95.openmanga.providers.MangaProvider;
import org.nv95.openmanga.providers.MangaSummary;

import java.util.ArrayList;

/**
 * Created by nv95 on 30.09.15.
 *
 */
public class ReadActivity extends Activity implements View.OnClickListener, ViewPager.OnPageChangeListener {
    private ReaderViewPager pager;
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
        pager = (ReaderViewPager) findViewById(R.id.pager);
        chapterTitleTextView = (TextView) findViewById(R.id.textView_title);
        chapterProgressBar = (ProgressBar) findViewById(R.id.progressBar_reading);

        findViewById(R.id.imageView_menu).setOnClickListener(this);
        findViewById(R.id.block_chapters).setOnClickListener(this);
        findViewById(R.id.toolbutton_fav).setOnClickListener(this);
        findViewById(R.id.toolbutton_img).setOnClickListener(this);
        findViewById(R.id.toolbutton_next).setOnClickListener(this);
        findViewById(R.id.toolbutton_prev).setOnClickListener(this);
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

        mangaSummary = new MangaSummary(getIntent().getExtras());
        chapterId = getIntent().getIntExtra("chapter", 0);
        pageId = getIntent().getIntExtra("page", 0);
        chapter = mangaSummary.getChapters().get(chapterId);
        pager.setOffscreenPageLimit(3);
        chapterTitleTextView.setText(chapter.getName());
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
            case R.id.toolbutton_prev:
                if (chapterId == 0)
                    break;
                chapterId--;
                pageId = 0;
                chapter = mangaSummary.getChapters().get(chapterId);
                chapterTitleTextView.setText(chapter.getName());
                hideToolbars();
                new LoadPagesTask().execute();
                break;
            case R.id.toolbutton_next:
                if (chapterId == mangaSummary.getChapters().size() - 1)
                    break;
                chapterId++;
                pageId = 0;
                chapter = mangaSummary.getChapters().get(chapterId);
                chapterTitleTextView.setText(chapter.getName());
                hideToolbars();
                new LoadPagesTask().execute();
                break;
            case R.id.toolbutton_share:
                hideToolbars();
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("text/plain");
                intent.putExtra(Intent.EXTRA_TEXT, mangaSummary.getReadLink());
                intent.putExtra(android.content.Intent.EXTRA_SUBJECT, mangaSummary.getName());
                startActivity(Intent.createChooser(intent, getString(R.string.action_share)));
                break;
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
            pager.setCurrentItem(pageId);
        }

        @Override
        protected ArrayList<MangaPage> doInBackground(Void... params) {
            try {
                MangaProvider provider = (MangaProvider) mangaSummary.getProvider().newInstance();
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
