package org.nv95.openmanga.adapters;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Checkable;
import android.widget.ImageView;
import android.widget.TextView;

import org.nv95.openmanga.R;
import org.nv95.openmanga.activities.PreviewActivity2;
import org.nv95.openmanga.components.RatingView;
import org.nv95.openmanga.items.MangaInfo;
import org.nv95.openmanga.items.ThumbSize;
import org.nv95.openmanga.lists.PagedList;
import org.nv95.openmanga.utils.ImageUtils;
import org.nv95.openmanga.utils.QuickReadTask;
import org.nv95.openmanga.utils.choicecontrol.ModalChoiceController;
import org.nv95.openmanga.utils.choicecontrol.OnHolderClickListener;

/**
 * Created by nv95 on 30.09.15.
 */
public class MangaListAdapter extends EndlessAdapter<MangaInfo, MangaListAdapter.MangaViewHolder> {

    private boolean mGrid;
    private ThumbSize mThumbSize;
    @Nullable
    private OnItemLongClickListener<MangaViewHolder> mOnItemLongClickListener;
    private final ModalChoiceController mChoiceController;

    public MangaListAdapter(PagedList<MangaInfo> dataset, RecyclerView recyclerView) {
        super(dataset, recyclerView);
        mChoiceController = new ModalChoiceController(this);
    }

    public boolean setGrid(boolean grid) {
        if (mGrid != grid) {
            mGrid = grid;
            notifyDataSetChanged();
            return true;
        } else {
            return false;
        }
    }

    public ModalChoiceController getChoiceController() {
        return mChoiceController;
    }

    public void setThumbnailsSize(@NonNull ThumbSize size) {
        if (!size.equals(mThumbSize)) {
            mThumbSize = size;
            notifyItemRangeChanged(0, getItemCount());
        }
    }

    @Override
    public MangaViewHolder onCreateHolder(ViewGroup parent) {
        MangaViewHolder holder = new MangaViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(mGrid ? R.layout.item_mangagrid : R.layout.item_mangalist, parent, false), mOnItemLongClickListener);
        holder.setListener(mChoiceController);

        return holder;
    }

    @Override
    public long getItemId(MangaInfo data) {
        return data.id;
    }

    @Override
    public void onBindHolder(MangaViewHolder viewHolder, MangaInfo data, int position) {
        viewHolder.fill(data, mThumbSize, mChoiceController.isSelected(position));
    }

    static class MangaViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        @Nullable
        private final OnItemLongClickListener<MangaViewHolder> mLongClickListener;
        private final TextView textViewTitle;
        private final TextView textViewSubtitle;
        private final TextView textViewSummary;
        private final TextView textViewBadge;
        private final RatingView ratingView;
        private final ImageView imageView;
        private final ImageView imageViewStatus;
        @Nullable
        private final Button buttonRead;
        private MangaInfo mData;
        @Nullable
        private OnHolderClickListener mListener;

        MangaViewHolder(View itemView, @Nullable OnItemLongClickListener<MangaViewHolder> longClickListener) {
            super(itemView);
            textViewTitle = itemView.findViewById(R.id.textView_title);
            textViewSubtitle = itemView.findViewById(R.id.textView_subtitle);
            textViewSummary = itemView.findViewById(R.id.textView_summary);
            textViewBadge = itemView.findViewById(R.id.textView_badge);
            ratingView = itemView.findViewById(R.id.ratingView);
            imageView = itemView.findViewById(R.id.imageView);
            buttonRead = itemView.findViewById(R.id.buttonRead);
            imageViewStatus = itemView.findViewById(R.id.imageView_status);
            itemView.setOnClickListener(this);
            mLongClickListener = longClickListener;
            itemView.setOnLongClickListener(this);
            if (buttonRead != null) {
                buttonRead.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        new QuickReadTask(view.getContext())
                                .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, mData);
                    }
                });
            }
        }


        public MangaInfo getData() {
            return mData;
        }

        public void fill(MangaInfo data, ThumbSize thumbSize, boolean checked) {
            mData = data;
            if (itemView instanceof Checkable) {
                ((Checkable) itemView).setChecked(checked);
            }
            textViewTitle.setText(mData.name);
            if (TextUtils.isEmpty(mData.subtitle)) {
                textViewSubtitle.setVisibility(View.GONE);
            } else {
                textViewSubtitle.setText(mData.subtitle);
                textViewSubtitle.setVisibility(View.VISIBLE);
            }
            textViewSummary.setText(mData.genres);
            ratingView.setRating(mData.rating);
            ImageUtils.setThumbnail(imageView, data.preview, thumbSize);
            // TODO: 17.02.16
            //textViewTitle.setTypeface(mData.isCompleted() ? Typeface.DEFAULT_BOLD : Typeface.DEFAULT);
            if (mData.status == MangaInfo.STATUS_UNKNOWN) {
                imageViewStatus.setVisibility(View.INVISIBLE);
            } else {
                imageViewStatus.setImageResource(mData.isCompleted() ? R.drawable.ic_completed : R.drawable.ic_ongoing);
                imageViewStatus.setVisibility(View.VISIBLE);
            }
            if (mData.extra == null) {
                textViewBadge.setVisibility(View.GONE);
            } else {
                textViewBadge.setText(mData.extra);
                textViewBadge.setVisibility(View.VISIBLE);
            }
        }

        public void setListener(@Nullable OnHolderClickListener listener) {
            this.mListener = listener;
        }

        @Override
        public void onClick(View v) {
            if (mListener == null || !mListener.onClick(this)) {
                Context context = v.getContext();
                Intent intent = new Intent(context, PreviewActivity2.class);
                intent.putExtras(mData.toBundle());
                context.startActivity(intent);
            }
        }

        @Override
        public boolean onLongClick(View v) {
            return !(mListener == null || !mListener.onLongClick(this)) || mLongClickListener != null && mLongClickListener.onItemLongClick(this);
        }
    }
}
