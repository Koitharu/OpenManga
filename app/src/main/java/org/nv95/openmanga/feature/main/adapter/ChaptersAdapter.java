package org.nv95.openmanga.feature.main.adapter;

import android.content.Context;
import android.graphics.Typeface;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
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

import java.util.Collections;
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
    private boolean mReversed = false;
    
    public ChaptersAdapter(Context context) {
        mClickListener = null;
        mLastNumber = -1;
        mDataset = new ChaptersList();
        mColors = new int[] {
                LayoutUtils.getAttrColor(context, android.R.attr.textColorPrimary),
                LayoutUtils.getAttrColor(context, android.R.attr.textColorSecondary)
        };
        setHasStableIds(true);
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

    public void reverse() {
        Collections.reverse(mDataset);
        notifyDataSetChanged();
        mReversed = !mReversed;
    }

    public boolean isReversed() {
        return mReversed;
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
    public long getItemId(int position) {
        if (mLastNumber >= 0) {
            return position == 0 ? 0 : mDataset.get(position - 1).id();
        } else {
            return mDataset.get(position).id();
        }
    }

    @Override
    public int getItemCount() {
        return mDataset.size() + (mLastNumber >= 0 ? 1 : 0);
    }
    
    @Override
    public void onChapterClick(int pos, MangaChapter chapter, RecyclerView.ViewHolder viewHolder) {
        if (mClickListener != null) {
            if (mLastNumber < 0) {
                mClickListener.onChapterClick(pos, mDataset.get(pos), viewHolder);
            } else {
                if (pos == -1) {
                    mClickListener.onChapterClick(-1, null, viewHolder);
                } else {
                    mClickListener.onChapterClick(pos - 1, mDataset.get(pos - 1), viewHolder);
                }
            }
        }
    }

    @Override
    public boolean onChapterLongClick(int pos, MangaChapter chapter, RecyclerView.ViewHolder viewHolder) {
        if (mClickListener != null) {
            if (mLastNumber < 0) {
                return mClickListener.onChapterLongClick(pos, mDataset.get(pos), viewHolder);
            } else {
                if (pos == -1) {
                    return mClickListener.onChapterLongClick(-1, null, viewHolder);
                } else {
                    return mClickListener.onChapterLongClick(pos - 1, mDataset.get(pos - 1), viewHolder);
                }
            }
        }
        return false;
    }

    static class HeaderHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        
        private final OnChapterClickListener mListener;
        final TextView textViewTitle;
        final TextView textViewSubtitle;
        
        HeaderHolder(View itemView, OnChapterClickListener listener) {
            super(itemView);
            itemView.findViewById(R.id.button_positive).setOnClickListener(this);
            mListener = listener;
            textViewTitle = itemView.findViewById(R.id.textView_title);
            textViewSubtitle = itemView.findViewById(R.id.textView_subtitle);
        }
        
        @Override
        public void onClick(View v) {
            mListener.onChapterClick(-1, null, this);
        }
    }
    
    static class ChapterHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        
        private final OnChapterClickListener mListener;
    
        ChapterHolder(View itemView, OnChapterClickListener listener) {
            super(itemView);
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
            mListener = listener;
        }
        
        public TextView getTextView() {
            return (TextView) itemView;
        }
    
        @Override
        public void onClick(View v) {
            mListener.onChapterClick(getAdapterPosition(), null, this);
        }

        @Override
        public boolean onLongClick(View view) {
            return mListener.onChapterLongClick(getAdapterPosition(), null, this);
        }
    }
}

