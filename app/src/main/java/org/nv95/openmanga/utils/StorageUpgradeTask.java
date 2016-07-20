package org.nv95.openmanga.utils;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.preference.PreferenceManager;

import org.nv95.openmanga.R;
import org.nv95.openmanga.helpers.StorageHelper;

import java.io.File;

/**
 * Created by nv95 on 04.06.16.
 */

public class StorageUpgradeTask extends AsyncTask<Void,Integer,Boolean> {

    private static final int STORAGE_VERSION = 1;

    private final ProgressDialog mProgressDialog;
    private final Context mContext;

    public StorageUpgradeTask(Context context) {
        mContext = context;
        mProgressDialog = new ProgressDialog(mContext);
        mProgressDialog.setCancelable(false);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        mProgressDialog.setMessage(mContext.getString(R.string.storage_upgrading));
        mProgressDialog.setIndeterminate(true);
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        mProgressDialog.show();
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        boolean errors = false;
        final StorageHelper storageHelper = new StorageHelper(mContext);
        final MangaStore newStore = new MangaStore(mContext);
        final SQLiteDatabase oldDatabase = storageHelper.getReadableDatabase();
        SQLiteDatabase newDatabase = null;
        Cursor cursor = null;
        Cursor cursor1 = null;
        Cursor cursor2 = null;
        ContentValues cv;
        ContentValues cv1;
        ContentValues cv2;
        int mangaId;
        int chapterId;
        File cover;
        //enum old saved manga
        try {
            if (!StorageHelper.isTableExists(oldDatabase, "local_storage")) {
                oldDatabase.close();
                storageHelper.close();
                return true;
            }
            cursor = oldDatabase.query("local_storage", null, null, null, null, null, null);
            if (cursor.moveToFirst()) {
                boolean hasTimestamp = cursor.getColumnIndex("timestamp") != -1;
                do {
                    //copy manga to new database
                    cv = new ContentValues();
                    cv.put("id", mangaId = cursor.getInt(0));
                    cv.put("name", cursor.getString(1));
                    cv.put("subtitle", cursor.getString(2));
                    cv.put("summary", cursor.getString(3));
                    cv.put("description", cursor.getString(7));
                    //move cover
                    cover = new File(cursor.getString(4));
                    //noinspection ResultOfMethodCallIgnored
                    cover.renameTo(new File(cover.getParentFile(), "cover"));
                    cv.put("dir", cover.getParent());
                    cv.put("timestamp", hasTimestamp ? cursor.getInt(8) : 0);
                    newDatabase = newStore.getDatabase(true);
                    newDatabase.insert(MangaStore.TABLE_MANGAS, null, cv);
                    //copy all chapters
                    cursor1 = oldDatabase.query("local_chapters", null, "mangaId=" + cursor.getString(6), null, null, null, null);
                    if (cursor1.moveToFirst()) {
                        do {
                            //copy chapter to new database
                            cv1 = new ContentValues();
                            cv1.put("id", chapterId = cursor1.getInt(1));
                            cv1.put("mangaid", mangaId);
                            cv1.put("name", cursor1.getString(3));
                            cv1.put("number", cursor1.getInt(0));
                            if (!newDatabase.isOpen()) {
                                newDatabase = newStore.getDatabase(true);
                            }
                            newDatabase.insert(MangaStore.TABLE_CHAPTERS, null, cv1);
                            //enum all pages
                            cursor2 = oldDatabase.query("local_pages", null, "chapterId=" + chapterId, null, null, null, null);
                            if (cursor2.moveToFirst()) {
                                do {
                                    //copy page to new database
                                    cv2 = new ContentValues();
                                    cv2.put("id", cursor2.getInt(1));
                                    cv2.put("chapterid", chapterId);
                                    cv2.put("mangaid", mangaId);
                                    cv2.put("file", chapterId + "/" + new File(cursor2.getString(3)).getName());
                                    cv2.put("number", cursor2.getInt(0));
                                    if (!newDatabase.isOpen()) {
                                        newDatabase = newStore.getDatabase(true);
                                    }
                                    newDatabase.insert(MangaStore.TABLE_PAGES, null, cv2);
                                } while (cursor2.moveToNext());
                            }
                            cursor2.close();
                            cursor2 = null;
                        } while (cursor1.moveToNext());
                    }
                    cursor1.close();
                    cursor1 = null;

                } while (cursor.moveToNext());
            }
            cursor.close();
            cursor = null;
        } catch (Exception e) {
            errors = true;
            FileLogger.getInstance().report(e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            if (cursor1 != null) {
                cursor1.close();
            }
            if (cursor2 != null) {
                cursor2.close();
            }
        }
        if (newDatabase != null && newDatabase.isOpen()) {
            newDatabase.close();
        }
        oldDatabase.close();
        storageHelper.close();
        return !errors;
    }

    @Override
    protected void onPostExecute(Boolean aBoolean) {
        super.onPostExecute(aBoolean);
        mProgressDialog.dismiss();
        ChangesObserver.getInstance().emitOnLocalChanged(-1, null);
    }

    public static void doUpgrade(Context context) {
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        if (prefs.getInt("storage_version", -1) < STORAGE_VERSION) {
            new StorageUpgradeTask(context)
                    .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            prefs.edit()
                    .putInt("storage_version", STORAGE_VERSION)
                    .apply();
        }
    }
}
