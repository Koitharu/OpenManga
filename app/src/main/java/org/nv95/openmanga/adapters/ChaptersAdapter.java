package org.nv95.openmanga.adapters;

import android.content.Context;
import android.graphics.Typeface;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.nv95.openmanga.R;
import org.nv95.openmanga.items.MangaChapter;
import org.nv95.openmanga.lists.ChaptersList;
import org.nv95.openmanga.providers.HistoryProvider;
import org.nv95.openmanga.utils.AppHelper;
import org.nv95.openmanga.utils.LayoutUtils;

import java.util.List;

/**
 * Created by unravel22 on 18.02.17.
 */

public class ChaptersAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements OnChapterClickListener {
    
    private final int ITEM_DEFAULT = 0;
    private final int ITEM_HEADER = 1;
    
    private final ChaptersList mDataset;
    private int mLastNumber = 2;
    private long mLastTime = 0;
    @Nullable
    private OnChapterClickListener mClickListener;
    private final int[] mColors;
    
    public ChaptersAdapter(Context context) {
        mClickListener = null;
        mLastNumber = -1;
        mDataset = new ChaptersList();
        mColors = new int[] {
                LayoutUtils.getAttrColor(context, android.R.attr.textColorPrimary),
                LayoutUtils.getAttrColor(context, android.R.attr.textColorSecondary)
        };
    }

    public void setOnItemClickListener(@Nullable OnChapterClickListener listener) {
        mClickListener = listener;
    }
    
    public void setExtra(@Nullable HistoryProvider.HistorySummary hs) {
        mLastNumber = hs != null ? hs.getChapter() : -1;
        mLastTime = hs != null ? hs.getTime() : 0;
    }
    
    public void setData(List<MangaChapter> chapters) {
        mDataset.clear();
        mDataset.addAll(chapters);
    }
    
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == ITEM_HEADER) {
            return new HeaderHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.header_chapter, parent, false), this);
        } else {
            return new ChapterHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chapter, parent, false), this);
        }
    }
    
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof ChapterHolder) {
            MangaChapter ch = mDataset.get(position - (mLastNumber >= 0 ? 1 : 0));
            TextView tv = ((ChapterHolder) holder).getTextView();
            tv.setText(ch.name);
            tv.setTextColor(ch.number >= mLastNumber ? mColors[0] : mColors[1]);
            tv.setTypeface(ch.number == mLastNumber ? Typeface.DEFAULT_BOLD : Typeface.DEFAULT);
        } else if (holder instanceof HeaderHolder) {
            MangaChapter ch = mDataset.getByNumber(mLastNumber);
            if (ch != null) {
                ((HeaderHolder) holder).textViewTitle.setText(ch.name);
                ((HeaderHolder) holder).textViewSubtitle.setText(AppHelper.getReadableDateTimeRelative(mLastTime));
            }
        }
    }
    
    @Override
    public int getItemViewType(int position) {
        return (position == 0 && mLastNumber >= 0) ? ITEM_HEADER : ITEM_DEFAULT;
    }
    
    @Override
    public int getItemCount() {
        return mDataset.size() + (mLastNumber >= 0 ? 1 : 0);
    }
    
    @Override
    public void onChapterClick(int pos, MangaChapter chapter) {
        if (mClickListener != null) {
            if (mLastNumber < 0) {
                mClickListener.onChapterClick(pos, mDataset.get(pos));
            } else {
                if (pos == -1) {
                    mClickListener.onChapterClick(-1, null);
                } else {
                    mClickListener.onChapterClick(pos - 1, mDataset.get(pos - 1));
                }
            }
        }
    }
    
    static class HeaderHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        
        private final OnChapterClickListener mListener;
        final TextView textViewTitle;
        final TextView textViewSubtitle;
        
        public HeaderHolder(View itemView, OnChapterClickListener listener) {
            super(itemView);
            itemView.findViewById(R.id.button_positive).setOnClickListener(this);
            mListener = listener;
            textViewTitle = (TextView) itemView.findViewById(R.id.textView_title);
            textViewSubtitle = (TextView) itemView.findViewById(R.id.textView_subtitle);
        }
        
        @Override
        public void onClick(View v) {
            mListener.onChapterClick(-1, null);
        }
    }
    
    static class ChapterHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        
        private final OnChapterClickListener mListener;
    
        public ChapterHolder(View itemView, OnChapterClickListener listener) {
            super(itemView);
            itemView.setOnClickListener(this);
            mListener = listener;
        }
        
        public TextView getTextView() {
            return (TextView) itemView;
        }
    
        @Override
        public void onClick(View v) {
            mListener.onChapterClick(getAdapterPosition(), null);
        }
    }
}

