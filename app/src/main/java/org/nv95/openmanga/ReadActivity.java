package org.nv95.openmanga;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.view.ViewPager;

import org.nv95.openmanga.providers.MangaChapter;
import org.nv95.openmanga.providers.MangaPage;
import org.nv95.openmanga.providers.MangaProvider;
import org.nv95.openmanga.providers.MangaSummary;

import java.util.ArrayList;

/**
 * Created by nv95 on 30.09.15.
 *
 */
public class ReadActivity extends Activity {
    private ViewPager pager;
    private MangaSummary mangaInfo;
    private MangaChapter chapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reader);
        mangaInfo = new MangaSummary(getIntent().getExtras());
        int chapterId = getIntent().getIntExtra("chapter", 0);
        chapter = mangaInfo.getChapters().get(chapterId);
        pager = (ViewPager) findViewById(R.id.pager);
        pager.setOffscreenPageLimit(3);
        new LoadPagesTask().execute();
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
            pager.setAdapter(new PagerReaderAdapter(ReadActivity.this, mangaPages));
            pager.setCurrentItem(0);
        }

        @Override
        protected ArrayList<MangaPage> doInBackground(Void... params) {
            try {
                MangaProvider provider = (MangaProvider) mangaInfo.getProvider().newInstance();
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
}
