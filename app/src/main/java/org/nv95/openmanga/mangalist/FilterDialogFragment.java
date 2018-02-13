package org.nv95.openmanga.mangalist;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import org.nv95.openmanga.R;
import org.nv95.openmanga.common.dialogs.AppBaseBottomSheetDialogFragment;
import org.nv95.openmanga.common.utils.ThemeUtils;
import org.nv95.openmanga.core.models.MangaGenre;
import org.nv95.openmanga.common.views.recyclerview.HeaderDividerItemDecoration;
import org.nv95.openmanga.common.utils.CollectionsUtils;

import java.util.ArrayList;

/**
 * Created by koitharu on 31.12.17.
 */

public final class FilterDialogFragment extends AppBaseBottomSheetDialogFragment implements View.OnClickListener {

	private RecyclerView mRecyclerView;
	private Toolbar mToolbar;
	private Button mButtonApply;
	private Button mButtonReset;
	private AppBarLayout mAppBar;
	private FilterSortAdapter mAdapter;

	private int[] mSorts;
	private MangaGenre[] mGenres;
	private MangaQueryArguments mQueryArgs;

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		final Bundle args = getArguments();
		assert args != null;
		mSorts = args.getIntArray("sorts");
		mGenres = (MangaGenre[]) args.getParcelableArray("genres");
		mQueryArgs = MangaQueryArguments.from(args.getBundle("query"));
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
		mToolbar = view.findViewById(R.id.toolbar);
		mAppBar = view.findViewById(R.id.appbar);
		mButtonApply = view.findViewById(R.id.buttonApply);
		mButtonReset = view.findViewById(R.id.buttonReset);

		mButtonApply.setOnClickListener(this);
		mButtonReset.setOnClickListener(this);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		final Activity activity = getActivity();
		assert activity != null;
		mAdapter = new FilterSortAdapter(activity, mSorts, mGenres, mQueryArgs.sort, mQueryArgs.genresValues());
		mRecyclerView.setAdapter(mAdapter);
		mRecyclerView.addItemDecoration(new HeaderDividerItemDecoration(activity));
		mToolbar.setNavigationOnClickListener(this);

		/*final BottomSheetBehavior behavior = BottomSheetBehavior.from(getDialog().findViewById(android.support.design.R.id.design_bottom_sheet));
		behavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
			@Override
			public void onStateChanged(@NonNull View bottomSheet, int newState) {
				AnimationUtils.setVisibility(mAppBar, newState == BottomSheetBehavior.STATE_EXPANDED ? View.VISIBLE : View.GONE);
			}

			@Override
			public void onSlide(@NonNull View bottomSheet, float slideOffset) {

			}
		});*/
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.buttonReset:
				mAdapter.reset();
				final Toast toast = Toast.makeText(v.getContext(), R.string.filter_resetted, Toast.LENGTH_SHORT);
				toast.setGravity(Gravity.TOP, 0, 0);
				toast.show();
				break;
			case R.id.buttonApply:
				Activity activity = getActivity();
				if (activity != null && activity instanceof FilterCallback) {
					ArrayList<MangaGenre> genres = CollectionsUtils.getIfTrue(mGenres, mAdapter.getSelectedGenres());
					((FilterCallback) activity).setFilter(mAdapter.getSelectedSort(), genres.toArray(new MangaGenre[genres.size()]));
				}
			default:
				dismiss();
		}
	}
}
