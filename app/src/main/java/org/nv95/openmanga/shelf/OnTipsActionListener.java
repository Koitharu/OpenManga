package org.nv95.openmanga.shelf;

import android.support.annotation.IdRes;

/**
 * Created by koitharu on 29.01.18.
 */

public interface OnTipsActionListener {

	void onTipActionClick(@IdRes int actionId);

	void onTipDismissed(@IdRes int actionId);
}
