package org.nv95.openmanga.adapters;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import org.nv95.openmanga.R;
import org.nv95.openmanga.utils.StorageHelper;

/**
 * Created by nv95 on 02.01.16.
 */
public class SearchHistoryAdapter extends BaseAdapter {
    private static final String TABLE_NAME = "search_history";
    private final Context mContext;
    private final StorageHelper mStorageHelper;
    private final SQLiteDatabase mDatabase;
    private final Cursor mCursor;

    public SearchHistoryAdapter(Context context) {
        mContext = context;
        mStorageHelper = new StorageHelper(context);
        mDatabase = mStorageHelper.getReadableDatabase();
        mCursor = mDatabase.query(TABLE_NAME, null, null, null, null, null, null);
    }

    @Override
    public int getCount() {
        return mCursor.getCount();
    }

    @Override
    public String getItem(int position) {
        mCursor.moveToPosition(position);
        return mCursor.getString(1);
    }

    @Override
    public long getItemId(int position) {
        mCursor.moveToPosition(position);
        return mCursor.getLong(0);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext)
                    .inflate(R.layout.item_search, parent, false);
        }
        ((TextView)convertView).setText(getItem(position));
        return convertView;
    }

    public void close() {
        if (mCursor != null) {
            mCursor.close();
        }
        if (mDatabase != null) {
            mDatabase.close();
        }
        if (mStorageHelper != null) {
            mStorageHelper.close();
        }
    }
}