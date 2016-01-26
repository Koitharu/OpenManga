package org.nv95.openmanga.adapters;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
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
  //todo use loader
  private static final String TABLE_NAME = "search_history";
  private static StorageHelper storageHelper;
  private static SQLiteDatabase database;
  private final int oddColor;
  private final int evenColor;
  private LayoutInflater inflater;

  private SearchHistoryAdapter(Context context, Cursor cursor) {
    super(context, cursor, true);
    oddColor = ContextCompat.getColor(context, R.color.light_background);
    evenColor = ContextCompat.getColor(context, R.color.light_background_darker);
    inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
  }

  public static SearchHistoryAdapter newInstance(Context context) {
    storageHelper = new StorageHelper(context);
    database = storageHelper.getReadableDatabase();
    @SuppressLint("Recycle")
    Cursor c = database.query(TABLE_NAME, null, null, null, null, null, null);
    return new SearchHistoryAdapter(context, c);
  }

  public static void Recycle(@Nullable SearchHistoryAdapter instance) {
    if (instance != null) {
      Cursor c = instance.getCursor();
      if (c != null) {
        c.close();
      }
    }
    if (database != null) {
      database.close();
    }
    if (storageHelper != null) {
      storageHelper.close();
    }
  }

  public static void clearHistory(Context context) {
    SQLiteDatabase database = new StorageHelper(context).getWritableDatabase();
    database.beginTransaction();
    database.delete(TABLE_NAME, null, null);
    database.setTransactionSuccessful();
    database.endTransaction();
    database.close();
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

  public void remove(long id) {
    SQLiteDatabase database = new StorageHelper(inflater.getContext()).getWritableDatabase();
    database.delete(TABLE_NAME, "_id=?", new String[]{String.valueOf(id)});
    database.close();
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

  public void update() {
    Cursor newCursor = database.query(TABLE_NAME, null, null, null, null, null, null);
    Cursor oldCursor = getCursor();
    swapCursor(newCursor);
    if (oldCursor != null) {
      oldCursor.close();
    }
  }
}