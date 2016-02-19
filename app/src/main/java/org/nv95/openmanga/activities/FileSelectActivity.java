package org.nv95.openmanga.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;

import org.nv95.openmanga.DirSelectDialog;
import org.nv95.openmanga.R;
import org.nv95.openmanga.adapters.FileSelectAdapter;

import java.io.File;

/**
 * Created by nv95 on 09.02.16.
 */
public class FileSelectActivity extends BaseAppActivity implements DirSelectDialog.OnDirSelectListener {
    private FileSelectAdapter mAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_importfile);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        String dir = getSharedPreferences(this.getLocalClassName(), MODE_PRIVATE)
                .getString("dir", null);
        mAdapter = new FileSelectAdapter(dir == null ? Environment.getExternalStorageDirectory()
                : new File(dir), ".cbz", this);
        recyclerView.setAdapter(mAdapter);
        enableHomeAsUp();
        setSubtitle(mAdapter.getCurrentDir().getPath());
    }

    @Override
    public void onDirSelected(File dir) {
        if (dir.isDirectory()) {
            mAdapter.setDirectory(dir);
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.setSubtitle(mAdapter.getCurrentDir().getPath());
            }
        } else {
            getSharedPreferences(this.getLocalClassName(), MODE_PRIVATE).edit()
                    .putString("dir", mAdapter.getCurrentDir().getPath())
                    .apply();
            Intent data = new Intent();
            data.putExtra(Intent.EXTRA_TEXT, dir.getPath());
            setResult(RESULT_OK, data);
            finish();
        }
    }

    @Override
    public void onBackPressed() {
        if (!mAdapter.toParentDir()) {
            super.onBackPressed();
        }
    }
}
