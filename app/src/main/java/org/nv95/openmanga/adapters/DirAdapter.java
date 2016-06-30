package org.nv95.openmanga.adapters;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import org.nv95.openmanga.R;
import org.nv95.openmanga.utils.LayoutUtils;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by nv95 on 01.01.16.
 */
public class DirAdapter extends BaseAdapter {
    private final Context context;
    private ArrayList<File> files;
    private File currentDir;
    private final Drawable[] icons;

    public DirAdapter(Context context, File dir) {
        this.context = context;
        files = new ArrayList<>();
        icons = LayoutUtils.getThemedIcons(
                context,
                R.drawable.ic_directory_dark,
                R.drawable.ic_directory_null_dark
        );
        setCurrentDir(dir);
    }

    @NonNull
    public File getCurrentDir() {
        return currentDir;
    }

    public void setCurrentDir(@NonNull File dir) {
        currentDir = dir;
        files.clear();
        File[] list = dir.listFiles();
        if (list != null) {
            for (File o : list) {
                if (o.isDirectory()) {
                    files.add(o);
                }
            }
        }
    }

    @Override
    public int getCount() {
        return files.size();
    }

    @Override
    public File getItem(int position) {
        return files.get(position);
    }

    @Override
    public long getItemId(int position) {
        return files.get(position).hashCode();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        TextView textView = (TextView) (convertView == null ? View.inflate(context, R.layout.item_dir, null) : convertView);
        File f = getItem(position);
        textView.setText(f.getName());
        textView.setCompoundDrawablesWithIntrinsicBounds(f.canWrite() ? icons[0]: icons[1], null, null, null);
        return textView;
    }
}
