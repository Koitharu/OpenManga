package org.nv95.openmanga.reader.thumbview;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.nv95.openmanga.R;
import org.nv95.openmanga.core.models.MangaPage;
import org.nv95.openmanga.common.views.recyclerview.SpaceItemDecoration;
import org.nv95.openmanga.common.utils.ResourceUtils;

import java.util.ArrayList;

/**
 * Created by koitharu on 13.01.18.
 */

public final class ThumbViewFragment extends BottomSheetDialogFragment implements OnThumbnailClickListener {

	private RecyclerView mRecyclerView;
	private ArrayList<MangaPage> mPages;

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		final Bundle args = getArguments();
		assert args != null;
		mPages = args.getParcelableArrayList("pages");
	}

	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		return inflater.inflate(R.layout.recyclerview, container, false);
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		mRecyclerView = view.findViewById(R.id.recyclerView);
		mRecyclerView.setLayoutManager(new GridLayoutManager(view.getContext(), 4));
		mRecyclerView.addItemDecoration(new SpaceItemDecoration(ResourceUtils.dpToPx(view.getResources(), 4)));
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		final Activity activity = getActivity();
		ThumbViewAdapter adapter = new ThumbViewAdapter(activity, mPages, this);
		mRecyclerView.setAdapter(adapter);
	}

	@Override
	public void onThumbnailClick(int position) {
		final Activity activity = getActivity();
		if (activity != null && activity instanceof OnThumbnailClickListener) {
			((OnThumbnailClickListener) activity).onThumbnailClick(position);
			dismiss();
		}
	}
}
