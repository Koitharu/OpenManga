package org.nv95.openmanga.feature.newchapter.adapter;

import android.content.Context;
import android.content.Intent;

import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.nv95.openmanga.R;
import org.nv95.openmanga.feature.preview.PreviewActivity2;
import org.nv95.openmanga.feature.manga.domain.MangaInfo;
import org.nv95.openmanga.lists.MangaList;
import org.nv95.openmanga.utils.ImageUtils;
import org.nv95.openmanga.utils.diffutil.MangaInfoDiffUtill;

import java.util.List;

/**
 * Created by nv95 on 17.04.16.
 */
public class NewChaptersAdapter extends RecyclerView.Adapter<NewChaptersAdapter.UpdatesHolder> {

    private List<MangaInfo> mDataset;

    public NewChaptersAdapter(MangaList dataset) {
        this.mDataset = dataset;
    }

    public MangaInfo getItem(int position) {
        return mDataset.get(position);
    }

    public void removeItem(int position) {
        mDataset.remove(position);
        notifyItemRemoved(position);
    }

    public void setDataset(List<MangaInfo> dataset) {
        DiffUtil.DiffResult diff = DiffUtil.calculateDiff(new MangaInfoDiffUtill(mDataset, dataset));
        this.mDataset = dataset;
        diff.dispatchUpdatesTo(this);
    }

    @Override
    public UpdatesHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new UpdatesHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_updates, parent, false));
    }

    @Override
    public void onBindViewHolder(UpdatesHolder holder, int position) {
        holder.fill(mDataset.get(position));
    }

    @Override
    public int getItemCount() {
        return mDataset.size();
    }

    static class UpdatesHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private final TextView mTextViewTitle;
        private final ImageView imageView;
        private final TextView mTextViewBadge;
        private final TextView mTextViewSubtitle;
        private MangaInfo mData;

        public UpdatesHolder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageView);
            mTextViewTitle = itemView.findViewById(R.id.textView_title);
            mTextViewSubtitle = itemView.findViewById(R.id.textView_subtitle);
            mTextViewBadge = itemView.findViewById(R.id.textView_badge);
            itemView.setOnClickListener(this);
        }

        public void fill(MangaInfo manga) {
            mData = manga;
            ImageUtils.setThumbnail(imageView, mData.preview);
            mTextViewTitle.setText(mData.name);
            mTextViewSubtitle.setText(mData.subtitle);
            mTextViewBadge.setText(mData.extra);
        }

        @Override
        public void onClick(View v) {
            Context context = v.getContext();
            Intent intent = new Intent(context, PreviewActivity2.class);
            intent.putExtras(mData.toBundle());
            context.startActivity(intent);
        }
    }
}
