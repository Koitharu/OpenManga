package org.nv95.openmanga.ui.mangalist;

import android.content.Context;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.v7.widget.RecyclerView;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckedTextView;
import android.widget.TextView;

import org.nv95.openmanga.R;
import org.nv95.openmanga.content.MangaGenre;
import org.nv95.openmanga.ui.common.TypedString;
import org.nv95.openmanga.utils.CollectionsUtils;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by koitharu on 31.12.17.
 */

public final class FilterSortAdapter extends RecyclerView.Adapter {

	private final ArrayList<TypedString> mDataset;
	private int mSelectedSort = 0;
	private final SparseBooleanArray mSelectedGenres;

	FilterSortAdapter(Context context, @NonNull @StringRes int[] sorts, @NonNull MangaGenre[] genres, int selectedSort, String[] selectedGenres) {
		mDataset = new ArrayList<>();
		mSelectedGenres = new SparseBooleanArray();
		setHasStableIds(true);
		if (sorts.length != 0) {
			mDataset.add(new TypedString(context, R.string.action_sort, ItemViewType.TYPE_ITEM_HEADER, -1));
			for (int i = 0; i < sorts.length; i++) {
				mDataset.add(new TypedString(context, sorts[i], ItemViewType.TYPE_ITEM_SORT, i));
				if (sorts[i] == selectedSort) {
					mSelectedSort = i;
				}
			}
		}
		if (genres.length != 0) {
			mDataset.add(new TypedString(context, R.string.genres, ItemViewType.TYPE_ITEM_HEADER, -1));
			for (int i = 0; i < genres.length; i++) {
				mDataset.add(new TypedString(context, genres[i].nameId, ItemViewType.TYPE_ITEM_GENRE, i));
				if (CollectionsUtils.contains(selectedGenres, genres[i].value)) {
					mSelectedGenres.put(i, true);
				}
			}
		}
	}

	@NonNull
	@Override
	public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, @ItemViewType int viewType) {
		final LayoutInflater inflater = LayoutInflater.from(parent.getContext());
		RecyclerView.ViewHolder holder;
		switch (viewType) {
			case ItemViewType.TYPE_ITEM_GENRE:
				holder = new GenreHolder(inflater.inflate(R.layout.item_checkable_check, parent, false));
				break;
			case ItemViewType.TYPE_ITEM_HEADER:
				return new HeaderHolder(inflater.inflate(R.layout.header_group, parent, false));
			case ItemViewType.TYPE_ITEM_SORT:
				holder = new SortHolder(inflater.inflate(R.layout.item_checkable_radio, parent, false));
				break;
				default:
					throw new AssertionError("");
		}
		return holder;
	}

	@Override
	public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
		TypedString item = mDataset.get(position);
		if (holder instanceof HeaderHolder) {
			((HeaderHolder) holder).textView.setText(item.toString());
		} else if (holder instanceof GenreHolder) {
			((GenreHolder) holder).checkedTextView.setText(item.toString());
			if (holder instanceof SortHolder) {
				((SortHolder) holder).checkedTextView.setChecked(item.getSubPosition() == mSelectedSort);
			} else {
				((GenreHolder) holder).checkedTextView.setChecked(mSelectedGenres.get(item.getSubPosition(), false));
			}
		}

	}

	@Override
	public int getItemCount() {
		return mDataset.size();
	}

	@Override
	public long getItemId(int position) {
		return mDataset.get(position).hashCode();
	}

	@ItemViewType
	@Override
	public int getItemViewType(int position) {
		return mDataset.get(position).getType();
	}

	int getSelectedSort() {
		return mSelectedSort;
	}

	SparseBooleanArray getSelectedGenres() {
		return mSelectedGenres;
	}

	void reset() {
		mSelectedSort = 0;
		mSelectedGenres.clear();
		notifyDataSetChanged();
	}

	class GenreHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

		final CheckedTextView checkedTextView;

		GenreHolder(View itemView) {
			super(itemView);
			itemView.setOnClickListener(this);
			checkedTextView = itemView.findViewById(R.id.checkedTextView);
			itemView.setTag(1); //some magic
		}

		@Override
		public void onClick(View view) {
			int pos = mDataset.get(getAdapterPosition()).getSubPosition();
			if (mSelectedGenres.get(pos, false)) {
				mSelectedGenres.put(pos, false);
			} else {
				mSelectedGenres.put(pos, true);
			}
			notifyDataSetChanged();
		}
	}

	final class SortHolder extends GenreHolder {

		SortHolder(View itemView) {
			super(itemView);
		}

		@Override
		public void onClick(View view) {
			mSelectedSort = mDataset.get(getAdapterPosition()).getSubPosition();
			notifyDataSetChanged();
		}
	}

	final class HeaderHolder extends RecyclerView.ViewHolder {

		final TextView textView;

		HeaderHolder(View itemView) {
			super(itemView);
			textView = itemView.findViewById(R.id.textView);
		}
	}

	@Retention(RetentionPolicy.SOURCE)
	@IntDef({ItemViewType.TYPE_ITEM_HEADER, ItemViewType.TYPE_ITEM_SORT, ItemViewType.TYPE_ITEM_GENRE})
	public @interface ItemViewType {
		int TYPE_ITEM_HEADER = 0;
		int TYPE_ITEM_SORT = 1;
		int TYPE_ITEM_GENRE = 2;
	}
}
