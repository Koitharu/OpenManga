package org.nv95.openmanga.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

/**
 * Created by nv95 on 09.08.16.
 */

public abstract class HeaderedAdapter<VH extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private int mHeaders = 0;
    private ArrayList<View> mHeaderViews = new ArrayList<>();

    @Override
    public final int getItemCount() {
        return mHeaders + getDataItemCount();
    }

    @Override
    public int getItemViewType(int position) {
        if (position < mHeaders) {
            return position;
        } else {
            return mHeaders + getDataItemType(position - mHeaders);
        }
    }

    @Override
    public final RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType < mHeaders) {
            return new HeaderHolder(mHeaderViews.get(viewType));
        } else {
            return onCreateDataViewHolder(parent, viewType - mHeaders);
        }
    }

    @Override
    public final void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (!(holder instanceof HeaderedAdapter.HeaderHolder)) {
            //noinspection unchecked
            onBindDataViewHolder((VH) holder, position - mHeaders);
        }
    }

    public void addHeader(View header, int position) {
        mHeaderViews.add(position, header);
        mHeaders++;
        notifyItemInserted(position);
    }

    public abstract int getDataItemCount();

    public int getDataItemType(int position) {
        return 0;
    }

    public abstract VH onCreateDataViewHolder(ViewGroup parent, int viewType);

    public abstract void onBindDataViewHolder(VH holder, int position);

    public int getHeadersCount() {
        return mHeaders;
    }

    private static class HeaderHolder extends RecyclerView.ViewHolder {

        public HeaderHolder(View itemView) {
            super(itemView);
        }
    }
}