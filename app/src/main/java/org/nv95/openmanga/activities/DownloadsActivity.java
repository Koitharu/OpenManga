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

import org.nv95.openmanga.R;
import org.nv95.openmanga.adapters.DownloadsAdapter;
import org.nv95.openmanga.services.DownloadService;

/**
 * Created by nv95 on 03.01.16.
 */
public class DownloadsActivity extends BaseAppActivity {
    private DownloadsAdapter adapter;
    private TextView textViewHolder;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_downloads);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        enableHomeAsUp();
        final RecyclerView mRecyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        textViewHolder = (TextView) findViewById(R.id.textView_holder);
        adapter = new DownloadsAdapter(mRecyclerView);
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
        adapter.enable();
    }

    @Override
    protected void onStop() {
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
}
