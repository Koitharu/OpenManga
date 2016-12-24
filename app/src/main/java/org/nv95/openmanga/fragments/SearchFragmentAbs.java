package org.nv95.openmanga.fragments;

import android.app.Activity;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.nv95.openmanga.R;
import org.nv95.openmanga.helpers.ListModeHelper;

/**
 * Created by nv95 on 23.12.16.
 */

public abstract class SearchFragmentAbs extends BaseAppFragment implements ListModeHelper.OnListModeListener {

    protected String mQuery;
    protected TextView mTextViewHolder;
    protected ProgressBar mProgressBar;
    protected RecyclerView mRecyclerView;
    protected ListModeHelper mListModeHelper;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle extras = savedInstanceState != null ? savedInstanceState : getArguments();
        mQuery = extras.getString("query");
        onInit(extras);
        setHasOptionsMenu(true);
    }

    protected void onInit(Bundle extras) {

    }

    protected void onActivityCreated(Activity activity) {

    }

    protected void onRestoredFromBackStack(Activity activity) {

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_recyclerview, container, false);
        mRecyclerView = (RecyclerView) view.findViewById(R.id.recyclerView);
        mProgressBar = (ProgressBar) view.findViewById(R.id.progressBar);
        mTextViewHolder = (TextView) view.findViewById(R.id.textView_holder);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Activity activity = getActivity();
        if (activity != null) {
            if (mListModeHelper == null) {
                mListModeHelper = new ListModeHelper(activity, this);
                mRecyclerView.setLayoutManager(new GridLayoutManager(activity, 1));
                onActivityCreated(activity);
                mListModeHelper.applyCurrent();
                mListModeHelper.enable();
                if (!TextUtils.isEmpty(mQuery)) {
                    search(mQuery);
                }
            } else {
                mRecyclerView.setLayoutManager(new GridLayoutManager(activity, 1));
                onRestoredFromBackStack(activity);
                mListModeHelper.applyCurrent();
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return mListModeHelper.onOptionsItemSelected(item) || super.onOptionsItemSelected(item);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        int viewMode = PreferenceManager.getDefaultSharedPreferences(mRecyclerView.getContext())
                .getInt("view_mode", 0);
        onListModeChanged(viewMode != 0, viewMode - 1);
    }


    @Override
    public void onResume() {
        super.onResume();
        mRecyclerView.postDelayed(new Runnable() {
            @Override
            public void run() {
                mRecyclerView.requestFocus();
            }
        }, 250);
    }

    public abstract void search(String query);

    @Override
    public void onDestroy() {
        mListModeHelper.disable();
        super.onDestroy();
    }
}
