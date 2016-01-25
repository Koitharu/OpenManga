package org.nv95.openmanga.adapters;

import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.Nullable;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.nv95.openmanga.R;
import org.nv95.openmanga.components.AsyncImageView;
import org.nv95.openmanga.items.MangaInfo;
import org.nv95.openmanga.lists.MangaList;
import org.nv95.openmanga.providers.MangaProviderManager;
import org.nv95.openmanga.utils.SerialExecutor;

import java.util.ArrayList;

/**
 * Created by nv95 on 12.01.16.
 */
public class MultipleSearchAdapter extends BaseExpandableListAdapter implements View.OnClickListener {
  public final ArrayList<Pair<MangaProviderManager.ProviderSumm, MangaList>> list;
  private final LayoutInflater inflater;
  private final String query;
  private final SerialExecutor serialExecutor = new SerialExecutor();
  private final AdapterView.OnItemClickListener onMoreClickListener;
  private final MangaProviderManager manager;

  public MultipleSearchAdapter(Context context, String query, AdapterView.OnItemClickListener onMoreClickListener) {
    this.query = query;
    this.onMoreClickListener = onMoreClickListener;
    inflater = LayoutInflater.from(context);
    manager = new MangaProviderManager(context);
    ArrayList<MangaProviderManager.ProviderSumm> providers = manager.getEnabledProviders();
    list = new ArrayList<>();
    for (MangaProviderManager.ProviderSumm o : providers) {
      list.add(new Pair<>(o, new MangaList()));
    }
  }

  @Override
  public int getGroupCount() {
    return list.size();
  }

  @Override
  public int getChildrenCount(int groupPosition) {
    return list.get(groupPosition).second.size();
  }

  @Override
  public MangaProviderManager.ProviderSumm getGroup(int groupPosition) {
    return list.get(groupPosition).first;
  }

  @Override
  public MangaInfo getChild(int groupPosition, int childPosition) {
    return list.get(groupPosition).second.get(childPosition);
  }

  @Override
  public long getGroupId(int groupPosition) {
    return getGroup(groupPosition).hashCode();
  }

  @Override
  public long getChildId(int groupPosition, int childPosition) {
    return getChild(groupPosition, childPosition).hashCode();
  }

  @Override
  public boolean hasStableIds() {
    return true;
  }

  @Override
  public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
    GroupViewHolder holder;
    if (convertView == null) {
      convertView = inflater.inflate(R.layout.item_multisearch_group, null);
      holder = new GroupViewHolder();
      holder.textView = (TextView) convertView.findViewById(R.id.textView);
      holder.progressBar = (ProgressBar) convertView.findViewById(R.id.progressBar);
      holder.textViewMore = (TextView) convertView.findViewById(R.id.button);
      holder.textViewMore.setTag(groupPosition);
      holder.textViewMore.setOnClickListener(this);
      holder._position = groupPosition;
      if (parent instanceof ExpandableListView) {
        ((ExpandableListView) parent).expandGroup(groupPosition);
      }
      convertView.setTag(holder);
    } else {
      holder = (GroupViewHolder) convertView.getTag();
    }
    MangaProviderManager.ProviderSumm prov = getGroup(groupPosition);
    holder.textView.setText(prov.name);
    int count = getChildrenCount(groupPosition);
    if (count == 0 && holder.task == null) {
      holder.task = new SublistTask(holder);
      holder.task.executeOnExecutor(serialExecutor, prov);
    }
    holder.progressBar.setVisibility(
            holder.task != null && holder.task.getStatus() != AsyncTask.Status.FINISHED ?
                    View.VISIBLE : View.INVISIBLE
    );
    holder.textViewMore.setVisibility(count != 0 ? View.VISIBLE : View.INVISIBLE);
    return convertView;
  }

  @Override
  public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
    ViewHolder viewHolder;
    if (convertView == null) {
      convertView = inflater.inflate(R.layout.item_mangalist, null);
      viewHolder = new ViewHolder();
      viewHolder.textViewTitle = (TextView) convertView.findViewById(R.id.textView_title);
      viewHolder.textViewSubtitle = (TextView) convertView.findViewById(R.id.textView_subtitle);
      viewHolder.textViewSummary = (TextView) convertView.findViewById(R.id.textView_summary);
      viewHolder.asyncImageView = (AsyncImageView) convertView.findViewById(R.id.imageView);
      convertView.setTag(viewHolder);
    } else {
      viewHolder = (ViewHolder) convertView.getTag();
    }
    MangaInfo info = getChild(groupPosition, childPosition);
    viewHolder.textViewTitle.setText(info.getName());
    viewHolder.textViewSubtitle.setText(info.getSubtitle());
    viewHolder.textViewSummary.setText(info.getSummary());
    viewHolder.asyncImageView.setImageAsync(info.getPreview());
    return convertView;
  }

  @Override
  public boolean isChildSelectable(int groupPosition, int childPosition) {
    return true;
  }

  @Override
  public void onClick(View v) {
    int pos = (int) v.getTag();
    onMoreClickListener.onItemClick(null, v, pos, getGroupId(pos));
  }

  private static class GroupViewHolder {
    TextView textView;
    ProgressBar progressBar;
    TextView textViewMore;
    @Nullable
    SublistTask task = null;
    int _position;
  }

  private static class ViewHolder {
    TextView textViewTitle;
    TextView textViewSubtitle;
    TextView textViewSummary;
    AsyncImageView asyncImageView;
  }

  private class SublistTask extends AsyncTask<MangaProviderManager.ProviderSumm, Void, MangaList> {
    private final GroupViewHolder groupViewHolder;

    private SublistTask(GroupViewHolder groupViewHolder) {
      this.groupViewHolder = groupViewHolder;
    }

    @Override
    protected void onPreExecute() {
      super.onPreExecute();
    }

    @Override
    protected MangaList doInBackground(MangaProviderManager.ProviderSumm... params) {
      try {
        return params[0].instance().search(query, 0);
      } catch (Exception e) {
        return null;
      }
    }

    @Override
    protected void onPostExecute(MangaList mangaInfos) {
      super.onPostExecute(mangaInfos);
      if (mangaInfos != null) {
        list.get(groupViewHolder._position).second.addAll(mangaInfos);
      }
      notifyDataSetChanged();
    }
  }
}
