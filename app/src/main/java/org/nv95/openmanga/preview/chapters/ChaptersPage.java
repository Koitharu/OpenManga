package org.nv95.openmanga.preview.chapters;

import android.support.annotation.NonNull;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.nv95.openmanga.R;
import org.nv95.openmanga.preview.PageHolder;

/**
 * Created by koitharu on 22.01.18.
 */

public final class ChaptersPage extends PageHolder {

	public RecyclerView mRecyclerViewChapters;
	public TextView mTextViewChaptersHolder;

	public ChaptersPage(@NonNull ViewGroup parent) {
		super(parent, R.layout.page_manga_chapters);
	}

	@Override
	protected void onViewCreated(@NonNull View view) {
		mRecyclerViewChapters = view.findViewById(R.id.recyclerView_chapters);
		mRecyclerViewChapters.addItemDecoration(new DividerItemDecoration(view.getContext(), LinearLayoutManager.VERTICAL));
		mTextViewChaptersHolder = view.findViewById(R.id.textView_chapters_holder);
	}
}
