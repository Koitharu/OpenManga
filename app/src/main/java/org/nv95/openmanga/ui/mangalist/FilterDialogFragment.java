package org.nv95.openmanga.ui.mangalist;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.nv95.openmanga.R;
import org.nv95.openmanga.content.MangaGenre;
import org.nv95.openmanga.ui.common.HeaderDividerItemDecoration;

/**
 * Created by koitharu on 31.12.17.
 */

public final class FilterDialogFragment extends BottomSheetDialogFragment {

	private RecyclerView mRecyclerView;
	private int[] mSorts;
	private MangaGenre[] mGenres;

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		final Bundle args = getArguments();
		assert args != null;
		mSorts = args.getIntArray("sorts");
		mGenres = (MangaGenre[]) args.getParcelableArray("genres");
	}

	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		return inflater.inflate(R.layout.dialog_filter, container, false);
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
		mRecyclerView.setAdapter(new FilterSortAdapter(activity, mSorts, mGenres));
		mRecyclerView.addItemDecoration(new HeaderDividerItemDecoration(activity));
	}
}
