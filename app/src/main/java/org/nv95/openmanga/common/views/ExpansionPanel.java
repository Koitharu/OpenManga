package org.nv95.openmanga.common.views;

import android.animation.LayoutTransition;
import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v4.widget.TextViewCompat;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.nv95.openmanga.R;

/**
 * Created by koitharu on 02.02.18.
 */

public final class ExpansionPanel extends LinearLayout {

	private static final String TAG_PERSISTENT = "persistent";

	private final TextView mTextViewControl;
	private final View mDivider;
	private boolean mExpanded = false;

	public ExpansionPanel(Context context) {
		this(context, null, 0);
	}

	public ExpansionPanel(Context context, @Nullable AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public ExpansionPanel(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		View.inflate(context, R.layout.view_expansion_panel, this);
		setOrientation(VERTICAL);
		setLayoutTransition(new LayoutTransition());
		mTextViewControl = findViewById(R.id.textView_control);
		mDivider = findViewById(R.id.divider);
		mTextViewControl.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				toggle();
			}
		});
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		reorderViews();
	}

	public void toggle() {
		setExpanded(!mExpanded);
	}

	public void setExpanded(boolean expanded) {
		if (expanded == mExpanded) {
			return;
		}
		mExpanded = expanded;
		if (mExpanded) {
			expand();
		} else {
			collapse();
		}
	}

	private void expand() {
		mExpanded = true;
		mTextViewControl.setText(R.string.hide_details);
		TextViewCompat.setCompoundDrawablesRelativeWithIntrinsicBounds(mTextViewControl,
				0, 0, R.drawable.ic_collapse_black, 0);
		final int count = getChildCount();
		for (int i = 0; i < count; i++) {
			final View child = getChildAt(i);
			if (!TAG_PERSISTENT.equals(child.getTag())) {
				child.setVisibility(VISIBLE);
			}
		}
	}

	private void collapse() {
		mExpanded = false;
		mTextViewControl.setText(R.string.show_more_details);
		TextViewCompat.setCompoundDrawablesRelativeWithIntrinsicBounds(mTextViewControl,
				0, 0, R.drawable.ic_expand_black, 0);
		final int count = getChildCount();
		for (int i = 0; i < count; i++) {
			final View child = getChildAt(i);
			if (!TAG_PERSISTENT.equals(child.getTag())) {
				child.setVisibility(GONE);
			}
		}
	}

	private void reorderViews() {
		removeView(mDivider);
		removeView(mTextViewControl);
		addView(mDivider);
		addView(mTextViewControl);
	}

	public static void hideChild(View view) {
		view.setTag(TAG_PERSISTENT);
		view.setVisibility(GONE);
	}

	public static void showChild(View view) {
		view.setTag(null);
		view.setVisibility(VISIBLE);
	}
}
