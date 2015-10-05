package org.nv95.openmanga;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import org.nv95.openmanga.providers.MangaInfo;
import org.nv95.openmanga.providers.MangaList;

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
        return position;
    }

    public MangaInfo getMangaInfo(int position) {
        return list.get(position);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null)
            convertView = inflater.inflate(R.layout.item_mangalist, null);
        MangaInfo info = getMangaInfo(position);
        ((TextView) convertView.findViewById(R.id.textView_title)).setText(info.getName());
        ((TextView) convertView.findViewById(R.id.textView_subtitle)).setText(info.getSubtitle());
        ((TextView) convertView.findViewById(R.id.textView_summary)).setText(info.getSummary());
        ((ImageView) convertView.findViewById(R.id.imageView)).setImageDrawable(new ColorDrawable(Color.TRANSPARENT));
        new ImageLoadTask((ImageView) convertView.findViewById(R.id.imageView),info.getPreview(), false, 0).execute();
        return convertView;
    }
}
