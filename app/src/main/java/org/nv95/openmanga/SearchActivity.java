package org.nv95.openmanga;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.nv95.openmanga.lists.MangaList;
import org.nv95.openmanga.providers.MangaProvider;
import org.nv95.openmanga.providers.MangaProviderManager;

/**
 * Created by nv95 on 01.10.15.
 */
public class SearchActivity extends AppCompatActivity implements
        DialogInterface.OnClickListener, MangaListLoader.OnContentLoadListener {
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
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mLoader = new MangaListLoader(mRecyclerView, this);
        setViewMode(PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
                .getInt("view_mode", 0));
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
                viewModeDialog();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

  /*
  @Override
  public String onEmptyList(MangaProvider provider) {
    if (title == null) {
      new AlertDialog.Builder(this)
              .setMessage(R.string.search_more_confirm)
              .setPositiveButton(android.R.string.ok, this)
              .setCancelable(true)
              .setNegativeButton(android.R.string.no, null)
              .create().show();
    }
    return getString(R.string.no_manga_found);
  }*/

    @Override
    public void onClick(DialogInterface dialog, int which) {
        startActivity(new Intent(SearchActivity.this, MultipleSearchActivity.class)
                .putExtra("query", query));
    }

    @Override
    public void onContentLoaded(boolean success) {
        mProgressBar.setVisibility(View.GONE);
        if (mLoader.getContentSize() == 0) {
            mTextViewHolder.setVisibility(View.VISIBLE);
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

    private void viewModeDialog() {
        int mode = PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
                .getInt("view_mode", 0);
        new AlertDialog.Builder(this)
                .setSingleChoiceItems(R.array.view_modes, mode, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        setViewMode(which);
                        PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
                                .edit().putInt("view_mode", which).apply();
                        dialog.dismiss();
                    }
                })
                .create().show();
    }

    private void setViewMode(int mode) {
        LinearLayoutManager layoutManager;
        switch (mode) {
            case 0:
                layoutManager = new LinearLayoutManager(this);
                break;
            case 1:
                layoutManager = new GridLayoutManager(this, 4);
                break;
            case 2:
                layoutManager = new GridLayoutManager(this, 3);
                break;
            case 3:
                layoutManager = new GridLayoutManager(this, 2);
                break;
            default:
                return;
        }
        mRecyclerView.setLayoutManager(layoutManager);
        mLoader.getAdapter().onLayoutManagerChanged(layoutManager);
    }
}
