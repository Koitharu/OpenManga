package org.nv95.openmanga.reader.pager;

import android.support.annotation.NonNull;
import android.view.GestureDetector;

import org.nv95.openmanga.core.models.MangaPage;

import java.util.ArrayList;

/**
 * Created by koitharu on 06.02.18.
 */

final class RtlPagerReaderAdapter extends PagerReaderAdapter {

	RtlPagerReaderAdapter(ArrayList<MangaPage> dataset, GestureDetector gestureDetector) {
		super(dataset, gestureDetector);
	}

	@Override
	protected void onBindView(@NonNull PageView view, int position) {
		super.onBindView(view, getCount() - position - 1);
	}
}
