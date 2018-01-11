package org.nv95.openmanga.ui.common;

import android.content.Context;
import android.support.v7.widget.AppCompatButton;
import android.util.AttributeSet;

import org.nv95.openmanga.utils.ThemeUtils;

/**
 * Created by koitharu on 11.01.18.
 */

public final class BorderlessButton extends AppCompatButton {

	public BorderlessButton(Context context) {
		this(context, null, 0);
	}

	public BorderlessButton(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public BorderlessButton(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		setBackgroundDrawable(ThemeUtils.getSelectableBackgroundBorderless(context));
	}
}
