package org.nv95.openmanga.legacy.adapters;

import android.support.v7.widget.RecyclerView;

/**
 * Created by nv95 on 28.01.16.
 */
public interface OnItemLongClickListener<VH extends RecyclerView.ViewHolder> {
    boolean onItemLongClick(VH viewHolder);
}
