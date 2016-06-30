package org.nv95.openmanga.utils.choicecontrol;

import android.annotation.SuppressLint;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.ActionMode;
import android.widget.Checkable;

import java.util.HashMap;

/**
 * Created by nv95 on 30.06.16.
 */

public class ModalChoiceController implements OnHolderClickListener {

    private boolean mEnabled;
    private final HashMap<Integer,RecyclerView.ViewHolder> mSelected;
    @Nullable
    private ModalChoiceCallback mCallback;
    @Nullable
    private ActionMode mActionMode;

    @SuppressLint("UseSparseArrays")
    public ModalChoiceController() {
        mSelected = new HashMap<>();
        mActionMode = null;
        mEnabled = false;
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
        int[] res = new int[mSelected.size()];
        Integer[] t = mSelected.keySet().toArray(new Integer[mSelected.size()]);
        for (int i=0;i<t.length;i++) {
            res[i] = t[i];
        }
        return res;
    }

    public boolean isSelected(int position) {
        return mSelected.containsKey(position);
    }

    public void clearSelection() {
        for (RecyclerView.ViewHolder o : mSelected.values()) {
            if (o.itemView instanceof Checkable) {
                ((Checkable) o.itemView).setChecked(false);
            }
        }
        mSelected.clear();
        if (mActionMode != null) {
            mActionMode.finish();
            mActionMode = null;
        }
    }

    private void select(RecyclerView.ViewHolder holder) {
        if (mSelected.size() == 0 && mCallback != null) {
            mActionMode = holder.itemView.startActionMode(mCallback);
        }
        mSelected.put(holder.getAdapterPosition(), holder);
        if (holder.itemView instanceof Checkable) {
            ((Checkable) holder.itemView).setChecked(true);
        }
        if (mCallback != null && mActionMode != null) {
            mCallback.onChoiceChanged(mActionMode, this, mSelected.size());
        }
    }

    private void deselect(RecyclerView.ViewHolder holder) {
        mSelected.remove(holder.getAdapterPosition());
        if (holder.itemView instanceof Checkable) {
            ((Checkable) holder.itemView).setChecked(false);
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
        return mSelected.containsKey(holder.getAdapterPosition());
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
