package org.nv95.openmanga.adapters;

import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import org.nv95.openmanga.R;
import org.nv95.openmanga.components.AutoHeightLayout;
import org.nv95.openmanga.components.reader.PageWrapper;
import org.nv95.openmanga.dialogs.NavigationListener;
import org.nv95.openmanga.utils.ImageUtils;

import java.util.List;

/**
 * Created by nv95 on 18.11.16.
 */

public class ThumbnailsAdapter extends RecyclerView.Adapter<ThumbnailsAdapter.ThumbHolder> implements View.OnClickListener {

    private final List<PageWrapper> mPages;
    @Nullable
    private NavigationListener mListener;

    public ThumbnailsAdapter(List<PageWrapper> pages) {
        mPages = pages;
        mListener = null;
    }

    public void setNavigationListener(@Nullable NavigationListener listener) {
        mListener = listener;
    }

    @Override
    public ThumbHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ThumbHolder holder = new ThumbHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_thumb, parent, false));
        holder.imageView.setOnClickListener(this);
        return holder;
    }

    @Override
    public void onBindViewHolder(ThumbHolder holder, int position) {
        PageWrapper pw = mPages.get(position);
        if (pw.isLoaded()) {
            ImageUtils.setThumbnail(holder.imageView, "file://" + pw.getFilename());
        } else {
            holder.imageView.setImageResource(R.drawable.placeholder);
        }
        holder.imageView.setTag(position);
    }

    @Override
    public int getItemCount() {
        return mPages.size();
    }

    @Override
    public void onClick(View view) {
        if (mListener != null) {
            int pos = (int) view.getTag();
            mListener.onPageChange(pos);
        }
    }

    static class ThumbHolder extends RecyclerView.ViewHolder implements View.OnTouchListener {

        final ImageView imageView;
        final View selector;

        ThumbHolder(View itemView) {
            super(itemView);
            imageView = (ImageView) itemView.findViewById(R.id.imageView);
            selector = itemView.findViewById(R.id.selector);
            ((AutoHeightLayout)(itemView)).setAspectRatio(1.3f);
            imageView.setOnTouchListener(this);
        }

        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            switch (motionEvent.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    selector.setVisibility(View.VISIBLE);
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    selector.setVisibility(View.GONE);
                    break;
            }
            return false;
        }
    }
}
