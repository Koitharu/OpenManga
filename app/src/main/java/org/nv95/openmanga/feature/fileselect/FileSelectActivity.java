package org.nv95.openmanga.feature.fileselect;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import org.nv95.openmanga.R;
import org.nv95.openmanga.core.activities.BaseAppActivity;
import org.nv95.openmanga.feature.fileselect.adapter.FileSelectAdapter;
import org.nv95.openmanga.feature.settings.main.dialog.DirSelectDialog;
import org.nv95.openmanga.feature.settings.main.dialog.StorageSelectDialog;

import java.io.File;

/**
 * Created by nv95 on 09.02.16.
 */
public class FileSelectActivity extends BaseAppActivity implements DirSelectDialog.OnDirSelectListener, View.OnClickListener {

    public static final String EXTRA_INITIAL_DIR = "initial_dir";
    public static final String EXTRA_FILTER = "filter";

    @Nullable
    private FileSelectAdapter mAdapter;
    private RecyclerView mRecyclerView;
    private File mDir;
    private String mFilter;
    private TextView mTextViewTitle;

    private final RecyclerView.AdapterDataObserver mObserver = new RecyclerView.AdapterDataObserver() {
        @Override
        public void onChanged() {
            super.onChanged();
            File file = mAdapter.getCurrentDir();
            mTextViewTitle.setText(file.getPath());
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_importfile);
        setSupportActionBar(R.id.toolbar);
        enableHomeAsUp();
        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mTextViewTitle = (TextView) findViewById(R.id.textView_title);
        findViewById(R.id.imageButton).setOnClickListener(this);
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
        mAdapter.registerAdapterDataObserver(mObserver);
        mObserver.onChanged();
        mRecyclerView.setAdapter(mAdapter);
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public void onDirSelected(File dir) {
        if (dir.isDirectory()) {
            mAdapter.setDirectory(dir);
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
    public void onClick(View view) {
        if (mAdapter == null) {
            return;
        }
        new StorageSelectDialog(this, true)
                .setDirSelectListener(new DirSelectDialog.OnDirSelectListener() {
                    @Override
                    public void onDirSelected(File dir) {
                        mAdapter.setCurrentDir(dir);
                    }
                })
                .show();
    }
}
