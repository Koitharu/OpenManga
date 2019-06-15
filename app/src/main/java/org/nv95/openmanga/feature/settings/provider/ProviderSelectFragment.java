package org.nv95.openmanga.feature.settings.provider;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.nv95.openmanga.R;
import org.nv95.openmanga.feature.settings.provider.adapter.ProvidersAdapter;
import org.nv95.openmanga.components.DividerItemDecoration;
import org.nv95.openmanga.providers.staff.MangaProviderManager;
import org.nv95.openmanga.providers.staff.ProviderSummary;

import java.util.Collections;
import java.util.List;

/**
 * Created by admin on 23.07.17.
 */

public class ProviderSelectFragment extends Fragment implements ProvidersAdapter.OnStartDragListener {

    private RecyclerView mRecyclerView;
    private List<ProviderSummary> mProviders;
    private MangaProviderManager mProviderManager;
    private ProvidersAdapter mAdapter;
    private ItemTouchHelper mItemTouchHelper;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View contentView = inflater.inflate(R.layout.fragment_provselect, container, false);
        mRecyclerView = contentView.findViewById(R.id.recyclerView);
        return contentView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Activity activity = getActivity();
        mProviderManager = new MangaProviderManager(activity);
        mProviders = mProviderManager.getOrderedProviders();

        mRecyclerView.setLayoutManager(new LinearLayoutManager(activity));
        mRecyclerView.setAdapter(mAdapter = new ProvidersAdapter(activity, mProviders, this));
        mAdapter.setActiveCount(mProviderManager.getProvidersCount());
        mRecyclerView.addItemDecoration(new DividerItemDecoration(activity));

        mItemTouchHelper = new ItemTouchHelper(new OrderManager());
        mItemTouchHelper.attachToRecyclerView(mRecyclerView);
    }

    @Override
    public void onStartDrag(RecyclerView.ViewHolder viewHolder) {
        mItemTouchHelper.startDrag(viewHolder);
    }

    private class OrderManager extends ItemTouchHelper.Callback {

        @Override
        public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
            return makeMovementFlags(ItemTouchHelper.UP | ItemTouchHelper.DOWN, 0);
        }

        @Override
        public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
            int fromPosition = viewHolder.getAdapterPosition();
            int toPosition = target.getAdapterPosition();

            if (toPosition == mProviders.size() + 1 || toPosition == mAdapter.getActiveCount()) { //drop to footer/divider
                return false;
            }

            if (viewHolder instanceof ProvidersAdapter.DividerHolder) { //divider
                if (toPosition == 0) {
                    return false; //enabled providers count must be > 0
                }
                mAdapter.setActiveCount(toPosition);
                mAdapter.notifyItemMoved(fromPosition, toPosition);
                mProviderManager.setProvidersCount(toPosition);
                return true;
            }

            if (fromPosition > mAdapter.getActiveCount() && toPosition < mAdapter.getActiveCount()) {
                return false;
            }

            if (fromPosition < mAdapter.getActiveCount() && toPosition > mAdapter.getActiveCount()) {
                return false;
            }

            if (fromPosition > mAdapter.getActiveCount()) {
                fromPosition--;
            }

            if (toPosition > mAdapter.getActiveCount()) {
                toPosition--;
            }

            if (fromPosition < toPosition) {
                for (int i = fromPosition; i < toPosition; i++) {
                    Collections.swap(mProviders, i, i + 1);
                }
            } else {
                for (int i = fromPosition; i > toPosition; i--) {
                    Collections.swap(mProviders, i, i - 1);
                }
            }

            mAdapter.notifyItemMoved(viewHolder.getAdapterPosition(), target.getAdapterPosition());
            mProviderManager.updateOrder(mProviders);
            return true;
        }

        @Override
        public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {

        }

        @Override
        public boolean isLongPressDragEnabled() {
            return false;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Activity activity = getActivity();
        if (activity != null) {
            activity.setTitle(R.string.manga_catalogues);
        }
    }
}
