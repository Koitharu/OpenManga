package org.nv95.openmanga.ui.browser;

import android.support.annotation.IntDef;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.nv95.openmanga.R;
import org.nv95.openmanga.ui.shelf.ShelfAdapter;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;

/**
 * Created by koitharu on 26.12.17.
 */

public final class BrowserIndexAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements View.OnClickListener {

	private final ArrayList<Object> mDataset;

	public BrowserIndexAdapter(ArrayList<Object> dataset) {
		mDataset = dataset;
	}

	@Override
	public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, @ItemViewType int viewType) {
		final LayoutInflater inflater = LayoutInflater.from(parent.getContext());
		RecyclerView.ViewHolder holder;
		switch (viewType) {
			case ItemViewType.TYPE_ITEM_WITH_ICON:
				holder = new DetailsProviderHolder(inflater.inflate(R.layout.item_two_lines_icon, parent, false));
				break;
			case ItemViewType.TYPE_HEADER:
				return new HeaderHolder(inflater.inflate(R.layout.header_group, parent, false));
			case ItemViewType.TYPE_ITEM_DEFAULT:
				holder = new ProviderHolder(inflater.inflate(R.layout.item_single_line, parent, false));
				break;
			default:
				throw new AssertionError("Unknown viewType");
		}
		holder.itemView.setOnClickListener(this);
		return holder;
	}

	@Override
	public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
		if (holder instanceof DetailsProviderHolder) {
			ProviderHeaderDetailed item = (ProviderHeaderDetailed) mDataset.get(position);
			((DetailsProviderHolder) holder).text1.setText(item.dname);
			holder.itemView.setTag(item.cname);
			((DetailsProviderHolder) holder).icon.setImageDrawable(item.icon);
			((DetailsProviderHolder) holder).text2.setText(item.summary);
		} else if (holder instanceof ProviderHolder) {
			ProviderHeader item = (ProviderHeader) mDataset.get(position);
			holder.itemView.setTag(item.cname);
			((ProviderHolder) holder).text1.setText(item.dname);
		} else if (holder instanceof HeaderHolder) {
			((HeaderHolder) holder).textView.setText((String) mDataset.get(position));
		}
	}

	@ItemViewType
	@Override
	public int getItemViewType(int position) {
		final Object item = mDataset.get(position);
		if (item instanceof String) {
			return ItemViewType.TYPE_HEADER;
		} else if (item instanceof ProviderHeaderDetailed) {
			return ItemViewType.TYPE_ITEM_WITH_ICON;
		} else if (item instanceof ProviderHeader) {
			return ItemViewType.TYPE_ITEM_DEFAULT;
		} else {
			throw new AssertionError("Unknown ItemViewType");
		}
	}

	@Override
	public int getItemCount() {
		return mDataset.size();
	}

	@Override
	public void onClick(View view) {
		final String cname = String.valueOf(view.getTag());
	}

	class ProviderHolder extends RecyclerView.ViewHolder {

		final TextView text1;

		ProviderHolder(View itemView) {
			super(itemView);
			text1 = itemView.findViewById(android.R.id.text1);
		}
	}

	class DetailsProviderHolder extends ProviderHolder {

		final TextView text2;
		final ImageView icon;

		DetailsProviderHolder(View itemView) {
			super(itemView);
			text2 = itemView.findViewById(android.R.id.text2);
			icon = itemView.findViewById(android.R.id.icon);
		}
	}

	class HeaderHolder extends RecyclerView.ViewHolder {

		private final TextView textView;

		public HeaderHolder(View itemView) {
			super(itemView);
			textView = itemView.findViewById(R.id.textView);
		}
	}

	@Retention(RetentionPolicy.SOURCE)
	@IntDef({ItemViewType.TYPE_ITEM_DEFAULT, ItemViewType.TYPE_ITEM_WITH_ICON, ItemViewType.TYPE_HEADER})
	public @interface ItemViewType {
		int TYPE_ITEM_DEFAULT = 0;
		int TYPE_ITEM_WITH_ICON = 1;
		int TYPE_HEADER = 3;
	}
}
