package org.nv95.openmanga.feature.main.adapter;

import androidx.recyclerview.widget.RecyclerView;

/**
 * Created by nv95 on 28.01.16.
 */
public interface OnItemLongClickListener<VH extends RecyclerView.ViewHolder> {
    boolean onItemLongClick(VH viewHolder);
}
