package org.nv95.openmanga.utils.choicecontrol;

import android.annotation.SuppressLint;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import android.view.ActionMode;

import org.nv95.openmanga.components.ExtraCheckable;

import java.util.TreeSet;

/**
 * Created by nv95 on 30.06.16.
 */

public class ModalChoiceController implements OnHolderClickListener {

    private boolean mEnabled;
    private final TreeSet<Integer> mSelected;
    @Nullable
    private ModalChoiceCallback mCallback;
    @Nullable
    private ActionMode mActionMode;
    private final RecyclerView.Adapter mAdapter;

    @SuppressLint("UseSparseArrays")
    public ModalChoiceController(RecyclerView.Adapter adapter) {
        mSelected = new TreeSet<>();
        mActionMode = null;
        mEnabled = false;
        mAdapter = adapter;
    }

    public int getSelectedItemsCount() {
        return mSelected.size();
    }

    public void setCallback(ModalChoiceCallback callback) {
        mCallback = callback;
    }

    public void setEnabled(boolean enabled) {
        mEnabled = enabled;
    }

    public int[] getSelectedItemsPositions() {
        Integer[] t = mSelected.toArray(new Integer[mSelected.size()]);
        int[] res = new int[t.length];
        for (int i=0;i<t.length;i++) {
            res[i] = t[i];
        }
        return res;
    }

    public boolean isSelected(int position) {
        return mSelected.contains(position);
    }

    public void clearSelection() {
        int[] keys = getSelectedItemsPositions();
        mSelected.clear();
        for (int i : keys) {
            mAdapter.notifyItemChanged(i);
        }
        if (mActionMode != null) {
            mActionMode.finish();
            mActionMode = null;
        }
    }

    public void selectAll(int viewType) {
        mSelected.clear();
        int count = mAdapter.getItemCount();
        for (int i=0;i<count;i++) {
            if (mAdapter.getItemViewType(i) == viewType) {
                mSelected.add(i);
            }
        }
        mAdapter.notifyDataSetChanged();
        if (mCallback != null && mActionMode != null) {
            mCallback.onChoiceChanged(mActionMode, this, mSelected.size());
        }
    }

    private void select(RecyclerView.ViewHolder holder) {
        if (mSelected.size() == 0 && mCallback != null) {
            mActionMode = holder.itemView.startActionMode(mCallback);
        }
        mSelected.add(holder.getAdapterPosition());
        if (holder.itemView instanceof ExtraCheckable) {
            ((ExtraCheckable) holder.itemView).setCheckedAnimated(true);
        }
        if (mCallback != null && mActionMode != null) {
            mCallback.onChoiceChanged(mActionMode, this, mSelected.size());
        }
    }

    private void deselect(RecyclerView.ViewHolder holder) {
        mSelected.remove(holder.getAdapterPosition());
        if (holder.itemView instanceof ExtraCheckable) {
            ((ExtraCheckable) holder.itemView).setCheckedAnimated(false);
        }
        if (mCallback != null && mActionMode != null) {
            if (mSelected.size() == 0) {
                mActionMode.finish();
                mActionMode = null;
            } else {
                mCallback.onChoiceChanged(mActionMode, this, mSelected.size());
            }
        }
    }

    private boolean isSelected(RecyclerView.ViewHolder holder) {
        return isSelected(holder.getAdapterPosition());
    }

    @Override
    public boolean onClick(RecyclerView.ViewHolder viewHolder) {
        if (!mEnabled || mSelected.size() == 0) {
            return false;
        } else {
            if (isSelected(viewHolder)) {
                deselect(viewHolder);
            } else {
                select(viewHolder);
            }
            return true;
        }
    }

    @Override
    public boolean onLongClick(RecyclerView.ViewHolder viewHolder) {
        if (!mEnabled) {
            return false;
        }
        if (isSelected(viewHolder)) {
            deselect(viewHolder);
        } else {
            select(viewHolder);
        }
        return true;
    }
}
