package org.nv95.openmanga.activities;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.nv95.openmanga.MangaListLoader;
import org.nv95.openmanga.R;
import org.nv95.openmanga.dialogs.NavigationListener;
import org.nv95.openmanga.dialogs.PageNumberDialog;
import org.nv95.openmanga.helpers.ListModeHelper;
import org.nv95.openmanga.items.ThumbSize;
import org.nv95.openmanga.lists.MangaList;
import org.nv95.openmanga.providers.MangaProvider;
import org.nv95.openmanga.providers.staff.MangaProviderManager;
import org.nv95.openmanga.utils.InternalLinkMovement;
import org.nv95.openmanga.utils.LayoutUtils;

/**
 * Created by nv95 on 01.10.15.
 */
public class SingleSearchActivity extends BaseAppActivity implements
        View.OnClickListener, MangaListLoader.OnContentLoadListener,
        ListModeHelper.OnListModeListener, InternalLinkMovement.OnLinkClickListener, NavigationListener, TextView.OnEditorActionListener {

    //views
    private RecyclerView mRecyclerView;
    private EditText mEditTextQuery;
    private ProgressBar mProgressBar;
    private TextView mTextViewHolder;
    //utils
    private MangaListLoader mLoader;
    private MangaProvider mProvider;
    private ListModeHelper mListModeHelper;
    //data
    private String mQuery;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fastsearch);
        setContentView(R.layout.activity_fastsearch);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setupToolbarScrolling(toolbar);
        disableTitle();
        enableHomeAsUp();
        mEditTextQuery = (EditText) findViewById(R.id.editTextQuery);
        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);
        mTextViewHolder = (TextView) findViewById(R.id.textView_holder);
        mTextViewHolder.setMovementMethod(new InternalLinkMovement(this));
        Bundle extras = getIntent().getExtras();
        mQuery = extras.getString("query");
        int provider = extras.getInt("provider");
        mProvider = new MangaProviderManager(this).getProviderById(provider);
        if (mProvider == null) {
            finish();
            return;
        }
        mEditTextQuery.setOnEditorActionListener(this);
        mRecyclerView.setLayoutManager(new GridLayoutManager(this, 1));
        mLoader = new MangaListLoader(mRecyclerView, this);
        mListModeHelper = new ListModeHelper(this, this);
        mListModeHelper.applyCurrent();
        mListModeHelper.enable();
        updateContent();
        if (TextUtils.isEmpty(mQuery)) {
            LayoutUtils.showSoftKeyboard(mEditTextQuery);
        } else {
            mEditTextQuery.setText(mQuery);
            LayoutUtils.hideSoftKeyboard(mEditTextQuery);
            mRecyclerView.requestFocus();
            updateContent();
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.action_goto).setVisible(mProvider.isMultiPage());
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_search, menu);
        menu.findItem(R.id.action_search).setVisible(mQuery == null);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        int viewMode = PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
                .getInt("view_mode", 0);
        onListModeChanged(viewMode != 0, viewMode - 1);
    }

    @Override
    protected void onDestroy() {
        mListModeHelper.disable();
        mLoader.cancelLoading();
        super.onDestroy();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_listmode:
                mListModeHelper.showDialog();
                return true;
            case R.id.action_goto:
                new PageNumberDialog(this)
                        .setNavigationListener(this)
                        .show(mLoader.getCurrentPage());
                return true;
            case R.id.action_search:
                onClick(null);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onClick(View v) {
        startActivity(new Intent(SingleSearchActivity.this, FastSearchActivity.class)
                .putExtra("query", mQuery));
    }

    @Override
    public void onContentLoaded(boolean success) {
        mProgressBar.setVisibility(View.GONE);
        if (mLoader.getContentSize() == 0) {
            mTextViewHolder.setVisibility(View.VISIBLE);
            Snackbar.make(mRecyclerView, R.string.no_manga_found,Snackbar.LENGTH_INDEFINITE)
                    .setAction(R.string.more, this).show();
        }
    }

    @Override
    public void onLoadingStarts(boolean hasItems) {
        if (!hasItems) {
            mProgressBar.setVisibility(View.VISIBLE);
        }
        mTextViewHolder.setVisibility(View.GONE);
    }

    @Nullable
    @Override
    public MangaList onContentNeeded(int page) {
        try {
            return mProvider.search(mQuery, page);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void onListModeChanged(boolean grid, int sizeMode) {
        int spans;
        ThumbSize thumbSize;
        switch (sizeMode) {
            case -1:
                spans = LayoutUtils.isTabletLandscape(this) ? 2 : 1;
                thumbSize = ThumbSize.THUMB_SIZE_LIST;
                break;
            case 0:
                spans = LayoutUtils.getOptimalColumnsCount(getResources(), thumbSize = ThumbSize.THUMB_SIZE_SMALL);
                break;
            case 1:
                spans = LayoutUtils.getOptimalColumnsCount(getResources(), thumbSize = ThumbSize.THUMB_SIZE_MEDIUM);
                break;
            case 2:
                spans = LayoutUtils.getOptimalColumnsCount(getResources(), thumbSize = ThumbSize.THUMB_SIZE_LARGE);
                break;
            default:
                return;
        }
        mLoader.updateLayout(grid, spans, thumbSize);
    }

    @Override
    public void onLinkClicked(TextView view, String scheme, String url) {
        switch (url) {
            case "update":
                updateContent();
                break;
        }
    }

    private void updateContent() {
        if (checkConnection()) {
            mLoader.loadContent(mProvider.isMultiPage(), true);
        } else {
            mLoader.clearItemsLazy();
            mTextViewHolder.setText(Html.fromHtml(getString(R.string.no_network_connection_html)));
            mTextViewHolder.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onPageChange(int page) {
        mLoader.loadFromPage(page - 1);
    }

    @Override
    public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
        if (actionId == EditorInfo.IME_ACTION_SEARCH) {
            mQuery = textView.getText().toString();
            LayoutUtils.hideSoftKeyboard(textView);
            mRecyclerView.requestFocus();
            updateContent();
            return true;
        } else {
            return false;
        }
    }
}
