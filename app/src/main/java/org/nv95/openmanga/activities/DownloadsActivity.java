package org.nv95.openmanga.activities;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import org.nv95.openmanga.Constants;
import org.nv95.openmanga.R;
import org.nv95.openmanga.adapters.DownloadsAdapter;
import org.nv95.openmanga.items.MangaInfo;
import org.nv95.openmanga.services.DownloadService;
import org.nv95.openmanga.utils.MangaChangesObserver;

/**
 * Created by nv95 on 03.01.16.
 */
public class DownloadsActivity extends BaseAppActivity implements MangaChangesObserver.OnMangaChangesListener {
    private DownloadsAdapter adapter;
    private RecyclerView mRecyclerView;
    private TextView textViewHolder;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_downloads);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        enableHomeAsUp();
        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        textViewHolder = (TextView) findViewById(R.id.textView_holder);
        adapter = new DownloadsAdapter(this);
        adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                textViewHolder.setVisibility(adapter.getItemCount() == 0 ? View.VISIBLE : View.GONE);
            }
        });
        mRecyclerView.setAdapter(adapter);
    }

    @Override
    protected void onStart() {
        super.onStart();
        MangaChangesObserver.addListener(this);
        adapter.enable();
    }

    @Override
    protected void onStop() {
        MangaChangesObserver.removeListener(this);
        adapter.disable();
        super.onStop();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.downloads, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_cancel:
                DownloadService.cancel(this);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onMangaChanged(int category) {
        if (category == Constants.CATEGORY_LOCAL) {
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onMangaAdded(int category, MangaInfo data) {

    }
}
