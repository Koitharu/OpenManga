package org.nv95.openmanga.reader;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.nv95.openmanga.R;
import org.nv95.openmanga.core.models.MangaChapter;
import org.nv95.openmanga.preview.ChaptersListAdapter;
import org.nv95.openmanga.common.utils.CollectionsUtils;

import java.util.ArrayList;

/**
 * Created by koitharu on 09.01.18.
 */

public final class ChaptersDialogFragment extends BottomSheetDialogFragment {

	private RecyclerView mRecyclerView;

	private ArrayList<MangaChapter> mChaptersList;
	private long mCurrentId;

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		final Bundle args = getArguments();
		assert args != null;
		mChaptersList = args.getParcelableArrayList("chapters");
		mCurrentId = args.getLong("current_id");
	}

	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		return inflater.inflate(R.layout.dialog_chapters, container, false);
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		mRecyclerView = view.findViewById(R.id.recyclerView);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		final Activity activity = getActivity();
		assert activity != null;
		mRecyclerView.addItemDecoration(new DividerItemDecoration(activity, LinearLayoutManager.VERTICAL));
		ChaptersListAdapter adapter = new ChaptersListAdapter(activity, mChaptersList, (ChaptersListAdapter.OnChapterClickListener) activity);
		adapter.setCurrentChapterId(mCurrentId);
		mRecyclerView.setAdapter(adapter);
		int current = CollectionsUtils.findPositionById(mChaptersList, mCurrentId);
		if (current != -1) {
			if (current > 2) {
				mRecyclerView.scrollToPosition(current - 1);
			}
		}
	}
}
