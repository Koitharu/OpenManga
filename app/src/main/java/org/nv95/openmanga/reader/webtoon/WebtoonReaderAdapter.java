package org.nv95.openmanga.reader.webtoon;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import org.nv95.openmanga.R;
import org.nv95.openmanga.core.models.MangaPage;

import java.util.ArrayList;

final class WebtoonReaderAdapter extends RecyclerView.Adapter<WebtoonPageHolder> {

	private final ArrayList<MangaPage> mDataset;

	WebtoonReaderAdapter(ArrayList<MangaPage> dataset) {
		mDataset = dataset;
	}


	@Override
	public WebtoonPageHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		return new WebtoonPageHolder(LayoutInflater.from(parent.getContext())
				.inflate(R.layout.item_page_webtoon, parent, false));
	}

	@Override
	public void onBindViewHolder(WebtoonPageHolder holder, int position) {
		holder.bind(mDataset.get(position));
	}

	@Override
	public void onViewRecycled(WebtoonPageHolder holder) {
		holder.recycle();
		super.onViewRecycled(holder);
	}

	@Override
	public int getItemCount() {
		return mDataset.size();
	}
}
