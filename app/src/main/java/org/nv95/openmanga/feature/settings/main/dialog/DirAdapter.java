package org.nv95.openmanga.feature.settings.main.dialog;

import android.content.Context;
import android.graphics.drawable.Drawable;
import androidx.annotation.NonNull;
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

    private final Context mContext;
    private final ArrayList<File> mFiles;
    private File mCurrentDir;
    private final Drawable[] mIcons;

    public DirAdapter(Context context, File dir) {
        mContext = context;
        mFiles = new ArrayList<>();
        mIcons = LayoutUtils.getThemedIcons(
                context,
                R.drawable.ic_directory_dark,
                R.drawable.ic_directory_null_dark
        );
        setCurrentDir(dir);
    }

    @NonNull
    public File getCurrentDir() {
        return mCurrentDir;
    }

    public void setCurrentDir(@NonNull File dir) {
        mCurrentDir = dir;
        mFiles.clear();
        File[] list = dir.listFiles();
        if (list != null) {
            for (File o : list) {
                if (o.isDirectory()) {
                    mFiles.add(o);
                }
            }
        }
    }

    @Override
    public int getCount() {
        return mFiles.size();
    }

    @Override
    public File getItem(int position) {
        return mFiles.get(position);
    }

    @Override
    public long getItemId(int position) {
        return mFiles.get(position).hashCode();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        TextView textView = (TextView) (convertView == null ? View.inflate(mContext, R.layout.item_dir, null) : convertView);
        File f = getItem(position);
        textView.setText(f.getName());
        textView.setCompoundDrawablesWithIntrinsicBounds(f.canWrite() ? mIcons[0]: mIcons[1], null, null, null);
        return textView;
    }
}
