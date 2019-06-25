package org.nv95.openmanga.feature.main.dialog;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import com.google.android.material.snackbar.Snackbar;
import androidx.core.content.ContextCompat;
import androidx.core.util.Pair;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import org.nv95.openmanga.R;
import org.nv95.openmanga.feature.read.ReadActivity2;
import org.nv95.openmanga.feature.main.adapter.BookmarksAdapter;
import org.nv95.openmanga.items.Bookmark;
import org.nv95.openmanga.feature.manga.domain.MangaInfo;
import org.nv95.openmanga.items.MangaSummary;
import org.nv95.openmanga.providers.BookmarksProvider;
import org.nv95.openmanga.providers.HistoryProvider;
import org.nv95.openmanga.providers.LocalMangaProvider;
import org.nv95.openmanga.providers.MangaProvider;
import org.nv95.openmanga.providers.staff.MangaProviderManager;

import java.util.ArrayList;

/**
 * Created by nv95 on 20.11.16.
 */

public class BookmarksDialog implements BookmarksAdapter.OnBookmarkClickListener, View.OnClickListener {

    private final Activity mActivity;
    private final AlertDialog mDialog;
    private final View mContentView;
    private final TextView mHolder;
    private final RecyclerView mRecyclerView;
    private final Toolbar mToolbar;

    private ArrayList<Bookmark> mBookmarks;

    public BookmarksDialog(Activity context) {
        mActivity = context;
        mContentView = LayoutInflater.from(context)
                .inflate(R.layout.dialog_bookmarks, null, false);
        mRecyclerView = mContentView.findViewById(R.id.recyclerView);
        mToolbar = mContentView.findViewById(R.id.toolbar);
        mHolder = mContentView.findViewById(R.id.textView_holder);
        mToolbar.setNavigationIcon(R.drawable.ic_cancel_light);
        mToolbar.setNavigationOnClickListener(this);
        mToolbar.setTitle(R.string.bookmarks);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(context));

