package org.nv95.openmanga.feature.search.adapter;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.nv95.openmanga.R;
import org.nv95.openmanga.helpers.StorageHelper;
import org.nv95.openmanga.utils.LayoutUtils;
import org.nv95.openmanga.utils.WeakAsyncTask;

import java.lang.ref.WeakReference;

/**
 * Created by nv95 on 02.01.16.
 */
public class SearchHistoryAdapter extends RecyclerView.Adapter<SearchHistoryAdapter.ItemHolder> {

    private static final String TABLE_NAME = "search_history";
    private final Drawable[] mIcons;

    private final StorageHelper mStorageHelper;
    @Nullable
    private Cursor mCursor;
    @Nullable
    private static WeakReference<StorageHelper> sStorageHelperRef;
    @NonNull
    private final OnHistoryEventListener mClickListener;
    @Nullable
    private WeakReference<QueryTask> mQueryTaskRef = null;

    public SearchHistoryAdapter(Context context, @NonNull OnHistoryEventListener clickListener) {
        mStorageHelper = new StorageHelper(context);
        sStorageHelperRef = new WeakReference<>(mStorageHelper);
        setHasStableIds(true);
        mClickListener = clickListener;
        mCursor = null;
        mIcons = LayoutUtils.getThemedIcons(context, R.drawable.ic_history_dark, R.drawable.ic_launch_black);
    }

    @Override
    protected void finalize() throws Throwable {
        mStorageHelper.close();
        if (sStorageHelperRef != null) {
            sStorageHelperRef.clear();
            sStorageHelperRef = null;
        }
        super.finalize();
    }

    @Override
    public ItemHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ItemHolder holder = new ItemHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_search, parent, false), mClickListener);
        holder.textView.setCompoundDrawablesWithIntrinsicBounds(mIcons[0], null, null, null);
        holder.imageButton.setImageDrawable(mIcons[1]);
        return holder;
    }

    @Override
    public void onBindViewHolder(ItemHolder holder, int position) {
        if (mCursor != null) {
            mCursor.moveToPosition(position);
            holder.fill(mCursor.getString(1));
        }
    }

    @Override
    public long getItemId(int position) {
        if (mCursor != null) {
            mCursor.moveToPosition(position);
            return mCursor.getInt(0);
        } else {
            return 0;
        }
    }

    private void swapCursor(@Nullable Cursor newCursor) {
        Cursor oldCursor = mCursor;
        mCursor = newCursor;
        if (oldCursor != null) {
            oldCursor.close();
        }
        notifyDataSetChanged();
    }

    public void requery(@Nullable String prefix) {
        cancelTask();
        Cursor cursor = mStorageHelper.getReadableDatabase().query(TABLE_NAME, null,
                TextUtils.isEmpty(prefix) ? null : "query LIKE ?",
                TextUtils.isEmpty(prefix) ? null : new String[] {prefix + "%"}, null, null, null);
        swapCursor(cursor);
    }

    public void requeryAsync(@Nullable String prefix) {
        cancelTask();
        QueryTask task = new QueryTask(this);
        mQueryTaskRef = new WeakReference<>(task);
        task.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, prefix);
    }

    private void cancelTask() {
        if (mQueryTaskRef != null) {
            QueryTask task = mQueryTaskRef.get();
            if (task != null && task.canCancel()) {
                task.cancel(false);
            }
        }
        mQueryTaskRef = null;
    }

    @Override
    public int getItemCount() {
        return mCursor == null ? 0 : mCursor.getCount();
    }

    static class ItemHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private final OnHistoryEventListener mClickListener;
        private String mText;
        final TextView textView;
        final ImageView imageButton;

        ItemHolder(View itemView, OnHistoryEventListener listener) {
            super(itemView);
            mClickListener = listener;
            textView = itemView.findViewById(android.R.id.text1);
            imageButton = itemView.findViewById(R.id.imageButton);
            imageButton.setOnClickListener(this);
            textView.setOnClickListener(this);
        }

        public void fill(String text) {
            mText = text;
            textView.setText(text);
        }

        @Override
        public void onClick(View view) {
            mClickListener.onHistoryItemClick(mText, view.getId() != imageButton.getId());
        }
    }

    public interface OnHistoryEventListener {
        void onHistoryItemClick(String text, boolean apply);
    }

    public static void clearHistory(Context context) {
        StorageHelper storageHelper = null;
        boolean reused = true;
        if (sStorageHelperRef != null) {
            storageHelper = sStorageHelperRef.get();
        }
        if (storageHelper == null) {
            storageHelper = new StorageHelper(context);
            reused = false;
        }
        storageHelper.getWritableDatabase().delete(TABLE_NAME, null, null);
        if (!reused) {
            storageHelper.close();
        }
    }

    public static void addToHistory(Context context, String what) {
        StorageHelper storageHelper = null;
        boolean reused = true;
        if (sStorageHelperRef != null) {
            storageHelper = sStorageHelperRef.get();
        }
        if (storageHelper == null) {
            storageHelper = new StorageHelper(context);
            reused = false;
        }
        SQLiteDatabase database = storageHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("_id", what.hashCode());
        cv.put("query", what);
        int updCount = database.update(TABLE_NAME, cv, "_id=" + what.hashCode(), null);
        if (updCount == 0) {
            database.insert(TABLE_NAME, null, cv);
        }
        if (!reused) {
            storageHelper.close();
        }
    }

    public static void removeFromHistory(Context context, long what) {
        StorageHelper storageHelper = null;
        boolean reused = true;
        if (sStorageHelperRef != null) {
            storageHelper = sStorageHelperRef.get();
        }
        if (storageHelper == null) {
            storageHelper = new StorageHelper(context);
            reused = false;
        }
        SQLiteDatabase database = storageHelper.getWritableDatabase();
        database.delete(TABLE_NAME, "_id=" + what, null);
        if (!reused) {
            storageHelper.close();
        }
    }


    public static int getHistorySize(Context context) {
        StorageHelper storageHelper = null;
        boolean reused = true;
        if (sStorageHelperRef != null) {
            storageHelper = sStorageHelperRef.get();
        }
        if (storageHelper == null) {
            storageHelper = new StorageHelper(context);
            reused = false;
        }
        int res = StorageHelper.getRowCount(
                storageHelper.getReadableDatabase(),
                TABLE_NAME,
                null
        );
        if (!reused) {
            storageHelper.close();
        }
        return res;
    }


    private static class QueryTask extends WeakAsyncTask<SearchHistoryAdapter, String,Void,Cursor> {

        QueryTask(SearchHistoryAdapter searchHistoryAdapter) {
            super(searchHistoryAdapter);
        }

        @SuppressWarnings("ConstantConditions")
        @Override
        protected Cursor doInBackground(String... strings) {
            try {
                String prefix = strings.length > 0 ? strings[0] : null;
                return getObject().mStorageHelper.getReadableDatabase().query(TABLE_NAME, null,
                        TextUtils.isEmpty(prefix) ? null : "query LIKE ?",
                        TextUtils.isEmpty(prefix) ? null : new String[] {prefix + "%"}, null, null, null);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(@NonNull SearchHistoryAdapter searchHistoryAdapter, Cursor cursor) {
            if (cursor != null) {
                searchHistoryAdapter.swapCursor(cursor);
            }
        }
    }
}