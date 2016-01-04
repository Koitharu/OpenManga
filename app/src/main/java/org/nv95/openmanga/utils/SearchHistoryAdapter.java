package org.nv95.openmanga.utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import org.nv95.openmanga.R;
import org.nv95.openmanga.providers.StorageHelper;

/**
 * Created by nv95 on 02.01.16.
 */
public class SearchHistoryAdapter extends CursorAdapter {
    //todo use loader
    private static final String TABLE_NAME = "search_history";
    private final int oddColor;
    private final int evenColor;

    private LayoutInflater inflater;

    public static SearchHistoryAdapter newInstance(Context context) {
        StorageHelper storageHelper = new StorageHelper(context);
        Cursor c = storageHelper.getReadableDatabase().query(TABLE_NAME, null, null, null, null, null, null);
        return new SearchHistoryAdapter(context, c);
    }

    private SearchHistoryAdapter(Context context, Cursor cursor) {
        super(context, cursor, true);
        oddColor = context.getResources().getColor(R.color.primary_dark);
        evenColor = context.getResources().getColor(R.color.primary_dark_dark);
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return inflater.inflate(R.layout.item_search, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        TextView content = (TextView) view;
        content.setBackgroundColor(cursor.getPosition() % 2 == 1 ? oddColor : evenColor);
        content.setText(cursor.getString(1));
    }

    public void addToHistory(String query) {
        SQLiteDatabase database = new StorageHelper(inflater.getContext()).getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("_id", query.hashCode());
        cv.put("query", query);
        int updCount = database.update(TABLE_NAME, cv, "_id=" + query.hashCode(), null);
        if (updCount == 0) {
            database.insert(TABLE_NAME, null, cv);
        }
        database.close();
    }

    public static void clearHistory(Context context) {
        SQLiteDatabase database = new StorageHelper(context).getWritableDatabase();
        database.beginTransaction();
        database.delete(TABLE_NAME, null, null);
        database.setTransactionSuccessful();
        database.endTransaction();
        database.close();
    }

    public void update() {
        getCursor().requery();
    }
}