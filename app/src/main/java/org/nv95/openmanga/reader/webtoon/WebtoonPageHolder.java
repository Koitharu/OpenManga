package org.nv95.openmanga.reader.webtoon;

import android.net.Uri;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewStub;
import android.widget.TextView;

import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;

import org.nv95.openmanga.R;
import org.nv95.openmanga.common.DataViewHolder;
import org.nv95.openmanga.common.utils.ErrorUtils;
import org.nv95.openmanga.common.views.TextProgressView;
import org.nv95.openmanga.core.models.MangaPage;
import org.nv95.openmanga.reader.decoder.ImageConverter;
import org.nv95.openmanga.reader.loader.PageDownloader;
import org.nv95.openmanga.reader.loader.PageLoadCallback;
import org.nv95.openmanga.reader.loader.PagesCache;

import java.io.File;

final class WebtoonPageHolder extends DataViewHolder<MangaPage> implements View.OnClickListener,
		ImageConverter.Callback, PageLoadCallback {

	private File mFile;

	private final WebtoonImageView mWebtoonImageView;
	private final TextProgressView mTextProgressView;
	@Nullable
	private ViewStub mStubError;
	@Nullable
	private View mErrorView = null;

	public WebtoonPageHolder(View itemView) {
		super(itemView);
		mWebtoonImageView = itemView.findViewById(R.id.webtoonImageView);
		mTextProgressView = itemView.findViewById(R.id.progressView);
		mStubError = itemView.findViewById(R.id.stubError);
		mWebtoonImageView.setDoubleTapZoomStyle(SubsamplingScaleImageView.ZOOM_FOCUS_FIXED);
		mWebtoonImageView.setPanLimit(SubsamplingScaleImageView.PAN_LIMIT_INSIDE);
		mWebtoonImageView.setMinimumScaleType(SubsamplingScaleImageView.SCALE_TYPE_CUSTOM);
		mWebtoonImageView.setMinimumDpi(90);
		mWebtoonImageView.setMinimumTileDpi(180);
		mWebtoonImageView.setOnImageEventListener(new SubsamplingScaleImageView.DefaultOnImageEventListener() {

			@Override
			public void onReady() {
				onLoadingComplete();
			}

			@Override
			public void onImageLoadError(Exception e) {
				onImageDisplayFailed(e);
			}
		});
	}

	@Override
	public void bind(MangaPage page) {
		super.bind(page);
		mTextProgressView.setProgress(TextProgressView.INDETERMINATE);
		mTextProgressView.setVisibility(View.VISIBLE);
		setError(null);
		if (page.url.startsWith("file://")) {
			mWebtoonImageView.setImage(ImageSource.uri(page.url));
		} else {
			mFile = PagesCache.getInstance(getContext()).getFileForUrl(page.url);
			if (mFile.exists()) {
				mWebtoonImageView.setImage(ImageSource.uri(Uri.fromFile(mFile)));
			} else {
				PageDownloader.getInstance().downloadPage(getContext(), page, mFile, this);
			}
		}
	}

	private void setError(@Nullable CharSequence errorMessage) {
		if (errorMessage == null) {
			if (mErrorView != null) {
				mErrorView.setVisibility(View.GONE);
			}
			return;
		}
		mTextProgressView.setVisibility(View.GONE);
		if (mErrorView == null) {
			assert mStubError != null;
			mErrorView = mStubError.inflate();
			mErrorView.findViewById(R.id.button_retry).setOnClickListener(this);
			mStubError = null;
		}
		mErrorView.setVisibility(View.VISIBLE);
		((TextView)mErrorView.findViewById(R.id.textView_error)).setText(errorMessage);
	}

	@Override
	public void onClick(View v) {
		final MangaPage page = getData();
		if (page == null) {
			return;
		}
		switch (v.getId()) {
			case R.id.button_retry:
				setError(null);
				mTextProgressView.setVisibility(View.VISIBLE);
				mFile.delete();
				PageDownloader.getInstance().downloadPage(v.getContext(), page, mFile, this);
				break;
		}
	}

	public void onLoadingComplete() {
		setError(null);
		mTextProgressView.setVisibility(View.GONE);
	}

	public void onImageDisplayFailed(Exception e) {
		if (mFile.exists()) {
			ImageConverter.getInstance().convert(mFile.getPath(), this);
		} else {
			mTextProgressView.setVisibility(View.GONE);
			setError(ErrorUtils.getErrorMessageDetailed(itemView.getContext(), e));
		}
	}

	@Override
	public void onImageConverted() {
		mWebtoonImageView.setImage(ImageSource.uri(Uri.fromFile(mFile)));
	}

	@Override
	public void onImageConvertFailed() {
		setError(itemView.getContext().getString(R.string.image_decode_error));
	}

	@Override
	public void onPageDownloaded() {
		mWebtoonImageView.setImage(ImageSource.uri(Uri.fromFile(mFile)));
	}

	@Override
	public void onPageDownloadFailed(Throwable reason) {
		setError(ErrorUtils.getErrorMessageDetailed(getContext(), reason));
	}

	@Override
	public void onPageDownloadProgress(int progress, int max) {
		mTextProgressView.setProgress(progress, max);
	}

	public void recycle() {
		final MangaPage page = getData();
		super.recycle();
		if (page != null) {
			PageDownloader.getInstance().cancel(page);
		}
		setError(null);
		mWebtoonImageView.recycle();
	}
}
