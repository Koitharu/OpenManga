package org.nv95.openmanga.preview;

import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by koitharu on 22.01.18.
 */

public abstract class PageHolder {

	@NonNull
	private final View mView;

	public PageHolder(@NonNull ViewGroup parent, @LayoutRes int layoutResId) {
		mView = onCreateView(parent, layoutResId);
		onViewCreated(mView);
	}

	@NonNull
	protected View onCreateView(@NonNull ViewGroup parent, @LayoutRes int layoutResId) {
		return LayoutInflater.from(parent.getContext()).inflate(layoutResId, parent, false);
	}

	protected abstract void onViewCreated(@NonNull View view);

	String getTitle() {
		return String.valueOf(mView.getTag());
	}

	@NonNull
	public final View getView() {
		return mView;
	}
}
