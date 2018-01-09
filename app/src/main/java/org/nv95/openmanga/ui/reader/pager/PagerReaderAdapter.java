package org.nv95.openmanga.ui.reader.pager;

import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.nv95.openmanga.R;
import org.nv95.openmanga.content.MangaPage;

import java.util.ArrayList;

/**
 * Created by koitharu on 09.01.18.
 */

public final class PagerReaderAdapter extends PagerAdapter {

	private final ArrayList<MangaPage> mDataset;

	public PagerReaderAdapter(ArrayList<MangaPage> dataset) {
		mDataset = dataset;
	}

	@NonNull
	@Override
	public Object instantiateItem(@NonNull ViewGroup container, int position) {
		final MangaPage page = mDataset.get(position);
		LayoutInflater inflater = LayoutInflater.from(container.getContext());
		ViewGroup layout = (ViewGroup) inflater.inflate(R.layout.item_page, container, false);
		container.addView(layout);
		return layout;
	}

	@Override
	public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
		container.removeView((View) object);
	}


	@Override
	public int getCount() {
		return mDataset.size();
	}

	@Override
	public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
		return view == object;
	}

	@Override
	public int getItemPosition(@NonNull Object object) {
		return super.getItemPosition(object);
	}
}
