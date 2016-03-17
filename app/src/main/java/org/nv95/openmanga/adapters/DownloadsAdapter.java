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

package org.nv95.openmanga.adapters;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.nv95.openmanga.R;
import org.nv95.openmanga.components.AsyncImageView;
import org.nv95.openmanga.components.ProgressGroup;
import org.nv95.openmanga.items.DownloadInfo;
import org.nv95.openmanga.items.ThumbSize;
import org.nv95.openmanga.services.DownloadService;

/**
 * Created by nv95 on 03.01.16.
 */
public class DownloadsAdapter extends RecyclerView.Adapter<DownloadsAdapter.DownloadHolder>
        implements ServiceConnection, DownloadService.OnProgressUpdateListener {
    private final Intent intent;// = new Intent("org.nv95.openmanga.SaveService");
    @Nullable
    private DownloadService.DownloadBinder mBinder;
    private LayoutInflater inflater;

    public DownloadsAdapter(Context context) {
        intent = new Intent(context, DownloadService.class);
        mBinder = null;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public void enable() {
        inflater.getContext().bindService(intent, this, 0);
    }

    public void disable() {
        if (mBinder != null) {
            mBinder.removeListener(this);
        }
        inflater.getContext().unbindService(this);
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        mBinder = (DownloadService.DownloadBinder) service;
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
        return new DownloadHolder(inflater.inflate(R.layout.item_download, parent, false));
    }

    @Override
    public void onBindViewHolder(DownloadHolder holder, int position) {
        if (mBinder != null) {
            holder.fill(mBinder.getItem(position));
        }
    }

    @Override
    public int getItemCount() {
        return mBinder != null ? mBinder.getCount() : 0;
    }

    @Override
    public void onProgressUpdated(int itemId) {
        notifyDataSetChanged();
    }

    protected static class DownloadHolder extends RecyclerView.ViewHolder {
        private final AsyncImageView mAsyncImageView;
        private final TextView mTextViewTitle;
        private final TextView mTextViewState;
        private final ProgressGroup mProgressGroup;

        public DownloadHolder(View itemView) {
            super(itemView);
            mAsyncImageView = (AsyncImageView) itemView.findViewById(R.id.imageView);
            mTextViewTitle = (TextView) itemView.findViewById(R.id.textView_title);
            mTextViewState = (TextView) itemView.findViewById(R.id.textView_state);
            mProgressGroup = (ProgressGroup) itemView.findViewById(R.id.progressGroup);
        }

        public void fill(DownloadInfo data) {
            mTextViewTitle.setText(data.name);
            mAsyncImageView.setImageThumbAsync(data.preview, ThumbSize.THUMB_SIZE_LIST);
            mTextViewState.setText(data.state.get() == DownloadInfo.STATE_RUNNING ? R.string.saving_manga : R.string.queue);
            final int sz = data.max.get();
            mProgressGroup.setProgressCount(sz);
            for (int i=0;i<sz;i++) {
                mProgressGroup.setProgress(i, data.chapters.get(i).second.progress.get(), data.chapters.get(i).second.max.get());
            }
        }
    }
}
