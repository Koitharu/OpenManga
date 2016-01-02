package org.nv95.openmanga.utils;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import org.nv95.openmanga.R;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by nv95 on 01.01.16.
 */
public class DirAdapter extends BaseAdapter {
    private final Context context;
    private ArrayList<File> files;
    private File currentDir;

    public DirAdapter(Context context, File dir) {
        this.context = context;
        files = new ArrayList<>();
        setCurrentDir(dir);
    }

    public void setCurrentDir(@NonNull  File dir) {
        currentDir = dir;
        files.clear();
        File[] list = dir.listFiles();
        for (File o:list){
            if (o.isDirectory()) {
                files.add(o);
            }
        }
    }

    @NonNull
    public File getCurrentDir() {
        return currentDir;
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
        return textView;
    }
}
