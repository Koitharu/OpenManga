package org.nv95.openmanga.reader;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import org.nv95.openmanga.AppBaseDialogFragment;
import org.nv95.openmanga.R;
import org.nv95.openmanga.common.utils.CollectionsUtils;
import org.nv95.openmanga.common.utils.LayoutUtils;
import org.nv95.openmanga.core.models.MangaChapter;
import org.nv95.openmanga.preview.chapters.ChaptersListAdapter;

import java.util.ArrayList;

/**
 * Created by koitharu on 09.01.18.
 */

public final class ChaptersDialogFragment extends AppBaseDialogFragment implements View.OnClickListener, Runnable {

	private RecyclerView mRecyclerView;
	private Button mButtonNext;

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
		mButtonNext = view.findViewById(R.id.button_next);
		mButtonNext.setOnClickListener(this);
		view.findViewById(R.id.button_close).setOnClickListener(this);
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
		mRecyclerView.post(this);
	}

	@Override
	public void onClick(View v) {
		final Activity activity = getActivity();
		if (activity != null && activity instanceof View.OnClickListener) {
			((View.OnClickListener) activity).onClick(v);
		}
		dismiss();
	}

	@Override
	public void run() {
		int current = CollectionsUtils.findChapterPositionById(mChaptersList, mCurrentId);
		if (current != -1) {
			LayoutUtils.scrollToCenter(mRecyclerView, current);
			//((LinearLayoutManager)mRecyclerView.getLayoutManager()).scrollToPositionWithOffset(current, 20);
			if (current == mChaptersList.size() - 1) {
				mButtonNext.setEnabled(false);
			}
		}
	}
}
