package org.nv95.openmanga.activities;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;

import org.nv95.openmanga.R;
import org.nv95.openmanga.adapters.SearchHistoryAdapter;
import org.nv95.openmanga.components.SearchLayout;
import org.nv95.openmanga.fragments.BaseAppFragment;
import org.nv95.openmanga.fragments.MultipleSearchFragment;
import org.nv95.openmanga.fragments.Searchable;
import org.nv95.openmanga.fragments.SingleSearchFragment;
import org.nv95.openmanga.helpers.ListModeHelper;
import org.nv95.openmanga.utils.AnimUtils;
import org.nv95.openmanga.utils.LayoutUtils;

/**
 * Created by nv95 on 15.12.16.
 */

public class FastSearchActivity extends BaseAppActivity implements ListModeHelper.OnListModeListener,
        TextView.OnEditorActionListener, View.OnFocusChangeListener,
        FragmentManager.OnBackStackChangedListener, SearchHistoryAdapter.OnHistoryEventListener, TextWatcher {

    private String mQuery;
    private EditText mEditTextQuery;
    private SearchLayout mSearchLayout;
    private FrameLayout mFrameContent;
    private FrameLayout mFrameSearch;
    private RecyclerView mRecyclerViewSearch;
    private TextView mTextViewHolder;
    private SearchHistoryAdapter mHistoryAdapter;
    private ListModeHelper mListModeHelper;
    @Nullable
    private BaseAppFragment mFragment;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fastsearch);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setupToolbarScrolling(toolbar);
        disableTitle();
        enableHomeAsUp();
        mSearchLayout = (SearchLayout) findViewById(R.id.search);
        mFrameContent = (FrameLayout) findViewById(R.id.content);
        mFrameSearch = (FrameLayout) findViewById(R.id.search_frame);
        mRecyclerViewSearch = (RecyclerView) findViewById(R.id.recyclerView);
        mTextViewHolder = (TextView) findViewById(R.id.textView_holder);
        mEditTextQuery = mSearchLayout.getEditText();
        mQuery = getIntent().getStringExtra("query");
        mEditTextQuery.setText(mQuery);
        mListModeHelper = new ListModeHelper(this, this);
        mListModeHelper.applyCurrent();
        mListModeHelper.enable();
        mFragment = null;
        mHistoryAdapter = new SearchHistoryAdapter(this, this);
        mRecyclerViewSearch.setAdapter(mHistoryAdapter);

        mEditTextQuery.setOnEditorActionListener(this);
        mSearchLayout.setOnEditFocusChangeListener(this);
        mEditTextQuery.addTextChangedListener(this);
        getFragmentManager().addOnBackStackChangedListener(this);

        if (TextUtils.isEmpty(mQuery)) {
            mEditTextQuery.postDelayed(new Runnable() {
                @Override
                public void run() {
                    LayoutUtils.showSoftKeyboard(mEditTextQuery);
                }
            }, 250);
        } else {
            mEditTextQuery.setText(mQuery);
            LayoutUtils.hideSoftKeyboard(mEditTextQuery);
            doSearch(mQuery);
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        int viewMode = PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
                .getInt("view_mode", 0);
        onListModeChanged(viewMode != 0, viewMode - 1);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.search, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.action_goto).setVisible(mFragment instanceof SingleSearchFragment && !TextUtils.isEmpty(mQuery));
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_listmode:
                mListModeHelper.showDialog();
                return true;
            default:
                return (mFragment != null && mFragment.onOptionsItemSelected(item)) || super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onListModeChanged(boolean grid, int sizeMode) {
        if (mFragment instanceof ListModeHelper.OnListModeListener) {
            ((ListModeHelper.OnListModeListener) mFragment).onListModeChanged(grid, sizeMode);
        }
    }

    @Override
    protected void onDestroy() {
        mListModeHelper.disable();
        super.onDestroy();
    }

    @Override
    public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
        if (actionId == EditorInfo.IME_ACTION_SEARCH) {
            String query = textView.getText().toString();
            SearchHistoryAdapter.addToHistory(this, query);
            LayoutUtils.hideSoftKeyboard(textView);
            doSearch(query);
            return true;
        } else {
            return false;
        }
    }

    private void doSearch(String query) {
        mQuery = query;
        if (mFragment != null && mFragment instanceof Searchable) {
            ((Searchable) mFragment).search(query);
        } else {
            changeFragment(new MultipleSearchFragment(), null, false);
        }
    }

    @Override
    public void onBackPressed() {
        FragmentManager fm = getFragmentManager();
        if (fm.getBackStackEntryCount() > 0) {
            fm.popBackStack();
        } else {
            super.onBackPressed();
        }
    }

    public void changeFragment(BaseAppFragment newFragment, @Nullable Bundle extras, boolean backStack) {
        Bundle args = getIntent().getExtras();
        args.putString("query", mQuery);
        if (extras != null) {
            args.putAll(extras);
        }
        newFragment.setArguments(args);
        mFragment = newFragment;
        FragmentTransaction tran = getFragmentManager().beginTransaction()
                .replace(R.id.content, newFragment);
        if (backStack) {
            tran.addToBackStack(null);
        }
        tran.commit();
        invalidateOptionsMenu();
    }

    @Override
    public void onFocusChange(View view, boolean b) {
        if (b) {
            mTextViewHolder.setVisibility(View.GONE);
            AnimUtils.crossfade(mFrameContent, mFrameSearch);
            mHistoryAdapter.requery(null);
            if (mHistoryAdapter.getItemCount() == 0) {
                mTextViewHolder.setVisibility(View.VISIBLE);
            }
        } else {
            AnimUtils.crossfade(mFrameSearch, mFrameContent);
        }
    }

    @Override
    public void onBackStackChanged() {
        mFragment = (BaseAppFragment) getFragmentManager().findFragmentById(R.id.content);
    }

    @Override
    public void onHistoryItemClick(String text, boolean apply) {
        mEditTextQuery.setText(text);
        if (apply) {
            LayoutUtils.hideSoftKeyboard(mEditTextQuery);
            doSearch(text);
        } else {
            mEditTextQuery.setSelection(text.length());
        }
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        mHistoryAdapter.requery(charSequence.toString());
    }

    @Override
    public void afterTextChanged(Editable editable) {

    }
}
