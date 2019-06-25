package org.nv95.openmanga.feature.main.dialog;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.nv95.openmanga.R;
import org.nv95.openmanga.feature.read.ReadActivity2;
import org.nv95.openmanga.feature.main.adapter.ChaptersAdapter;
import org.nv95.openmanga.feature.main.adapter.OnChapterClickListener;
import org.nv95.openmanga.items.Bookmark;
import org.nv95.openmanga.items.MangaChapter;
import org.nv95.openmanga.feature.manga.domain.MangaInfo;
import org.nv95.openmanga.items.MangaSummary;
import org.nv95.openmanga.providers.HistoryProvider;
import org.nv95.openmanga.providers.MangaProvider;
import org.nv95.openmanga.providers.staff.MangaProviderManager;
import org.nv95.openmanga.utils.AnimUtils;
import org.nv95.openmanga.utils.WeakAsyncTask;

import java.lang.ref.WeakReference;
import java.util.List;

/**
 * Created by admin on 16.08.17.
 */

public class PreviewDialog implements DialogInterface.OnDismissListener, OnChapterClickListener {

    private final AlertDialog mDialog;
    private final View mContentView;
    private final TextView mHolder;
    private final ProgressBar mProgressBar;
    private final RecyclerView mRecyclerView;
    private final ChaptersAdapter mChaptersAdapter;
    private MangaSummary mManga;
    private WeakReference<LoadTask> mTaskRef;

    public PreviewDialog(Context context) {
        mContentView = LayoutInflater.from(context)
                .inflate(R.layout.dialog_preview, null, false);
        mRecyclerView = mContentView.findViewById(R.id.recyclerView);
        mHolder = mContentView.findViewById(R.id.textView_holder);
        mProgressBar = mContentView.findViewById(R.id.progressBar);
        mChaptersAdapter = new ChaptersAdapter(context);
        mChaptersAdapter.setOnItemClickListener(this);
        mRecyclerView.setAdapter(mChaptersAdapter);

        mDialog = new AlertDialog.Builder(context)
                .setView(mContentView)
                .setCancelable(true)
                .setOnDismissListener(this)
                .create();
    }

    public void show(MangaInfo mangaInfo) {
        mManga = new MangaSummary(mangaInfo);
        LoadTask task = new LoadTask(this);
        mTaskRef = new WeakReference<>(task);
        task.start();
        mDialog.show();
    }

    @Override
    public void onDismiss(DialogInterface dialogInterface) {
        WeakAsyncTask.cancel(mTaskRef, true);
    }

    @Override
    public void onChapterClick(int pos, MangaChapter chapter, RecyclerView.ViewHolder viewHolder) {
        if (pos == -1) {
            Intent intent = new Intent(mDialog.getContext(), ReadActivity2.class);
            intent.putExtras(mManga.toBundle());
            HistoryProvider.HistorySummary hs = HistoryProvider.getInstance(mDialog.getContext())
                    .get(mManga);
            if (hs != null) {
                int index = mManga.chapters.indexByNumber(hs.getChapter());
                if (index != -1) {
                    intent.putExtra("chapter", index);
                    intent.putExtra("page", hs.getPage());
                }
            }
            mDialog.getContext().startActivity(intent);
        } else {
            HistoryProvider.getInstance(mDialog.getContext()).add(mManga, chapter.number, 0);
            mDialog.getContext().startActivity(new Intent(mDialog.getContext(), ReadActivity2.class)
                    .putExtra("chapter", pos).putExtras(mManga.toBundle()));
        }
        mDialog.dismiss();
    }

    @Override
    public boolean onChapterLongClick(int pos, MangaChapter chapter, RecyclerView.ViewHolder viewHolder) {
        return false;
    }

    private static class LoadTask extends WeakAsyncTask<PreviewDialog, Void, List<Bookmark>, MangaSummary> {

        LoadTask(PreviewDialog object) {
            super(object);
        }

        @Override
        protected void onPreExecute(@NonNull PreviewDialog dialog) {
        }

        @SuppressWarnings("ConstantConditions")
        @Override
        protected MangaSummary doInBackground(Void... params) {
            try {
                //noinspection unchecked
                MangaProvider provider = MangaProviderManager.instanceProvider(getObject().mDialog.getContext(), getObject().mManga.provider);
                return provider.getDetailedInfo(getObject().mManga);
            } catch (Exception e) {
                return null;
            }
        }

        @Override
        protected void onPostExecute(@NonNull final PreviewDialog dialog, MangaSummary mangaSummary) {
            if (mangaSummary != null) {
                dialog.mManga = mangaSummary;
                if (mangaSummary.chapters.isEmpty()) {
                    dialog.mHolder.setText(R.string.no_chapters_found);
                    AnimUtils.crossfade(dialog.mProgressBar, dialog.mHolder);
                } else {
                    dialog.mChaptersAdapter.setData(dialog.mManga.chapters);
                    dialog.mChaptersAdapter.setExtra(HistoryProvider.getInstance(dialog.mDialog.getContext()).get(dialog.mManga));
                    dialog.mChaptersAdapter.notifyDataSetChanged();
                    dialog.mRecyclerView.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            dialog.mRecyclerView.scrollToPosition(0);
                        }
                    }, 500);
                    AnimUtils.crossfade(dialog.mProgressBar, null);
                }
            } else {
                dialog.mHolder.setText(R.string.loading_error);
                AnimUtils.crossfade(dialog.mProgressBar, dialog.mHolder);
            }
        }
    }
}
