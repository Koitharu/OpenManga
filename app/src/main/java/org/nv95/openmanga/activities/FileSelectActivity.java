package org.nv95.openmanga.activities;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import org.nv95.openmanga.R;
import org.nv95.openmanga.adapters.FileSelectAdapter;
import org.nv95.openmanga.dialogs.DirSelectDialog;

import java.io.File;

/**
 * Created by nv95 on 09.02.16.
 */
public class FileSelectActivity extends BaseAppActivity implements DirSelectDialog.OnDirSelectListener {

    public static final String EXTRA_INITIAL_DIR = "initial_dir";
    public static final String EXTRA_FILTER = "filter";

    private FileSelectAdapter mAdapter;
    private RecyclerView mRecyclerView;
    private File mDir;
    private String mFilter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_importfile);
        setSupportActionBar(R.id.toolbar);
        enableHomeAsClose();
        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        String dir = getIntent().getStringExtra(EXTRA_INITIAL_DIR);
        if (dir == null) {
            dir = getSharedPreferences(this.getLocalClassName(), MODE_PRIVATE)
                    .getString("dir", null);
        }
        mDir = dir == null ? Environment.getExternalStorageDirectory()
                : new File(dir);
        mFilter = getIntent().getStringExtra(EXTRA_FILTER);
        if (checkPermission(Manifest.permission.READ_EXTERNAL_STORAGE)) {
           onPermissionGranted(null);
        }
    }

    @Override
    protected void onPermissionGranted(String permission) {
        mAdapter = new FileSelectAdapter(mDir, mFilter, this);
        mRecyclerView.setAdapter(mAdapter);
        setSubtitle(mAdapter.getCurrentDir().getPath());
    }

    @Override
    public void onDirSelected(File dir) {
        if (dir.isDirectory()) {
            mAdapter.setDirectory(dir);
            setSubtitle(mAdapter.getCurrentDir().getPath());
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
        } else {
            setSubtitle(mAdapter.getCurrentDir().getPath());
        }
    }
}
