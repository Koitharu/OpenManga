package org.nv95.openmanga.feature.main.adapter;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Checkable;
import android.widget.ImageView;
import android.widget.TextView;

import org.nv95.openmanga.R;
import org.nv95.openmanga.feature.preview.PreviewActivity2;
import org.nv95.openmanga.components.RatingView;
import org.nv95.openmanga.feature.main.dialog.PreviewDialog;
import org.nv95.openmanga.feature.manga.domain.MangaInfo;
import org.nv95.openmanga.items.ThumbSize;
import org.nv95.openmanga.lists.PagedList;
import org.nv95.openmanga.utils.ImageUtils;
import org.nv95.openmanga.utils.LayoutUtils;
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
        if (MangaViewHolder.PADDING_4 == 0) {
            MangaViewHolder.PADDING_4 = LayoutUtils.DpToPx(recyclerView.getResources(), 4);
            MangaViewHolder.PADDING_16 = LayoutUtils.DpToPx(recyclerView.getResources(), 16);
            MangaViewHolder.HEIGHT_68 = LayoutUtils.DpToPx(recyclerView.getResources(), 68);
            MangaViewHolder.HEIGHT_42 = LayoutUtils.DpToPx(recyclerView.getResources(), 42);
        }
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

    public static class MangaViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {

        private static int PADDING_16 = 0;
        private static int PADDING_4 = 0;
        private static int HEIGHT_68 = 0;
        private static int HEIGHT_42 = 0;

        @Nullable
        private final OnItemLongClickListener<MangaViewHolder> mLongClickListener;
        private final TextView textViewTitle;
        @Nullable
        private final TextView textViewSubtitle;
        private final TextView textViewSummary;
        private final TextView textViewBadge;
        @Nullable
        private final RatingView ratingView;
        private final ImageView imageView;
        @Nullable
        private final ImageView imageViewStatus;
        private final View buttonRead;
        private MangaInfo mData;
        @Nullable
        private OnHolderClickListener mListener;
        @Nullable
        private final View cellFooter;
        private int viewMode;

        public MangaViewHolder(final View itemView, @Nullable OnItemLongClickListener<MangaViewHolder> longClickListener) {
            super(itemView);
            textViewTitle = itemView.findViewById(R.id.textView_title);
            textViewSubtitle = itemView.findViewById(R.id.textView_subtitle);
            textViewSummary = itemView.findViewById(R.id.textView_summary);
            textViewBadge = itemView.findViewById(R.id.textView_badge);
            ratingView = itemView.findViewById(R.id.ratingView);
            imageView = itemView.findViewById(R.id.imageView);
            buttonRead = itemView.findViewById(R.id.buttonRead);
            cellFooter = itemView.findViewById(R.id.cell_footer);
            imageViewStatus = itemView.findViewById(R.id.imageView_status);
            itemView.setOnClickListener(this);
            mLongClickListener = longClickListener;
            itemView.setOnLongClickListener(this);
            buttonRead.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (itemView instanceof Checkable && ((Checkable) itemView).isChecked()) {
                        return;
                    }
                    new QuickReadTask(view.getContext())
                            .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, mData);
                }
            });
            buttonRead.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    if (itemView instanceof Checkable && ((Checkable) itemView).isChecked()) {
                        return false;
                    }
                    new PreviewDialog(view.getContext())
                            .show(mData);
                    return true;
                }
            });
        }

        public MangaInfo getData() {
            return mData;
        }

        private void updateViewMode(ThumbSize thumbSize) {
            int mode = 3; //large grid
            if (cellFooter == null) {
                mode = 0; //list
            } else if (thumbSize.getWidth() <= ThumbSize.THUMB_SIZE_SMALL.getWidth()) {
                mode = 1; //small grid
            } else if (thumbSize.getWidth() <= ThumbSize.THUMB_SIZE_MEDIUM.getWidth()) {
                mode = 2; //medium grid
            }
            if (viewMode == mode) {
                return;
            }
            viewMode = mode;
            switch (viewMode) {
                case 0:
                    buttonRead.setVisibility(View.VISIBLE);
                    textViewSummary.setVisibility(View.VISIBLE);
                    textViewTitle.setMaxLines(2);
                    break;
                case 1:
                    buttonRead.setVisibility(View.GONE);
                    textViewSummary.setVisibility(View.GONE);
                    textViewTitle.setMaxLines(2);
                    cellFooter.getLayoutParams().height = HEIGHT_42;
                    cellFooter.setPadding(PADDING_4, PADDING_4, PADDING_4, PADDING_4);
                    break;
                case 2:
                    buttonRead.setVisibility(View.GONE);
                    textViewTitle.setMaxLines(1);
                    textViewSummary.setVisibility(View.VISIBLE);
                    cellFooter.getLayoutParams().height = HEIGHT_68;
                    cellFooter.setPadding(PADDING_4, PADDING_4, PADDING_4, PADDING_4);
                    break;
                case 3:
                    buttonRead.setVisibility(View.VISIBLE);
                    textViewSummary.setVisibility(View.VISIBLE);
                    textViewTitle.setMaxLines(1);
                    cellFooter.getLayoutParams().height = HEIGHT_68;
                    cellFooter.setPadding(PADDING_16, PADDING_16, PADDING_16, PADDING_16);
            }
        }

        public void fill(MangaInfo data, ThumbSize thumbSize, boolean checked) {
            mData = data;
            updateViewMode(thumbSize);
            if (itemView instanceof Checkable) {
                ((Checkable) itemView).setChecked(checked);
            }
            textViewTitle.setText(mData.name);
            if (textViewSubtitle != null) {
                if (TextUtils.isEmpty(mData.subtitle)) {
                    textViewSubtitle.setVisibility(View.GONE);
                } else {
                    textViewSubtitle.setText(mData.subtitle);
                    textViewSubtitle.setVisibility(View.VISIBLE);
                }
            }
            textViewSummary.setText(mData.genres);
            if (ratingView != null) {
                ratingView.setRating(mData.rating);
            }
            ImageUtils.setThumbnail(imageView, data.preview, thumbSize);
            if (imageViewStatus != null) {
                if (mData.status == MangaInfo.STATUS_UNKNOWN) {
                    imageViewStatus.setVisibility(View.INVISIBLE);
                } else {
                    imageViewStatus.setImageResource(mData.isCompleted() ? R.drawable.ic_completed : R.drawable.ic_ongoing);
                    imageViewStatus.setVisibility(View.VISIBLE);
                }
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
