package org.nv95.openmanga.activities;

import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.PowerManager;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.nv95.openmanga.Constants;
import org.nv95.openmanga.R;
import org.nv95.openmanga.helpers.DirRemoveHelper;
import org.nv95.openmanga.providers.LocalMangaProvider;
import org.nv95.openmanga.utils.FileLogger;
import org.nv95.openmanga.utils.MangaChangesObserver;
import org.nv95.openmanga.utils.MangaStore;
import org.nv95.openmanga.utils.ZipBuilder;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static org.nv95.openmanga.utils.MangaStore.TABLE_CHAPTERS;
import static org.nv95.openmanga.utils.MangaStore.TABLE_MANGAS;
import static org.nv95.openmanga.utils.MangaStore.TABLE_PAGES;

/**
 * Created by nv95 on 05.02.16.
 */
public class CBZActivity extends AppCompatActivity implements View.OnClickListener {
    private TextView mTextViewPrimary;
    private TextView mTextViewSecondary;
    private ProgressBar mProgressBar;
    private Button mButton;
    private ImportTask mTaskInstance;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cbz);
        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);
        mTextViewPrimary = (TextView) findViewById(R.id.textView_title);
        mTextViewSecondary = (TextView) findViewById(R.id.textView_subtitle);
        mButton = (Button) findViewById(R.id.button);
        mButton.setOnClickListener(this);
        String path = getIntent().getStringExtra(Intent.EXTRA_TEXT);
        mTaskInstance = new ImportTask();
        mTaskInstance.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, path);
    }

    @Override
    public void onClick(View v) {
        if (mTaskInstance != null && mTaskInstance.getStatus() == AsyncTask.Status.RUNNING) {
            mTextViewPrimary.setText(R.string.cancelling);
            mTaskInstance.cancel(false);
        } else {
            finish();
        }
    }

    private class ImportTask extends AsyncTask<String,Object,Integer> {
        private final String mProgressInfo;
        private final PowerManager.WakeLock mWakeLock;

        public ImportTask() {
            mProgressInfo = getString(R.string.import_progress);
            PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
            mWakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "import");
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mTextViewPrimary.setText(R.string.wait);
            mTextViewSecondary.setText(R.string.loading);
            mWakeLock.acquire();
        }

        @Override
        protected Integer doInBackground(String... params) {
            SQLiteDatabase database = null;
            ZipInputStream zipInputStream = null;
            int pages = 0;
            try {
                //reading info
                final ZipEntry[] entries = ZipBuilder.enumerateEntries(params[0]);
                String name = entries[0].getName();
                name = name.substring(0, name.length() - 1);
                int total = 0;
                for (ZipEntry o:entries) {
                    if (!o.isDirectory()) {
                        total++;
                    }
                }
                int mangaId;
                int chapterId;
                int pageId;
                File preview = null;
                zipInputStream = new ZipInputStream(new FileInputStream(params[0]));
                final File dest = new File(MangaStore.getMangasDir(CBZActivity.this),
                        String.valueOf(mangaId = params[0].hashCode()));
                if (dest.exists()) {
                    new DirRemoveHelper(dest).run();
                }
                dest.mkdirs();
                final byte[] buffer = new byte[1024];
                publishProgress(false);
                publishProgress(name, null);
                publishProgress(0, entries.length);
                //importing
                database = new MangaStore(CBZActivity.this).getDatabase(true);
                ContentValues cv;
                //all pages
                chapterId = 0;
                File outFile;
                ZipEntry entry;
                FileOutputStream outputStream;
                while ((entry = zipInputStream.getNextEntry()) != null && !isCancelled()) {
                    if (!entry.isDirectory()) {
                        pageId = entry.getName().hashCode();
                        outFile = new File(dest, chapterId + "_" + pageId);
                        if (outFile.exists() || outFile.createNewFile()) {
                            outputStream = new FileOutputStream(outFile);
                            int len;
                            while ((len = zipInputStream.read(buffer)) > 0) {
                                outputStream.write(buffer, 0, len);
                            }
                            outputStream.close();
                            cv = new ContentValues();
                            cv.put("id", pageId);
                            cv.put("chapterid", chapterId);
                            cv.put("mangaid", mangaId);
                            cv.put("file", outFile.getName());
                            cv.put("number", pages);
                            database.insert(TABLE_PAGES, null, cv);
                            pages++;
                            publishProgress(pages, total);
                            publishProgress(null, String.format(mProgressInfo, pages, total));
                            if (preview == null) {
                                preview = new File(dest, "cover");
                                LocalMangaProvider.CopyFile(outFile, preview);
                            }
                        }
                    }
                }
                if (isCancelled()) {
                    //remove all
                    database.delete(TABLE_PAGES, "mangaid=?", new String[]{String.valueOf(mangaId)});
                    new DirRemoveHelper(dest).run();
                    database.close();
                    try {
                        zipInputStream.close();
                    } catch (IOException ignored) {
                    }
                    return null;
                }
                //save chapter
                cv = new ContentValues();
                cv.put("id", chapterId);
                cv.put("mangaid", mangaId);
                cv.put("name", "default");
                cv.put("number", 0);
                database.insert(TABLE_CHAPTERS, null, cv);
                //save manga
                cv = new ContentValues();
                cv.put("id", mangaId);
                cv.put("name", name);
                cv.put("subtitle", "");
                cv.put("summary", "");
                cv.put("description", getString(R.string.imported_from, new File(params[0]).getName()));
                cv.put("dir", MangaStore.getMangaDir(CBZActivity.this, database, mangaId).getPath());
                cv.put("timestamp", new Date().getTime());
                database.insert(TABLE_MANGAS, null, cv);
            } catch (Exception e) {
                FileLogger.getInstance().report(e);
                pages = -1;
            } finally {
                if (database != null) {
                    database.close();
                }
                if (zipInputStream != null) {
                    try {
                        zipInputStream.close();
                    } catch (IOException ignored) {
                    }
                }
            }
            return pages;
        }

        @Override
        protected void onProgressUpdate(Object... values) {
            super.onProgressUpdate(values);
            if (values.length == 2) {
                if (values[1] != null) {
                    if (values[1] instanceof String) {
                        mTextViewSecondary.setText((String)values[1]);
                    } else if (values[1] instanceof Integer) {
                        mProgressBar.setMax((Integer) values[1]);
                    }
                }
                if (values[0] != null) {
                    if (values[0] instanceof String) {
                        mTextViewPrimary.setText((String)values[0]);
                    } else if (values[0] instanceof Integer) {
                        mProgressBar.setProgress((Integer) values[0]);
                    }
                }
            } else if (values.length == 1) {
                if (values[0] != null && values[0] instanceof Boolean) {
                    mProgressBar.setIndeterminate((Boolean) values[0]);
                }
            }
        }

        @Override
        protected void onPostExecute(Integer integer) {
            super.onPostExecute(integer);
            MangaChangesObserver.queueChanges(Constants.CATEGORY_LOCAL);
            if (integer == -1) {
                onProgressUpdate(getString(R.string.error), null);
            } else {
                onProgressUpdate(getString(R.string.done), getString(R.string.import_complete));
            }
            mButton.setText(R.string.close);
            mWakeLock.release();
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            MangaChangesObserver.queueChanges(Constants.CATEGORY_LOCAL);
            onProgressUpdate(getString(R.string.cancelled), null);
            mButton.setText(R.string.close);
            mWakeLock.release();
        }
    }
}
