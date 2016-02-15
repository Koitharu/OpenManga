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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import org.nv95.openmanga.DownloadService;
import org.nv95.openmanga.R;
import org.nv95.openmanga.components.AsyncImageView;
import org.nv95.openmanga.items.MangaInfo;

/**
 * Created by nv95 on 03.01.16.
 */
public class DownloadsAdapter extends BaseAdapter implements ServiceConnection {
    private final Intent intent;// = new Intent("org.nv95.openmanga.SaveService");
    @Nullable
    private DownloadService.DownloadBinder mBinder;
    private LayoutInflater inflater;

    public DownloadsAdapter(Context context) {
        intent = new Intent(context, DownloadService.class);
        mBinder = null;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return mBinder == null ? 0 : mBinder.getQueueSize();
    }

    @Override
    public MangaInfo getItem(int position) {
        return mBinder == null ? null : mBinder.getQueue()[position];
    }

    @Override
    public long getItemId(int position) {
        return getItem(position).hashCode();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.item_download, null);
            viewHolder = new ViewHolder();
            viewHolder.textViewTitle = (TextView) convertView.findViewById(R.id.textView_title);
            viewHolder.textViewStatus = (TextView) convertView.findViewById(R.id.textView_status);
            viewHolder.asyncImageView = (AsyncImageView) convertView.findViewById(R.id.imageView);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        MangaInfo info = getItem(position);
        viewHolder.textViewTitle.setText(info.getName());
        viewHolder.asyncImageView.setImageAsync(info.getPreview());
        return convertView;
    }

    public void enable() {
        inflater.getContext().bindService(intent, this, 0);
    }

    public void disable() {
        inflater.getContext().unbindService(this);
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        mBinder = (DownloadService.DownloadBinder) service;
        notifyDataSetChanged();
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        mBinder = null;
        notifyDataSetInvalidated();
    }

    private static class ViewHolder {
        TextView textViewTitle;
        TextView textViewStatus;
        AsyncImageView asyncImageView;
    }
}
