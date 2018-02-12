package org.nv95.openmanga.filepicker;

import android.Manifest;
import android.app.LoaderManager;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;

import org.nv95.openmanga.AppBaseActivity;
import org.nv95.openmanga.R;
import org.nv95.openmanga.common.WeakAsyncTask;
import org.nv95.openmanga.common.utils.ResourceUtils;
import org.nv95.openmanga.core.ListWrapper;
import org.nv95.openmanga.core.models.FileDesc;
import org.nv95.openmanga.core.models.MangaHeader;
import org.nv95.openmanga.core.providers.ZipArchiveProvider;
import org.nv95.openmanga.preview.PreviewActivity;

import java.io.File;
import java.util.ArrayList;

public final class FilePickerActivity extends AppBaseActivity implements LoaderManager.LoaderCallbacks<ListWrapper<FileDesc>>,
		OnFileSelectListener {

	private static final int REQUEST_CODE_PERMISSION = 14;

	private RecyclerView mRecyclerView;
	private final ArrayList<FileDesc> mDataset = new ArrayList<>();
	private FilePickerAdapter mAdapter;
	private File mRoot;

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_filepicker);
		setSupportActionBar(R.id.toolbar);
		enableHomeAsClose();

		mRecyclerView = findViewById(R.id.recyclerView);
		mRecyclerView.setHasFixedSize(true);
		mRecyclerView.setLayoutManager(ResourceUtils.isLandscape(getResources()) ?
				new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
				: new LinearLayoutManager(this));
		mAdapter = new FilePickerAdapter(mDataset, this);
		mRecyclerView.setAdapter(mAdapter);

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
			checkPermissions(REQUEST_CODE_PERMISSION, Manifest.permission.READ_EXTERNAL_STORAGE);
		} else {
			onPermissionGranted(REQUEST_CODE_PERMISSION, null);
		}
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		mRecyclerView.setLayoutManager(newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE ?
				new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
				: new LinearLayoutManager(this));
	}

	@NonNull
	@Override
	public Loader<ListWrapper<FileDesc>> onCreateLoader(int id, Bundle args) {
		return new FileListLoader(this, args.getString("path"));
	}

	@Override
	public void onLoadFinished(Loader<ListWrapper<FileDesc>> loader, ListWrapper<FileDesc> data) {
		if (data.isSuccess()) {
			mDataset.clear();
			mDataset.addAll(data.get());
			mAdapter.notifyDataSetChanged();
		}
 	}

	@Override
	public void onLoaderReset(Loader<ListWrapper<FileDesc>> loader) {

	}

	@Override
	protected void onPermissionGranted(int requestCode, String permission) {
		mRoot = Environment.getExternalStorageDirectory();
		final Bundle args = new Bundle(1);
		args.putString("path", mRoot.getAbsolutePath());
		getLoaderManager().initLoader(0, args, this).forceLoad();
	}

	@Override
	public void onFileSelected(@NonNull File file) {
		if (file.isDirectory()) {
			mRoot = file;
			final Bundle args = new Bundle(1);
			args.putString("path", file.getAbsolutePath());
			getLoaderManager().restartLoader(0, args, this).forceLoad();
		} else if (file.isFile()) {
			new MangaOpenTask(this)
					.start(Uri.fromFile(file));
		}
	}

	@Override
	public void onBackPressed() {
		mRoot = mRoot.getParentFile();
		if (mRoot != null && mRoot.exists() && mRoot.canRead()) {
			onFileSelected(mRoot);
		} else {
			super.onBackPressed();
		}
	}

	private static class MangaOpenTask extends WeakAsyncTask<FilePickerActivity, Uri, Void, MangaHeader>
			implements DialogInterface.OnCancelListener {

		private final ProgressDialog mProgressDialog;

		MangaOpenTask(FilePickerActivity filePickerActivity) {
			super(filePickerActivity);
			mProgressDialog = new ProgressDialog(filePickerActivity);
			mProgressDialog.setCancelable(true);
			mProgressDialog.setOnCancelListener(this);
			mProgressDialog.setMessage(filePickerActivity.getString(R.string.loading));
		}

		@Override
		protected void onPreExecute(@NonNull FilePickerActivity filePickerActivity) {
			mProgressDialog.show();
		}

		@Override
		protected MangaHeader doInBackground(Uri... uris) {
			try {
				return ZipArchiveProvider.getManga(getObject(), uris[0]);
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		}

		@Override
		protected void onPostExecute(@NonNull FilePickerActivity filePickerActivity, MangaHeader manga) {
			mProgressDialog.dismiss();
			if (manga == null) {
				new AlertDialog.Builder(filePickerActivity)
						.setMessage(R.string.invalid_file_not_supported)
						.setNegativeButton(R.string.close, null)
						.create()
						.show();
			} else {
				filePickerActivity.startActivity(new Intent(filePickerActivity, PreviewActivity.class)
						.putExtra("manga", manga));
			}
		}

		@Override
		public void onCancel(DialogInterface dialog) {
			if (canCancel()) {
				cancel(false);
			}
		}
	}
}
