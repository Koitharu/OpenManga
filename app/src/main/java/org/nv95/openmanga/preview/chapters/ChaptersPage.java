package org.nv95.openmanga.preview.chapters;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.nv95.openmanga.R;
import org.nv95.openmanga.common.utils.LayoutUtils;
import org.nv95.openmanga.core.models.MangaChaptersList;
import org.nv95.openmanga.core.models.MangaHistory;
import org.nv95.openmanga.preview.PageHolder;

/**
 * Created by koitharu on 22.01.18.
 */

public final class ChaptersPage extends PageHolder {

	private RecyclerView mRecyclerViewChapters;
	private TextView mTextViewChaptersHolder;
	@Nullable
	private ChaptersListAdapter mChaptersAdapter;

	public ChaptersPage(@NonNull ViewGroup parent) {
		super(parent, R.layout.page_manga_chapters);
	}

	@Override
	protected void onViewCreated(@NonNull View view) {
		mRecyclerViewChapters = view.findViewById(R.id.recyclerView_chapters);
		mRecyclerViewChapters.addItemDecoration(new DividerItemDecoration(view.getContext(), LinearLayoutManager.VERTICAL));
		mTextViewChaptersHolder = view.findViewById(R.id.textView_chapters_holder);
	}

	public void setData(MangaChaptersList chapters, @Nullable MangaHistory history,
						ChaptersListAdapter.OnChapterClickListener chapterClickListener) {
		mChaptersAdapter = new ChaptersListAdapter(getContext(), chapters, chapterClickListener);
		if (history == null) {
			mChaptersAdapter.setCurrentChapterId(0);
		} else {
			mChaptersAdapter.setCurrentChapterId(history.chapterId);
		}
		mRecyclerViewChapters.setAdapter(mChaptersAdapter);
		if (chapters.isEmpty()) {
			mTextViewChaptersHolder.setText(R.string.no_chapters_found);
			mTextViewChaptersHolder.setVisibility(View.VISIBLE);
		} else {
			mTextViewChaptersHolder.setVisibility(View.GONE);
		}
	}

	public void setError() {
		mTextViewChaptersHolder.setText(R.string.failed_to_load_chapters);
		mTextViewChaptersHolder.setVisibility(View.VISIBLE);
	}

	public void updateHistory(MangaHistory history) {
		if (mChaptersAdapter != null) {
			mChaptersAdapter.setCurrentChapterId(history.chapterId);
			mChaptersAdapter.notifyDataSetChanged();
		}
	}

	public boolean setReversed(boolean reversed) {
		if (mChaptersAdapter != null && mChaptersAdapter.isReversed() != reversed) {
			final int topPos = LayoutUtils.findLastVisibleItemPosition(mRecyclerViewChapters);
			mChaptersAdapter.reverse();
			if (topPos != -1) {
				int newPos = mChaptersAdapter.getItemCount() - topPos - 1;
				/*if (newPos <= mChaptersAdapter.getItemCount() - 1) {
					newPos += 2;	//toolbar
				}*/
				LayoutUtils.setSelectionFromTop(mRecyclerViewChapters, newPos);
			}
			return true;
		} else {
			return false;
		}
	}

	public void setFilter(String str) {
		final int pos = mChaptersAdapter != null ? mChaptersAdapter.findByNameContains(str) : -1;
		if (pos != -1) {
			LayoutUtils.setSelectionFromTop(mRecyclerViewChapters, pos);
		}
	}
}
