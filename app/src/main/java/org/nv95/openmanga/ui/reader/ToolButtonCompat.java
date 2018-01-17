package org.nv95.openmanga.ui.reader;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;
import android.widget.ImageView;

import org.nv95.openmanga.utils.ThemeUtils;

/**
 * Created by koitharu on 08.01.18.
 */

public final class ToolButtonCompat extends AppCompatImageView {

	public ToolButtonCompat(Context context) {
		this(context, null, 0);
	}

	public ToolButtonCompat(Context context, @Nullable AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public ToolButtonCompat(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		setBackgroundDrawable(ThemeUtils.getSelectableBackgroundBorderless(context));
		setScaleType(ImageView.ScaleType.CENTER);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(
				heightMeasureSpec,
				heightMeasureSpec
		);
	}

}
