package org.nv95.openmanga.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import org.nv95.openmanga.R;
import org.nv95.openmanga.components.AsyncImageView;
import org.nv95.openmanga.items.MangaInfo;
import org.nv95.openmanga.items.MangaList;

/**
 * Created by nv95 on 30.09.15.
 */
public class MangaListAdapter extends BaseAdapter {
  private MangaList list;
  private Context context;
  private boolean grid;
  private LayoutInflater inflater;

  public MangaListAdapter(Context context, MangaList list, boolean grid) {
    this.list = list;
    this.context = context;
    this.grid = grid;
    inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
  }

  @Override
  public int getCount() {
    return list.size();
  }

  @Override
  public Object getItem(int position) {
    return list.get(position);
  }

  @Override
  public long getItemId(int position) {
    return list.get(position).getPath().hashCode();
  }

  public MangaInfo getMangaInfo(int position) {
    return list.get(position);
  }

  @Override
  public View getView(int position, View convertView, ViewGroup parent) {
    ViewHolder viewHolder;
    if (convertView == null) {
      convertView = inflater.inflate(grid ? R.layout.item_mangagrid : R.layout.item_mangalist, null);
      viewHolder = new ViewHolder();
      viewHolder.textViewTitle = (TextView) convertView.findViewById(R.id.textView_title);
      viewHolder.textViewSubtitle = (TextView) convertView.findViewById(R.id.textView_subtitle);
      viewHolder.textViewSummary = (TextView) convertView.findViewById(R.id.textView_summary);
      viewHolder.asyncImageView = (AsyncImageView) convertView.findViewById(R.id.imageView);
      convertView.setTag(viewHolder);
    } else {
      viewHolder = (ViewHolder) convertView.getTag();
    }
    MangaInfo info = getMangaInfo(position);
    viewHolder.textViewTitle.setText(info.getName());
    viewHolder.textViewSubtitle.setText(info.getSubtitle());
    viewHolder.textViewSummary.setText(info.getSummary());
    viewHolder.asyncImageView.setImageAsync(info.getPreview());
    return convertView;
  }

  public boolean isGrid() {
    return grid;
  }

  public void setGrid(boolean grid) {
    if (this.grid != grid) {
      this.grid = grid;
      notifyDataSetChanged();
    }
  }

  @Override
  public boolean hasStableIds() {
    return true;
  }

  private static class ViewHolder {
    TextView textViewTitle;
    TextView textViewSubtitle;
    TextView textViewSummary;
    AsyncImageView asyncImageView;
  }
}
