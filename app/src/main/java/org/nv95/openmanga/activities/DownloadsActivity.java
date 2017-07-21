package org.nv95.openmanga.activities;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import org.nv95.openmanga.R;
import org.nv95.openmanga.adapters.DownloadsAdapter;
import org.nv95.openmanga.helpers.MangaSaveHelper;

/**
 * Created by nv95 on 03.01.16.
 */
public class DownloadsActivity extends BaseAppActivity {

    private DownloadsAdapter mAdapter;
    private TextView mTextViewHolder;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_downloads);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        enableHomeAsClose();
        RecyclerView mRecyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        assert mRecyclerView != null;
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mTextViewHolder = (TextView) findViewById(R.id.textView_holder);
        mAdapter = new DownloadsAdapter(mRecyclerView);
        mAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                mTextViewHolder.setVisibility(mAdapter.getItemCount() == 0 ? View.VISIBLE : View.GONE);
            }
        });
        mRecyclerView.setAdapter(mAdapter);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mAdapter.enable();
    }

    @Override
    protected void onStop() {
        mAdapter.disable();
        super.onStop();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mAdapter.notifyDataSetChanged();
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
                new AlertDialog.Builder(this)
                        .setNegativeButton(android.R.string.no, null)
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                new MangaSaveHelper(DownloadsActivity.this).cancelAll();
                            }
                        })
                        .setMessage(R.string.downloads_cancel_confirm)
                        .create().show();
                return true;
            case R.id.action_resume:
                mAdapter.setTaskPaused(false);
                invalidateOptionsMenu();
                return true;
            case R.id.action_pause:
                mAdapter.setTaskPaused(true);
                invalidateOptionsMenu();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
