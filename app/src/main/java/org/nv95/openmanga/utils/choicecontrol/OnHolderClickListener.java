package org.nv95.openmanga.utils.choicecontrol;

import androidx.recyclerview.widget.RecyclerView;

/**
 * Created by nv95 on 30.06.16.
 */

public interface OnHolderClickListener {
    boolean onClick(RecyclerView.ViewHolder viewHolder);
    boolean onLongClick(RecyclerView.ViewHolder viewHolder);
}
