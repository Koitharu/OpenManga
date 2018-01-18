package org.nv95.openmanga.shelf;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import org.nv95.openmanga.R;
import org.nv95.openmanga.common.CrashHandler;
import org.nv95.openmanga.common.Dismissible;
import org.nv95.openmanga.common.utils.ImageUtils;
import org.nv95.openmanga.common.utils.ResourceUtils;
import org.nv95.openmanga.core.models.ListHeader;
import org.nv95.openmanga.core.models.MangaFavourite;
import org.nv95.openmanga.core.models.MangaHeader;
import org.nv95.openmanga.core.models.MangaHistory;
import org.nv95.openmanga.core.models.UserTip;
import org.nv95.openmanga.core.providers.MangaProvider;
import org.nv95.openmanga.mangalist.favourites.FavouritesActivity;
import org.nv95.openmanga.mangalist.history.HistoryActivity;
import org.nv95.openmanga.preview.PreviewActivity;
import org.nv95.openmanga.reader.ReaderActivity;

import java.util.ArrayList;

/**
 * Created by koitharu on 21.12.17.
 */

public final class ShelfAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

	private final ArrayList<Object> mDataset;

	public ShelfAdapter() {
		mDataset = new ArrayList<>();
		setHasStableIds(true);
	}

	void updateData(ArrayList<Object> data) {
		mDataset.clear();
		mDataset.addAll(data);
		notifyDataSetChanged();
	}

	@Override
	public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, @ShelfItemType int viewType) {
		final LayoutInflater inflater = LayoutInflater.from(parent.getContext());
		switch (viewType) {
			case ShelfItemType.TYPE_HEADER:
				return new HeaderHolder(inflater.inflate(R.layout.header_group_button, parent, false));
			case ShelfItemType.TYPE_ITEM_DEFAULT:
				return new MangaHolder(inflater.inflate(R.layout.item_manga, parent, false));
			case ShelfItemType.TYPE_RECENT:
				return new RecentHolder(inflater.inflate(R.layout.item_recent, parent, false));
			case ShelfItemType.TYPE_ITEM_SMALL:
				return new MangaHolder(inflater.inflate(R.layout.item_manga_small, parent, false));
			case ShelfItemType.TYPE_TIP:
				return new TipHolder(inflater.inflate(R.layout.item_tip, parent, false));
			default:
				throw new AssertionError("Unknown viewType");
		}
	}

	@Override
	public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
		if (holder instanceof TipHolder) {
			UserTip tip = (UserTip) mDataset.get(position);
			((TipHolder) holder).textViewTitle.setText(tip.title);
			((TipHolder) holder).textViewContent.setText(tip.content);
			if (tip.hasIcon()) {
				((TipHolder) holder).imageViewIcon.setImageResource(tip.icon);
				((TipHolder) holder).imageViewIcon.setVisibility(View.VISIBLE);
			} else {
				((TipHolder) holder).imageViewIcon.setVisibility(View.GONE);
			}
			if (tip.hasAction()) {
				((TipHolder) holder).buttonAction.setText(tip.actionText);
				((TipHolder) holder).buttonAction.setId(tip.actionId);
				((TipHolder) holder).buttonAction.setVisibility(View.VISIBLE);
			} else {
				((TipHolder) holder).buttonAction.setVisibility(View.GONE);
			}
		} else if (holder instanceof HeaderHolder) {
			ListHeader item = (ListHeader) mDataset.get(position);
			if (item.text != null) {
				((HeaderHolder) holder).textView.setText(item.text);
			} else if (item.textResId != 0) {
				((HeaderHolder) holder).textView.setText(item.textResId);
			} else {
				((HeaderHolder) holder).textView.setText(null);
			}
			holder.itemView.setTag(item.extra);
		} else if (holder instanceof MangaHolder) {
			MangaHeader item = (MangaHeader) mDataset.get(position);
			ImageUtils.setThumbnail(((MangaHolder) holder).imageViewThumbnail, item.thumbnail, MangaProvider.getDomain(item.provider));
			((MangaHolder) holder).textViewTitle.setText(item.name);
			holder.itemView.setTag(item);
			if (holder instanceof RecentHolder) {
				MangaHistory history = (MangaHistory) item;
				((RecentHolder) holder).textViewSubtitle.setText(item.summary);
				((RecentHolder) holder).textViewStatus.setText(ResourceUtils.formatTimeRelative(history.updatedAt));
			}
		}

	}

	@ShelfItemType
	@Override
	public int getItemViewType(int position) {
		Object item = mDataset.get(position);
		if (item instanceof ListHeader) {
			return ShelfItemType.TYPE_HEADER;
		} else if (item instanceof UserTip) {
			return ShelfItemType.TYPE_TIP;
		} else if (item instanceof MangaHistory) {
			return ShelfItemType.TYPE_RECENT;
		} else if (item instanceof MangaFavourite) {
			return ShelfItemType.TYPE_ITEM_DEFAULT;
		} else if (item instanceof MangaHeader) {
			return ShelfItemType.TYPE_ITEM_SMALL;
		} else {
			throw new AssertionError("Unknown viewType");
		}
	}

	@Override
	public int getItemCount() {
		return mDataset.size();
	}

	@Override
	public long getItemId(int position) {
		Object item = mDataset.get(position);
		if (item instanceof MangaHeader) {
			return ((MangaHeader) item).id;
		} else if (item instanceof ListHeader) {
			final String text = ((ListHeader) item).text;
			return text != null ? text.hashCode() : ((ListHeader) item).textResId;
		} else if (item instanceof UserTip) {
			return ((UserTip) item).title.hashCode() + ((UserTip) item).content.hashCode();
		} else {
			throw new AssertionError("Unknown viewType");
		}
	}

	@Override
	public void onViewRecycled(RecyclerView.ViewHolder holder) {
		if (holder instanceof MangaHolder) {
			ImageUtils.recycle(((MangaHolder) holder).imageViewThumbnail);
		}
		super.onViewRecycled(holder);
	}

	class HeaderHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

		final TextView textView;
		final Button buttonMore;

		HeaderHolder(View itemView) {
			super(itemView);
			textView = itemView.findViewById(R.id.textView);
			buttonMore = itemView.findViewById(R.id.button_more);
			buttonMore.setOnClickListener(this);
		}

		@Override
		public void onClick(View v) {
			final Object extra = itemView.getTag();
			if (extra == null) {
				return;
			}
			final Context context = v.getContext();
			if (extra.equals(ShelfContent.SECTION_HISTORY)) {
				context.startActivity(new Intent(context, HistoryActivity.class));
			} else if (extra instanceof Integer) {
				context.startActivity(new Intent(context, FavouritesActivity.class)
					.putExtra("category_id", (Integer) extra));
			}
		}
	}

	class TipHolder extends RecyclerView.ViewHolder implements View.OnClickListener, Dismissible {

		final TextView textViewTitle;
		final TextView textViewContent;
		final ImageView imageViewIcon;
		final Button buttonAction;

		TipHolder(View itemView) {
			super(itemView);
			textViewTitle = itemView.findViewById(android.R.id.text1);
			textViewContent = itemView.findViewById(android.R.id.text2);
			buttonAction = itemView.findViewById(android.R.id.button1);
			imageViewIcon = itemView.findViewById(android.R.id.icon);
			buttonAction.setOnClickListener(this);
		}

		@Override
		public void onClick(View v) {
			switch (v.getId()) {
				case R.id.action_crash_report:
					final CrashHandler crashHandler = CrashHandler.get();
					if (crashHandler != null) {
						new AlertDialog.Builder(v.getContext())
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
			switch (buttonAction.getId()) {
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

	class MangaHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

		final ImageView imageViewThumbnail;
		final TextView textViewTitle;

		MangaHolder(View itemView) {
			super(itemView);
			imageViewThumbnail = itemView.findViewById(R.id.imageViewThumbnail);
			textViewTitle = itemView.findViewById(R.id.textViewTitle);
			itemView.setOnClickListener(this);
		}

		@Override
		public void onClick(View view) {
			MangaHeader mangaHeader = (MangaHeader) itemView.getTag();
			final Context context = view.getContext();
			context.startActivity(new Intent(context.getApplicationContext(), PreviewActivity.class)
					.putExtra("manga", mangaHeader));
		}
	}

	class RecentHolder extends MangaHolder {

		final TextView textViewStatus;
		final TextView textViewSubtitle;

		RecentHolder(View itemView) {
			super(itemView);
			textViewStatus = itemView.findViewById(R.id.textView_status);
			textViewSubtitle = itemView.findViewById(R.id.textView_subtitle);
			itemView.findViewById(R.id.button_continue).setOnClickListener(this);
		}

		@Override
		public void onClick(View view) {
			if (view.getId() == R.id.button_continue) {
				MangaHeader mangaHeader = (MangaHeader) itemView.getTag();
				final Context context = view.getContext();
				context.startActivity(new Intent(context.getApplicationContext(), ReaderActivity.class)
						.setAction(ReaderActivity.ACTION_READING_CONTINUE)
						.putExtra("manga", mangaHeader));
			} else {
				super.onClick(view);
			}
		}
	}
}
