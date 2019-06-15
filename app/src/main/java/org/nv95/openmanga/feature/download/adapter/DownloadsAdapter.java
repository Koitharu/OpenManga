/*
 * Copyright (C) 2016 Vasily Nikitin
 *
 * This program is free software: you can redistribute it and/or modify it under the terms
 * of the GNU General Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program.
 * If not, see http://www.gnu.org/licenses/.
 */

package org.nv95.openmanga.feature.download.adapter;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.drawable.Drawable;
import android.os.IBinder;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.nv95.openmanga.R;
import org.nv95.openmanga.feature.download.domain.model.DownloadInfo;
import org.nv95.openmanga.items.ThumbSize;
import org.nv95.openmanga.feature.download.service.SaveService;
import org.nv95.openmanga.utils.ImageUtils;
import org.nv95.openmanga.utils.LayoutUtils;
import org.nv95.openmanga.utils.PausableAsyncTask.ExStatus;

import java.util.ArrayList;

/**
 * Created by nv95 on 03.01.16.
 */
public class DownloadsAdapter extends RecyclerView.Adapter<DownloadsAdapter.DownloadHolder>
        implements ServiceConnection, SaveService.OnSaveProgressListener, View.OnClickListener {

    private final Intent mIntent;
    @Nullable
    private SaveService.SaveServiceBinder mBinder;
    private final RecyclerView mRecyclerView;
    private final ArrayList<Integer> mItemsIds;
    private final Drawable[] mIcons;

    public DownloadsAdapter(RecyclerView recyclerView) {
        mItemsIds = new ArrayList<>();
        mIntent = new Intent(recyclerView.getContext(), SaveService.class);
        mBinder = null;
        mRecyclerView = recyclerView;
        mIcons = LayoutUtils.getThemedIcons(recyclerView.getContext(), R.drawable.ic_resume_darker, R.drawable.ic_pause_darker, R.drawable.ic_cancel_darker);
        setHasStableIds(true);
    }

    public void enable() {
        mRecyclerView.getContext().bindService(mIntent, this, 0);
    }

    public void disable() {
        if (mBinder != null) {
            mBinder.removeListener(this);
        }
        mRecyclerView.getContext().unbindService(this);
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        mBinder = (SaveService.SaveServiceBinder) service;
        mItemsIds.clear();
        mItemsIds.addAll(mBinder.getAllIds());
        mBinder.addListener(this);
        notifyDataSetChanged();
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        mBinder = null;
        notifyDataSetChanged();
    }

    @Override
    public DownloadHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        DownloadHolder holder = new DownloadHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_download, parent, false));
        holder.imageRemove.setOnClickListener(this);
        holder.imageRemove.setImageDrawable(mIcons[2]);
        holder.imageRemove.setTag(holder);
        holder.imagePause.setOnClickListener(this);
        holder.imagePause.setImageDrawable(mIcons[1]);
        holder.imagePause.setTag(holder);
        holder.imageResume.setOnClickListener(this);
        holder.imageResume.setImageDrawable(mIcons[0]);
        holder.imageResume.setTag(holder);
        return holder;
    }

    @Override
    public void onBindViewHolder(DownloadHolder holder, int position) {
        if (mBinder != null) {
            int id = (int) getItemId(position);
            holder.fill(mBinder.getItemById(id), mBinder.getTaskStatus(id));
        }
    }

    @Override
    public int getItemCount() {
        return mBinder != null ? mBinder.getTaskCount() : 0;
    }

    @Override
    public long getItemId(int position) {
        if (mItemsIds.size() < position + 1) {
            mItemsIds.clear();
            mItemsIds.addAll(mBinder.getAllIds());
        }
        return mItemsIds.get(position);
    }

    @Override
    public void onProgressUpdated(int id) {
        int position = mItemsIds.indexOf(id);
        RecyclerView.ViewHolder holder = mRecyclerView.findViewHolderForAdapterPosition(position);
        if (mBinder != null && holder != null && holder instanceof DownloadHolder) {
            DownloadInfo item = mBinder.getItemById(id);
            if (item.pos < item.max) {
                ((DownloadHolder) holder).updateProgress(
                        item.pos * 100 + item.getChapterProgressPercent(),
                        item.max * 100,
                        item.chaptersProgresses[item.pos],
                        item.chaptersSizes[item.pos],
                        item.chapters.get(item.pos).name
                );
            } else {
                notifyItemChanged(position);
            }
        }
    }

    @Override
    public void onDataUpdated(int id) {
        /*int pos = mItemsIds.indexOf(id);
        if (pos >= 0) {
            notifyItemChanged(pos);
        } else {
            onDataUpdated();
        }*/
        onDataUpdated();
    }

    @Override
    public void onDataUpdated() {
        mItemsIds.clear();
        mItemsIds.addAll(mBinder.getAllIds());
        notifyDataSetChanged();
    }

    public void setTaskPaused(boolean paused) {
        if (mBinder != null) {
            if (paused) {
                mBinder.pauseAll();
            } else {
                mBinder.resumeAll();
            }
        }
    }

    @Override
    public void onClick(View view) {
        if (mBinder == null) return;
        Object tag = view.getTag();
        if (tag != null && tag instanceof DownloadHolder) {
            DownloadHolder holder = (DownloadHolder) tag;
            final int pos = holder.getAdapterPosition();
            switch (view.getId()) {
                case R.id.buttonRemove:
                    new AlertDialog.Builder(view.getContext())
                            .setMessage(view.getContext().getString(R.string.download_unqueue_confirm, holder.mTextViewTitle.getText()))
                            .setPositiveButton(R.string.action_remove, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    if (mBinder != null) {
                                        mBinder.cancelAndRemove((int) getItemId(pos));
                                    }
                                }
                            })
                            .setNegativeButton(android.R.string.cancel, null)
                            .create()
                            .show();
                    break;
                case R.id.buttonPause:
                    mBinder.setPaused((int) getItemId(pos), true);
                    notifyItemChanged(pos);
                    break;
                case R.id.buttonResume:
                    mBinder.setPaused((int) getItemId(pos), false);
                    notifyItemChanged(pos);
                    break;
            }
        }
    }

    static class DownloadHolder extends RecyclerView.ViewHolder {

        private final ImageView mImageView;
        private final TextView mTextViewTitle;
        private final TextView mTextViewSubtitle;
        private final TextView mTextViewState;
        private final TextView mTextViewPercent;
        private final ProgressBar mProgressBarPrimary;
        private final ProgressBar mProgressBarSecondary;
        final ImageView imageRemove;
        final ImageView imagePause;
        final ImageView imageResume;

        DownloadHolder(View itemView) {
            super(itemView);
            mImageView = itemView.findViewById(R.id.imageView);
            mTextViewTitle = itemView.findViewById(R.id.textView_title);
            mTextViewSubtitle = itemView.findViewById(R.id.textView_subtitle);
            mTextViewState = itemView.findViewById(R.id.textView_state);
            mProgressBarPrimary = itemView.findViewById(R.id.progressBar_primary);
            mProgressBarSecondary = itemView.findViewById(R.id.progressBar_secondary);
            mTextViewPercent = itemView.findViewById(R.id.textView_percent);
            imageRemove = itemView.findViewById(R.id.buttonRemove);
            imagePause = itemView.findViewById(R.id.buttonPause);
            imageResume = itemView.findViewById(R.id.buttonResume);
        }

        @SuppressLint("SetTextI18n")
        public void fill(DownloadInfo data, ExStatus status) {
            mTextViewTitle.setText(data.name);
            ImageUtils.setThumbnail(mImageView, data.preview, ThumbSize.THUMB_SIZE_LIST);
            switch (status) {
                case PENDING:
                    mTextViewState.setText(R.string.queue);
                    imagePause.setVisibility(View.GONE);
                    imageResume.setVisibility(View.GONE);
                    imageRemove.setVisibility(View.VISIBLE);
                    break;
                case FINISHED:
                    mTextViewState.setText(R.string.completed);
                    imagePause.setVisibility(View.GONE);
                    imageResume.setVisibility(View.GONE);
                    imageRemove.setVisibility(View.GONE);
                    break;
                case RUNNING:
                    mTextViewState.setText(R.string.saving_manga);
                    imagePause.setVisibility(View.VISIBLE);
                    imageResume.setVisibility(View.GONE);
                    imageRemove.setVisibility(View.VISIBLE);
                    break;
                case PAUSED:
                    mTextViewState.setText(R.string.paused);
                    imagePause.setVisibility(View.GONE);
                    imageResume.setVisibility(View.VISIBLE);
                    imageRemove.setVisibility(View.VISIBLE);
                    break;
            }
            if (data.pos < data.max) {
                updateProgress(data.pos, data.max, data.chaptersProgresses[data.pos], data.chaptersSizes[data.pos],
                        data.chapters.get(data.pos).name);
            } else {
                mProgressBarPrimary.setProgress(mProgressBarPrimary.getMax());
                mProgressBarSecondary.setProgress(mProgressBarSecondary.getMax());
                mTextViewPercent.setText("100%");
                mTextViewSubtitle.setText(itemView.getContext().getString(R.string.chapters_total, data.max));
            }
        }

        /**
         *
         * @param tPos current chapter
         * @param tMax chapters count
         * @param cPos current page
         * @param cMax pages count
         * @param subtitle chapter name
         */
        @SuppressLint("SetTextI18n")
        void updateProgress(int tPos, int tMax, int cPos, int cMax, String subtitle) {
            mProgressBarPrimary.setMax(tMax);
            mProgressBarPrimary.setProgress(tPos);
            mProgressBarSecondary.setMax(cMax);
            mProgressBarSecondary.setProgress(cPos);
            mTextViewSubtitle.setText(subtitle);
            mTextViewPercent.setText((tMax == 0 ? 0 : tPos * 100 / tMax) + "%");
        }
    }
}
