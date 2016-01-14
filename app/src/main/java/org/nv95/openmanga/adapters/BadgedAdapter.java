package org.nv95.openmanga.adapters;

import android.content.Context;
import android.util.Pair;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import org.nv95.openmanga.R;

/**
 * Created by nv95 on 14.01.16.
 */
public class BadgedAdapter extends ArrayAdapter<Pair<Integer,String>> {

  public BadgedAdapter(Context context, int resource, Pair<Integer, String>[] objects) {
    super(context, resource, objects);
  }

  @Override
  public View getView(int position, View convertView, ViewGroup parent) {
    TextView textView = (TextView) (convertView == null ? View.inflate(getContext(), R.layout.item_dir, null) : convertView);
    Pair<Integer,String> item = getItem(position);
    textView.setText(item.second);
    textView.setCompoundDrawablesWithIntrinsicBounds(item.first, 0, 0, 0);
    return textView;
  }
}
