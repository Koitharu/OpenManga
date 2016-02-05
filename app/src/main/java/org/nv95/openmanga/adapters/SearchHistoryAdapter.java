package org.nv95.openmanga.adapters;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.Nullable;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.nv95.openmanga.R;
import org.nv95.openmanga.utils.StorageHelper;

/**
 * Created by nv95 on 02.01.16.
 */
public class SearchHistoryAdapter extends CursorAdapter {
    private static final String TABLE_NAME = "search_history";
    private final StorageHelper mStorageHelper;
    private final SQLiteDatabase mDatabase;

    public SearchHistoryAdapter(Context context) {
        super(context, null, true);
        mStorageHelper = new StorageHelper(context);
        mDatabase = mStorageHelper.getReadableDatabase();
        updateContent(null);
    }

    public static void clearHistory(Context context) {
        StorageHelper storageHelper = new StorageHelper(context);
        SQLiteDatabase database = storageHelper.getWritableDatabase();
        database.beginTransaction();
        database.delete(TABLE_NAME, null, null);
        database.setTransactionSuccessful();
        database.endTransaction();
        storageHelper.close();
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context)
                .inflate(R.layout.item_search, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ((TextView) view).setText(cursor.getString(1));
    }

    public void updateContent(@Nullable String query) {
        Cursor newCursor = mDatabase.query(TABLE_NAME, null,
                query == null ? null : "query LIKE '" + query + "%'",
                null, null, null, null);
        Cursor oldCursor = swapCursor(newCursor);
        if (oldCursor != null) {
            oldCursor.close();
        }
        notifyDataSetChanged();
    }

    public String getString(int position) {
        Cursor c = getCursor();
        c.moveToPosition(position);
        return c.getString(1);
    }

    public void addToHistory(String what) {
        SQLiteDatabase database = mStorageHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("_id", what.hashCode());
        cv.put("query", what);
        int updCount = database.update(TABLE_NAME, cv, "_id=" + what.hashCode(), null);
        if (updCount == 0) {
            database.insert(TABLE_NAME, null, cv);
        }
        //database.close();
    }

    @Override
    protected void finalize() throws Throwable {
        if (mCursor != null) {
            mCursor.close();
        }
        if (mDatabase != null) {
            mDatabase.close();
        }
        if (mStorageHelper != null) {
            mStorageHelper.close();
        }
        super.finalize();
    }
}