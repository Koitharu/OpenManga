package org.nv95.openmanga.settings;

import android.support.annotation.IntDef;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import org.nv95.openmanga.common.CrashHandler;
import org.nv95.openmanga.R;
import org.nv95.openmanga.common.Dismissible;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;

/**
 * Created by koitharu on 12.01.18.
 */

public class SettingsAdapter extends RecyclerView.Adapter<SettingsAdapter.PreferenceHolder> {

	private final ArrayList<SettingsHeader> mDataset;
	private final AdapterView.OnItemClickListener mClickListener;

	public SettingsAdapter(ArrayList<SettingsHeader> headers, AdapterView.OnItemClickListener clickListener) {
		mDataset = headers;
		mClickListener = clickListener;
		setHasStableIds(true);
	}

	@Override
	public PreferenceHolder onCreateViewHolder(ViewGroup parent, @ItemType int viewType) {
		switch (viewType) {
			case ItemType.TYPE_ITEM_DEFAULT:
				return new PreferenceHolder(LayoutInflater.from(parent.getContext())
						.inflate(R.layout.item_two_lines_icon, parent, false));
			case ItemType.TYPE_TIP:
				return new TipHolder(LayoutInflater.from(parent.getContext())
						.inflate(R.layout.item_tip, parent, false));
			default:
				throw new AssertionError("Unknown viewType");
		}
	}

	@Override
	public void onBindViewHolder(PreferenceHolder holder, int position) {
		SettingsHeader item = mDataset.get(position);
		holder.text1.setText(item.title);
		holder.icon.setImageDrawable(item.icon);
		if (item.summary == null) {
			holder.text2.setVisibility(View.GONE);
		} else {
			holder.text2.setText(item.summary);
			holder.text2.setVisibility(View.VISIBLE);
		}
		if (holder instanceof TipHolder) {
			((TipHolder) holder).button.setText(item.actionText);
			((TipHolder) holder).button.setId(item.actionId);
		}
	}

	@Override
	public int getItemViewType(int position) {
		return mDataset.get(position).hasAction() ? ItemType.TYPE_TIP : ItemType.TYPE_ITEM_DEFAULT;

	}

	@Override
	public long getItemId(int position) {
		return mDataset.get(position).title.hashCode();
	}

	@Override
	public int getItemCount() {
		return mDataset.size();
	}

	class PreferenceHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

		private final ImageView icon;
		private final TextView text1;
		private final TextView text2;

		PreferenceHolder(View itemView) {
			super(itemView);
			icon = itemView.findViewById(android.R.id.icon);
			text1 = itemView.findViewById(android.R.id.text1);
			text2 = itemView.findViewById(android.R.id.text2);
			itemView.setOnClickListener(this);
		}

		@Override
		public void onClick(View view) {
			mClickListener.onItemClick(null, itemView, getAdapterPosition(), getItemId());
		}
	}

	class TipHolder extends PreferenceHolder implements Dismissible {

		final Button button;

		TipHolder(View itemView) {
			super(itemView);
			itemView.setOnClickListener(null);
			button = itemView.findViewById(android.R.id.button1);
			button.setOnClickListener(this);
		}

		@Override
		public void onClick(View view) {
			switch (view.getId()) {
				case R.id.action_crash_report:
					final CrashHandler crashHandler = CrashHandler.get();
					if (crashHandler != null) {
						new AlertDialog.Builder(view.getContext())
								.setTitle(crashHandler.getErrorClassName())
								.setMessage(crashHandler.getErrorMessage() + "\n\n" + crashHandler.getErrorStackTrace())
								.setNegativeButton(R.string.close, null)
								.create()
								.show();
					}
					break;
			}
		}

		@Override
		public void dismiss() {
			switch (button.getId()) {
				case R.id.action_crash_report:
					final CrashHandler crashHandler = CrashHandler.get();
					if (crashHandler != null) {
						crashHandler.clear();
					}
					break;
			}
			mDataset.remove(getAdapterPosition());
			notifyDataSetChanged();
			//notifyItemRemoved throws ArrayIndexOutOfBoundsException
		}
	}

	@Retention(RetentionPolicy.SOURCE)
	@IntDef({ItemType.TYPE_ITEM_DEFAULT, ItemType.TYPE_TIP})
	public @interface ItemType {
		int TYPE_ITEM_DEFAULT = 0;
		int TYPE_TIP = 1;
	}
}