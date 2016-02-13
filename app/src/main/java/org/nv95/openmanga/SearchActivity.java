package org.nv95.openmanga;

import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.nv95.openmanga.items.ThumbSize;
import org.nv95.openmanga.lists.MangaList;
import org.nv95.openmanga.providers.MangaProvider;
import org.nv95.openmanga.providers.MangaProviderManager;
import org.nv95.openmanga.utils.LayoutUtils;

/**
 * Created by nv95 on 01.10.15.
 */
public class SearchActivity extends AppCompatActivity implements
        View.OnClickListener, MangaListLoader.OnContentLoadListener,
        ListModeDialog.OnListModeListener {
    //views
    private RecyclerView mRecyclerView;
    private ProgressBar mProgressBar;
    private TextView mTextViewHolder;
    //utils
    private MangaListLoader mLoader;
    private MangaProvider mProvider;
    //data
    private String query;
    @Nullable
    private String title;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);
        mTextViewHolder = (TextView) findViewById(R.id.textView_holder);
        Bundle extras = getIntent().getExtras();
        query = extras.getString("query");
        title = extras.getString("title");
        int provider = extras.getInt("provider");
        mProvider = new MangaProviderManager(this).getMangaProvider(provider);
        if (mProvider == null) {
            finish();
            return;
        }
        if (title != null) {
            setTitle(query);
        }
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
            actionBar.setSubtitle(title == null ? query : title);
        }
        mRecyclerView.setLayoutManager(new GridLayoutManager(this, 1));
        mLoader = new MangaListLoader(mRecyclerView, this);
        int viewMode = PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
                .getInt("view_mode", 0);
        onListModeChanged(viewMode != 0, viewMode - 1);
        mLoader.loadContent(mProvider.hasFeature(MangaProviderManager.FUTURE_MULTIPAGE), true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_search, menu);
        menu.findItem(R.id.action_search).setVisible(title == null);
        return super.onCreateOptionsMenu(menu);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.action_listmode:
                new ListModeDialog(this).show(this);
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
        startActivity(new Intent(SearchActivity.this, MultipleSearchActivity.class)
                .putExtra("query", query));
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
    public void onLoadingStarts(int page) {
        if (page == 0) {
            mProgressBar.setVisibility(View.VISIBLE);
        }
        mTextViewHolder.setVisibility(View.GONE);
    }

    @Nullable
    @Override
    public MangaList onContentNeeded(int page) {
        try {
            return mProvider.search(query, page);
        } catch (Exception e) {
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

}