        mDialog = new AlertDialog.Builder(context)
                .setView(mContentView)
                .create();
        mDialog.setOwnerActivity(mActivity);

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {

            private Drawable background;
            private Drawable xMark;
            boolean initialized = false;

            private void init() {
                background = new ColorDrawable(ContextCompat.getColor(mActivity, R.color.red_overlay));
                xMark = ContextCompat.getDrawable(mActivity, R.drawable.ic_delete_light);
                initialized = true;
            }

            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                int pos = viewHolder.getAdapterPosition();
                if (BookmarksProvider.getInstance(mActivity).remove(mBookmarks.get(pos).hashCode())) {
                    mBookmarks.remove(pos);
                    mRecyclerView.getAdapter().notifyItemRemoved(pos);
                    showHideHolder();
                    Snackbar.make(mContentView, R.string.bookmark_removed, Snackbar.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder,
                                    float dX, float dY, int actionState, boolean isCurrentlyActive) {

                if (viewHolder.getAdapterPosition() == -1) {
                    return;
                }

                View itemView = viewHolder.itemView;

                if (!initialized) {
                    init();
                }

                background.setBounds(itemView.getRight() + (int) dX, itemView.getTop(), itemView.getRight(), itemView.getBottom());
                background.draw(c);

                int itemHeight = itemView.getBottom() - itemView.getTop();
                int intrinsicWidth = xMark.getIntrinsicWidth();
                int intrinsicHeight = xMark.getIntrinsicWidth();

                int xMarkPos = (int) ((dX + intrinsicHeight) / 2);
                xMark.setAlpha(Math.min(255, (int)(-dX)));

                int xMarkLeft = itemView.getRight() + xMarkPos - intrinsicWidth;
                int xMarkRight = itemView.getRight() + xMarkPos;
                int xMarkTop = itemView.getTop() + (itemHeight - intrinsicHeight)/2;
                int xMarkBottom = xMarkTop + intrinsicHeight;
                xMark.setBounds(xMarkLeft, xMarkTop, xMarkRight, xMarkBottom);

                xMark.draw(c);

                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            }
        });
        itemTouchHelper.attachToRecyclerView(mRecyclerView);
    }

    public void show() {
        mBookmarks = BookmarksProvider.getInstance(mActivity).getAll();
        mRecyclerView.setAdapter(new BookmarksAdapter(mBookmarks, this));
        showHideHolder();
        mDialog.show();
    }

    public void show(MangaInfo manga) {
        mToolbar.setSubtitle(manga.name);
        mBookmarks = BookmarksProvider.getInstance(mActivity).getAll(manga.id);
        mRecyclerView.setAdapter(new BookmarksAdapter(mBookmarks, this));
        showHideHolder();
        mDialog.show();
    }

    private void showHideHolder() {
        if (mBookmarks.isEmpty()) {
            mHolder.setVisibility(View.VISIBLE);
            mRecyclerView.setVisibility(View.GONE);
        } else {
            mHolder.setVisibility(View.GONE);
            mRecyclerView.setVisibility(View.VISIBLE);
        }
    }
    
    @Override
    public void onBookmarkSelected(Bookmark bookmark) {
        mDialog.dismiss();
        new BookmarkOpenTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, bookmark);
    }
    
    @Override
    public void onClick(View v) {
        mDialog.dismiss();
    }
    
    
    private class BookmarkOpenTask extends AsyncTask<Bookmark,Void,Pair<Integer,Intent>> implements DialogInterface.OnCancelListener {

        private final ProgressDialog mProgressDialog;

        BookmarkOpenTask() {
            mProgressDialog = new ProgressDialog(mActivity);
            mProgressDialog.setIndeterminate(true);
            mProgressDialog.setCancelable(true);
            mProgressDialog.setOnCancelListener(this);
            mProgressDialog.setMessage(mActivity.getString(R.string.loading));
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mProgressDialog.show();
        }

        @Override
        protected Pair<Integer, Intent> doInBackground(Bookmark... params) {
            try {
                Intent intent;
                HistoryProvider historyProvider = HistoryProvider.getInstance(mActivity);
                MangaInfo info = historyProvider.get(params[0].mangaId);
                if (info == null) {
                    return new Pair<>(2, null);
                }
                MangaProvider provider;
                if (info.provider.equals(LocalMangaProvider.class)) {
                    provider = LocalMangaProvider.getInstance(mActivity);
                } else {
                    if (!MangaProviderManager.checkConnection(mActivity)) {
                        return new Pair<>(1, null);
                    }
                    provider = MangaProviderManager.instanceProvider(mActivity, info.provider);
                }
                MangaSummary summary = provider.getDetailedInfo(info);
                intent = new Intent(mActivity, ReadActivity2.class);
                intent.putExtras(summary.toBundle());
                intent.putExtra("chapter", summary.getChapters().indexByNumber(params[0].chapter));
                intent.putExtra("page", params[0].page);
                return new Pair<>(0, intent);
            } catch (Exception e) {
                return new Pair<>(3, null);
            }
        }

        @Override
        protected void onPostExecute(Pair<Integer,Intent> result) {
            super.onPostExecute(result);
            mProgressDialog.dismiss();
            int msg;
            switch (result.first) {
                case 0:
                    mActivity.startActivity(result.second);
                    return;
                case 1:
                    msg = R.string.no_network_connection;
                    break;
                case 2:
                    msg = R.string.history_empty;
                    break;
                default:
                    msg = R.string.error;
                    break;
            }
            new AlertDialog.Builder(mActivity)
                    .setCancelable(true)
                    .setPositiveButton(android.R.string.ok, null)
                    .setMessage(mActivity.getString(msg))
                    .create().show();
        }

        @Override
        public void onCancel(DialogInterface dialog) {
            this.cancel(false);
        }
    }
}
