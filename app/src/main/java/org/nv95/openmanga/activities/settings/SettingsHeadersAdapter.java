package org.nv95.openmanga.activities.settings;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;

import org.nv95.openmanga.R;

import java.util.ArrayList;

/**
 * Created by admin on 24.07.17.
 */

public class SettingsHeadersAdapter extends RecyclerView.Adapter<SettingsHeadersAdapter.PreferenceHolder> {

    private final ArrayList<PreferenceHeader> mDataset;
    private final AdapterView.OnItemClickListener mClickListener;

    public SettingsHeadersAdapter(ArrayList<PreferenceHeader> headers, AdapterView.OnItemClickListener clickListener) {
        mDataset = headers;
        mClickListener = clickListener;
        setHasStableIds(true);
    }

    @Override
    public PreferenceHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new PreferenceHolder(LayoutInflater.from(parent.getContext())
        .inflate(R.layout.item_pref_header, parent,false), mClickListener);
    }

    @Override
    public void onBindViewHolder(PreferenceHolder holder, int position) {
        PreferenceHeader item = mDataset.get(position);
        holder.text.setText(item.title);
        holder.icon.setImageDrawable(item.icon);
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
    }
}
