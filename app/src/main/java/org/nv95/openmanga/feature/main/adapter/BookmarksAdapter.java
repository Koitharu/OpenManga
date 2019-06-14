package org.nv95.openmanga.feature.main.adapter;

import android.annotation.SuppressLint;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.nv95.openmanga.R;
import org.nv95.openmanga.items.Bookmark;
import org.nv95.openmanga.utils.AppHelper;
import org.nv95.openmanga.utils.ImageUtils;

import java.util.List;

/**
 * Created by unravel22 on 18.02.17.
 */

public class BookmarksAdapter extends RecyclerView.Adapter<BookmarksAdapter.BookmarkHolder> implements View.OnClickListener {
    
    private final List<Bookmark> mBookmarks;
    @Nullable
    private final OnBookmarkClickListener mClickListener;
    
    public BookmarksAdapter(List<Bookmark> list, @Nullable OnBookmarkClickListener clickListener) {
        mBookmarks = list;
        mClickListener = clickListener;
    }
    
    @Override
    public BookmarkHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        BookmarkHolder holder = new BookmarkHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_bookmark, parent, false));
        holder.itemView.setOnClickListener(this);
        return holder;
    }
    
    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(BookmarkHolder holder, int position) {
        Bookmark o = mBookmarks.get(position);
        ImageUtils.setThumbnail(holder.imageView, "file://" + o.thumbnailFile);
        holder.textView1.setText(o.name + "\n" + holder.imageView.getContext().getString(R.string.bookmark_pos, o.chapter, o.page));
        holder.textView2.setText(AppHelper.getReadableDateTimeRelative(o.datetime));
        holder.itemView.setTag(o);
    }
    
    @Override
    public int getItemCount() {
        return mBookmarks.size();
    }
    
    @Override
    public void onClick(View v) {
        Object tag = v.getTag();
        if (mClickListener != null && tag instanceof Bookmark) {
            mClickListener.onBookmarkSelected((Bookmark) tag);
        }
    }
    
    static class BookmarkHolder extends RecyclerView.ViewHolder {
        
        final ImageView imageView;
        final TextView textView1;
        final TextView textView2;
        
        BookmarkHolder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageView);
            textView1 = itemView.findViewById(R.id.textView_title);
            textView2 = itemView.findViewById(R.id.textView_subtitle);
        }
    }
    
    public interface OnBookmarkClickListener {
        void onBookmarkSelected(Bookmark bookmark);
    }
}

