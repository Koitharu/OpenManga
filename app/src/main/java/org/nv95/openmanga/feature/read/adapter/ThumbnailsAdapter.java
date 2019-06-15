package org.nv95.openmanga.feature.read.adapter;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.nv95.openmanga.R;
import org.nv95.openmanga.components.AutoHeightLayout;
import org.nv95.openmanga.feature.read.reader.PageWrapper;
import org.nv95.openmanga.dialogs.NavigationListener;
import org.nv95.openmanga.items.ThumbSize;
import org.nv95.openmanga.utils.ImageUtils;

import java.util.List;

/**
 * Created by nv95 on 18.11.16.
 */

public class ThumbnailsAdapter extends RecyclerView.Adapter<ThumbnailsAdapter.ThumbHolder> implements View.OnClickListener {

    private final List<PageWrapper> mPages;
    @Nullable
    private NavigationListener mListener;
    private int mCurrentPosition;

    public ThumbnailsAdapter(List<PageWrapper> pages) {
        mPages = pages;
        mListener = null;
    }

    public void setNavigationListener(@Nullable NavigationListener listener) {
        mListener = listener;
    }

    public void setCurrentPosition(int pos) {
        int lastPos = mCurrentPosition;
        mCurrentPosition = pos;
        if (lastPos != -1) {
            notifyItemChanged(lastPos);
        }
        if (mCurrentPosition != -1) {
            notifyItemChanged(mCurrentPosition);
        }
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
            ImageUtils.setThumbnail(holder.imageView, "file://" + pw.getFilename(), ThumbSize.THUMB_SIZE_MEDIUM);
            holder.textView.setText(null);
        } else {
            holder.imageView.setImageResource(R.drawable.placeholder);
            holder.textView.setText(String.valueOf(position + 1));
        }
        holder.setSelected(position == mCurrentPosition);
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
        final TextView textView;
        final View selector;
        private boolean mSelected;

        ThumbHolder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageView);
            textView = itemView.findViewById(R.id.textView);
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
                    imageView.performClick();
                case MotionEvent.ACTION_CANCEL:
                    if (!mSelected) {
                        selector.setVisibility(View.GONE);
                    }
                    break;
            }
            return false;
        }

        private void setSelected(boolean selected) {
            mSelected = selected;
            selector.setVisibility(mSelected ? View.VISIBLE : View.INVISIBLE);
        }
    }
}
