package org.nv95.openmanga.activities;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;

import org.nv95.openmanga.R;
import org.nv95.openmanga.adapters.SearchHistoryAdapter;
import org.nv95.openmanga.components.SearchLayout;
import org.nv95.openmanga.fragments.MultipleSearchFragment;
import org.nv95.openmanga.fragments.SearchFragmentAbs;
import org.nv95.openmanga.utils.AnimUtils;
import org.nv95.openmanga.utils.LayoutUtils;

/**
 * Created by nv95 on 15.12.16.
 */

public class FastSearchActivity extends BaseAppActivity implements TextView.OnEditorActionListener,
        View.OnFocusChangeListener, FragmentManager.OnBackStackChangedListener,
        SearchHistoryAdapter.OnHistoryEventListener, TextWatcher {

    private String mQuery;
    private EditText mEditTextQuery;
    private SearchLayout mSearchLayout;
    private FrameLayout mFrameContent;
    private FrameLayout mFrameSearch;
    private RecyclerView mRecyclerViewSearch;
    private TextView mTextViewHolder;
    private SearchHistoryAdapter mHistoryAdapter;
    @Nullable
    private SearchFragmentAbs mFragment;

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
        if (mFragment != null) {
            mFragment.search(query);
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

    public void changeFragment(SearchFragmentAbs newFragment, @Nullable Bundle extras, boolean backStack) {
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
        mFragment = (SearchFragmentAbs) getFragmentManager().findFragmentById(R.id.content);
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
