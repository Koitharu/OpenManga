package org.nv95.openmanga.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import org.nv95.openmanga.R;
import org.nv95.openmanga.components.AsyncImageView;
import org.nv95.openmanga.utils.UpdatesChecker;

import java.util.ArrayList;

/**
 * Created by nv95 on 03.01.16.
 */
public class UpdatesAdapter extends BaseAdapter {
  private final ArrayList<UpdatesChecker.MangaUpdate> list;
  private LayoutInflater inflater;

  public UpdatesAdapter(Context context, ArrayList<UpdatesChecker.MangaUpdate> list) {
    this.list = list;
    inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
  }

  @Override
  public int getCount() {
    return list.size();
  }

  @Override
  public UpdatesChecker.MangaUpdate getItem(int position) {
    return list.get(position);
  }

  @Override
  public long getItemId(int position) {
    return getItem(position).manga.hashCode();
  }

  @Override
  public View getView(int position, View convertView, ViewGroup parent) {
    ViewHolder viewHolder;
    if (convertView == null) {
      convertView = inflater.inflate(R.layout.item_download, null);
      viewHolder = new ViewHolder();
      viewHolder.textViewTitle = (TextView) convertView.findViewById(R.id.textView_title);
      viewHolder.textViewStatus = (TextView) convertView.findViewById(R.id.textView_status);
      viewHolder.asyncImageView = (AsyncImageView) convertView.findViewById(R.id.imageView);
      convertView.setTag(viewHolder);
    } else {
      viewHolder = (ViewHolder) convertView.getTag();
    }
    UpdatesChecker.MangaUpdate update = getItem(position);
    int nc = update.chapters - update.lastChapters;
    viewHolder.textViewTitle.setText(update.manga.getName());
    viewHolder.asyncImageView.setImageAsync(update.manga.getPreview());
    if (nc > 0) {
      viewHolder.textViewStatus.setText(String.format(
              inflater.getContext().getString(R.string.new_chapters_count), nc
      ));
    } else {
      viewHolder.textViewStatus.setText(R.string.no_new_chapters);
    }
    return convertView;
  }

  private static class ViewHolder {
    TextView textViewTitle;
    TextView textViewStatus;
    AsyncImageView asyncImageView;
  }
}