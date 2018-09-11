package org.nv95.openmanga.common.views;

import android.animation.LayoutTransition;
import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.nv95.openmanga.R;
import org.nv95.openmanga.common.utils.ResourceUtils;

/**
 * Created by koitharu on 09.01.18.
 */

public final class TextProgressView extends LinearLayout {

	private static final int MIN_SHOW_TIME = 500; // ms
	private static final int MIN_DELAY = 500; // ms
	public static final int INDETERMINATE = -1;

	private long mStartTime = -1;

	private boolean mPostedHide = false;
	private boolean mPostedShow = false;
	private boolean mDismissed = false;

	private final Runnable mDelayedHide = () -> {
		mPostedHide = false;
		mStartTime = -1;
		setVisibility(View.GONE);
	};

	private final Runnable mDelayedShow = () -> {
		mPostedShow = false;
		if (!mDismissed) {
			mStartTime = System.currentTimeMillis();
			setVisibility(View.VISIBLE);
		}
	};

	private final ProgressBar mProgressBar;
	private final TextView mTextView;

	private int mMax = 0;
	private int mProgress = -1;

	public TextProgressView(Context context) {
		this(context, null, 0);
	}

	public TextProgressView(Context context, @Nullable AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public TextProgressView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		View.inflate(context, R.layout.view_progress, this);
		setOrientation(VERTICAL);
		setLayoutTransition(new LayoutTransition());
		setGravity(Gravity.CENTER_HORIZONTAL);
		final int padding = ResourceUtils.dpToPx(context.getResources(),4);
		setPadding(padding, padding, padding, padding);
		mTextView = findViewById(android.R.id.text1);
		mProgressBar = findViewById(android.R.id.progress);
	}

	@Override
	public void onAttachedToWindow() {
		super.onAttachedToWindow();
		removeCallbacks();
	}

	@Override
	public void onDetachedFromWindow() {
		super.onDetachedFromWindow();
		removeCallbacks();
	}

	private void removeCallbacks() {
		removeCallbacks(mDelayedHide);
		removeCallbacks(mDelayedShow);
	}

	/**
	 * Hide the progress view if it is visible. The progress view will not be
	 * hidden until it has been shown for at least a minimum show time. If the
	 * progress view was not yet visible, cancels showing the progress view.
	 */
	public void hide() {
		mDismissed = true;
		removeCallbacks(mDelayedShow);
		long diff = System.currentTimeMillis() - mStartTime;
		if (diff >= MIN_SHOW_TIME || mStartTime == -1) {
			// The progress spinner has been shown long enough
			// OR was not shown yet. If it wasn't shown yet,
			// it will just never be shown.
			setVisibility(View.GONE);
		} else {
			// The progress spinner is shown, but not long enough,
			// so put a delayed message in to hide it when its been
			// shown long enough.
			if (!mPostedHide) {
				postDelayed(mDelayedHide, MIN_SHOW_TIME - diff);
				mPostedHide = true;
			}
		}
	}

	/**
	 * Show the progress view after waiting for a minimum delay. If
	 * during that time, hide() is called, the view is never made visible.
	 */
	public void show() {
		// Reset the start time.
		mStartTime = -1;
		mDismissed = false;
		removeCallbacks(mDelayedHide);
		if (!mPostedShow) {
			postDelayed(mDelayedShow, MIN_DELAY);
			mPostedShow = true;
		}
	}

	public void setMax(int max) {
		mMax = max;
		updateState();
	}

	public void setProgress(int progress) {
		mProgress = progress;
		updateState();
	}

	public void setProgress(int progress, int max) {
		mProgress = progress;
		mMax = max;
		updateState();
	}

	@SuppressLint("SetTextI18n")
	private void updateState() {
		if (mMax == 0 || mProgress < 0) {
			mTextView.setVisibility(GONE);
		} else {
			mTextView.setVisibility(VISIBLE);
			final int percent = Math.round(mProgress / mMax * 100);
			mTextView.setText(percent + "%");
		}
	}
}
