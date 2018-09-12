package org.nv95.openmanga;

import android.app.Fragment;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by koitharu on 21.12.17.
 */

public abstract class AppBaseFragment extends Fragment {

	protected View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @LayoutRes int resource) {
		return inflater.inflate(resource, container, false);
	}

	public void scrollToTop() {
	}
}
