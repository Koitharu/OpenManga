package org.nv95.openmanga.reader.pager;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewStub;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;

import org.nv95.openmanga.R;
import org.nv95.openmanga.common.utils.ErrorUtils;
import org.nv95.openmanga.common.views.TextProgressView;
import org.nv95.openmanga.core.models.MangaPage;
import org.nv95.openmanga.reader.decoder.ImageConverter;
import org.nv95.openmanga.reader.loader.PageDownloader;
import org.nv95.openmanga.reader.loader.PageLoadCallback;
import org.nv95.openmanga.reader.loader.PagesCache;

import java.io.File;

/**
 * Created by koitharu on 09.01.18.
 */

public final class PageView extends FrameLayout implements View.OnClickListener,
		ImageConverter.Callback, PageLoadCallback {

	private MangaPage mPage;
	private File mFile;

	private final SubsamplingScaleImageView mSubsamplingScaleImageView;
	private final TextProgressView mTextProgressView;
	@Nullable
	private ViewStub mStubError;
	@Nullable
	private View mErrorView = null;

	public PageView(@NonNull Context context) {
		this(context, null, 0);
	}

	public PageView(@NonNull Context context, @Nullable AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public PageView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		View.inflate(context, R.layout.view_page, this);
		mSubsamplingScaleImageView = findViewById(R.id.subsamplingImageView);
		mTextProgressView = findViewById(R.id.progressView);
		mStubError = findViewById(R.id.stubError);
		mSubsamplingScaleImageView.setDoubleTapZoomStyle(SubsamplingScaleImageView.ZOOM_FOCUS_FIXED);
		mSubsamplingScaleImageView.setPanLimit(SubsamplingScaleImageView.PAN_LIMIT_INSIDE);
		mSubsamplingScaleImageView.setMinimumDpi(90);
		mSubsamplingScaleImageView.setMinimumTileDpi(180);
		mSubsamplingScaleImageView.setOnImageEventListener(new SubsamplingScaleImageView.DefaultOnImageEventListener() {

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

	@SuppressLint("ClickableViewAccessibility")
	public void setTapDetector(final GestureDetector detector) {
		mSubsamplingScaleImageView.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				return detector.onTouchEvent(event);
			}
		});
	}

	public void setData(MangaPage page) {
		mTextProgressView.setProgress(TextProgressView.INDETERMINATE);
		mTextProgressView.setVisibility(VISIBLE);
		setError(null);
		mPage = page;
		mFile = PagesCache.getInstance(getContext()).getFileForUrl(page.url);
		if (mFile.exists()) {
			mSubsamplingScaleImageView.setImage(ImageSource.uri(Uri.fromFile(mFile)));
		} else {
			PageDownloader.getInstance().downloadPage(getContext(), page, mFile, this);
		}
	}

	private void setError(@Nullable CharSequence errorMessage) {
		if (errorMessage == null) {
			if (mErrorView != null) {
				mErrorView.setVisibility(GONE);
			}
			return;
		}
		mTextProgressView.setVisibility(GONE);
		if (mErrorView == null) {
			assert mStubError != null;
			mErrorView = mStubError.inflate();
			mErrorView.findViewById(R.id.button_retry).setOnClickListener(this);
			mStubError = null;
		}
		mErrorView.setVisibility(VISIBLE);
		((TextView)mErrorView.findViewById(R.id.textView_error)).setText(errorMessage);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.button_retry:
				setError(null);
				mTextProgressView.setVisibility(VISIBLE);
				mFile.delete();
				PageDownloader.getInstance().downloadPage(getContext(), mPage, mFile, this);
				break;
		}
	}

	/**
	 * Loading done, alles ok
	 */
	public void onLoadingComplete() {
		setError(null);
		mTextProgressView.setVisibility(GONE);
	}

	public void onImageDisplayFailed(Exception e) {
		if (mFile.exists()) {
			ImageConverter.getInstance().convert(mFile.getPath(), this);
		} else {
			mTextProgressView.setVisibility(GONE);
			setError(getContext().getString(ErrorUtils.getErrorMessage(e)));
		}
	}

	@Override
	public void onImageConverted() {
		mSubsamplingScaleImageView.setImage(ImageSource.uri(Uri.fromFile(mFile)));
	}

	@Override
	public void onImageConvertFailed() {
		setError(getContext().getString(R.string.image_decode_error));
	}

	@Override
	public void onPageDownloaded() {
		mSubsamplingScaleImageView.setImage(ImageSource.uri(Uri.fromFile(mFile)));
	}

	@Override
	public void onPageDownloadFailed(Throwable reason) {
		setError(ErrorUtils.getErrorMessage(getContext(), reason));
	}

	@Override
	public void onPageDownloadProgress(int progress, int max) {
		mTextProgressView.setProgress(progress, max);
	}

	public void recycle() {
		if (mPage != null) {
			PageDownloader.getInstance().cancel(mPage);
		}
		setError(null);
		mSubsamplingScaleImageView.recycle();
	}

	public MangaPage getData() {
		return mPage;
	}
}
