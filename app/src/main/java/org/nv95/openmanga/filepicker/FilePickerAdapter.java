package org.nv95.openmanga.filepicker;

import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.nv95.openmanga.R;
import org.nv95.openmanga.common.DataViewHolder;
import org.nv95.openmanga.common.WeakAsyncTask;
import org.nv95.openmanga.common.utils.MetricsUtils;
import org.nv95.openmanga.core.models.FileDesc;
import org.nv95.openmanga.core.providers.ZipArchiveProvider;

import java.io.File;
import java.util.ArrayList;

final class FilePickerAdapter extends RecyclerView.Adapter<FilePickerAdapter.FileViewHolder> {

	private final ArrayList<FileDesc> mDataset;
	private final OnFileSelectListener mListener;

	FilePickerAdapter(ArrayList<FileDesc> dataset, OnFileSelectListener listener) {
		mDataset = dataset;
		mListener = listener;
	}

	@Override
	public FileViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		return new FileViewHolder(LayoutInflater.from(parent.getContext())
				.inflate(R.layout.item_two_lines_icon, parent, false));
	}

	@Override
	public void onBindViewHolder(FileViewHolder holder, int position) {
		holder.bind(mDataset.get(position));
	}

	@Override
	public int getItemCount() {
		return mDataset.size();
	}

	class FileViewHolder extends DataViewHolder<FileDesc> implements View.OnClickListener {

		private final ImageView mIcon;
		private final TextView mText1;
		private final TextView mText2;

		FileViewHolder(View itemView) {
			super(itemView);
			mIcon = itemView.findViewById(android.R.id.icon);
			mText1 = itemView.findViewById(android.R.id.text1);
			mText2 = itemView.findViewById(android.R.id.text2);
			itemView.setOnClickListener(this);
		}

		@Override
		public void bind(FileDesc desc) {
			super.bind(desc);
			if (desc.file.isDirectory()) {
				mIcon.setImageResource(R.drawable.ic_folder_white);
				mText2.setText(itemView.getResources().getQuantityString(
						R.plurals.files_count,
						desc.entryCount,
						desc.entryCount
				));
				mText2.setVisibility(View.VISIBLE);
			} else {
				if (ZipArchiveProvider.isFileSupported(desc.file)) {
					new ThumbnailTask(this, desc.file.getAbsolutePath().hashCode())
							.start(desc.file);
				} else {
					mIcon.setImageResource(R.drawable.ic_file_white);
				}
				mText2.setVisibility(View.GONE);
			}
			mText1.setText(desc.file.getName());
		}

		@Override
		public void onClick(View v) {
			final FileDesc desc = getData();
			if (desc != null) {
				mListener.onFileSelected(desc.file);
			}
		}
	}

	private static class ThumbnailTask extends WeakAsyncTask<FileViewHolder, File, Void, Drawable> {

		private final Integer mId;
		private final MetricsUtils.Size mSize;

		public ThumbnailTask(FileViewHolder fileViewHolder, int id) {
			super(fileViewHolder);
			fileViewHolder.mIcon.setImageResource(R.drawable.ic_file_dark);
			mId = id;
			fileViewHolder.mIcon.setTag(mId);
			mSize = MetricsUtils.Size.from(fileViewHolder.mIcon);
		}

		@Nullable
		@Override
		protected Drawable doInBackground(File... files) {
			try {
				final File thumb = ZipArchiveProvider.createThumbnail(
						getObject().itemView.getContext(),
						Uri.fromFile(files[0]),
						mSize
				);
				if (thumb == null) {
					return null;
				} else {
					RoundedBitmapDrawable drawable = RoundedBitmapDrawableFactory.create(
							getObject().itemView.getResources(),
							thumb.getPath()
					);
					drawable.setCornerRadius(mSize.width / 2.f);
					return drawable;
				}
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		}

		@Override
		protected void onPostExecute(@NonNull FileViewHolder fileViewHolder, @Nullable Drawable drawable) {
			if (mId.equals(fileViewHolder.mIcon.getTag()) && drawable != null) {
				fileViewHolder.mIcon.setImageDrawable(drawable);
			}
		}
	}
}
