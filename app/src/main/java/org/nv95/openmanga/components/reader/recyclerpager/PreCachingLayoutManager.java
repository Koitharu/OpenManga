package org.nv95.openmanga.components.reader.recyclerpager;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

/**
 * Created by nv95 on 17.11.16.
 */

public class PreCachingLayoutManager extends LinearLayoutManager {

    private final int mExtraSize;

    public PreCachingLayoutManager(Context context, int orientation, boolean reverseLayout) {
        super(context, orientation, reverseLayout);
        mExtraSize = orientation == VERTICAL ?
                context.getResources().getDisplayMetrics().heightPixels :
                context.getResources().getDisplayMetrics().widthPixels;
    }

    @Override
    protected int getExtraLayoutSpace(RecyclerView.State state) {
        return mExtraSize;
    }
}
