package org.nv95.openmanga.feature.settings.main.adapter;

import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;

import org.nv95.openmanga.R;
import org.nv95.openmanga.feature.settings.main.model.PreferenceHeaderItem;
import org.nv95.openmanga.utils.LayoutUtils;

import java.util.ArrayList;

/**
 * Created by admin on 24.07.17.
 */

public class SettingsHeadersAdapter extends RecyclerView.Adapter<SettingsHeadersAdapter.PreferenceHolder> {

    private final ArrayList<PreferenceHeaderItem> mDataset;
    private final AdapterView.OnItemClickListener mClickListener;
    private int mCurrentPosition = -1;

    public SettingsHeadersAdapter(ArrayList<PreferenceHeaderItem> headers, AdapterView.OnItemClickListener clickListener) {
        mDataset = headers;
        mClickListener = clickListener;
        setHasStableIds(true);
    }

    @Override
    public PreferenceHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new PreferenceHolder(LayoutInflater.from(parent.getContext())
        .inflate(R.layout.item_pref_header, parent,false), mClickListener);
    }

    public void setActivatedPosition(int pos) {
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
    public void onBindViewHolder(PreferenceHolder holder, int position) {
        PreferenceHeaderItem item = mDataset.get(position);
        holder.text.setText(item.title);
        holder.icon.setImageDrawable(item.icon);
        holder.setActivated(position == mCurrentPosition);
    }

    @Override
    public long getItemId(int position) {
        return mDataset.get(position).title.hashCode();
    }

    @Override
    public int getItemCount() {
        return mDataset.size();
    }

    static class PreferenceHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private final ImageView icon;
        private final TextView text;
        private final AdapterView.OnItemClickListener clickListener;

        PreferenceHolder(View itemView, AdapterView.OnItemClickListener listener) {
            super(itemView);
            icon = itemView.findViewById(android.R.id.icon);
            text = itemView.findViewById(android.R.id.text1);
            clickListener = listener;
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            clickListener.onItemClick(null, itemView, getAdapterPosition(), getItemId());
        }

        void setActivated(boolean activated) {
            if (activated) {
                itemView.setBackgroundColor(ContextCompat.getColor(itemView.getContext(), R.color.light_gray));
            } else {
                ViewCompat.setBackground(itemView, LayoutUtils.getSelectableBackground(itemView.getContext()));
            }
        }
    }
}
