package org.nv95.openmanga.common;

import android.content.Context;
import android.support.annotation.CallSuper;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * Created by koitharu on 29.01.18.
 */

public abstract class DataViewHolder<D> extends RecyclerView.ViewHolder {

	@Nullable
	private D mData;

	public DataViewHolder(View itemView) {
		super(itemView);
	}

	@CallSuper
	public void bind(D d) {
		mData = d;
	}

	@CallSuper
	public void recycle() {
		mData = null;
	}

	@Nullable
	protected final D getData() {
		return mData;
	}

	public final Context getContext() {
		return itemView.getContext();
	}
}
