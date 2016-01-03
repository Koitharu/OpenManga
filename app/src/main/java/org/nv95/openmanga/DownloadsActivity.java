package org.nv95.openmanga;

import android.database.DataSetObserver;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import org.nv95.openmanga.adapters.DownloadsAdapter;
import org.nv95.openmanga.utils.MangaChangesObserver;

/**
 * Created by nv95 on 03.01.16.
 */
public class DownloadsActivity extends AppCompatActivity implements MangaChangesObserver.OnMangaChangesListener {
    private DownloadsAdapter adapter;
    private ListView listView;
    private TextView textViewHolder;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_downloads);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        listView = (ListView) findViewById(R.id.listView);
        textViewHolder = (TextView) findViewById(R.id.textView_holder);
        adapter = new DownloadsAdapter(this);
        adapter.registerDataSetObserver(new DataSetObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                textViewHolder.setVisibility(adapter.getCount() == 0 ? View.VISIBLE : View.GONE);
            }
        });
        listView.setAdapter(adapter);
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
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onMangaChanged(int category) {
        if (category == MangaChangesObserver.CATEGORY_LOCAL) {
            adapter.notifyDataSetChanged();
        }
    }
}
