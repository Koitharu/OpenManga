package org.nv95.openmanga.adapters;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.nv95.openmanga.DirSelectDialog;
import org.nv95.openmanga.R;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * Created by nv95 on 09.02.16.
 */
public class FileSelectAdapter extends RecyclerView.Adapter<FileSelectAdapter.FileViewHolder>
    implements FileHolderCallback{

    private String mPattern;
    private final DirSelectDialog.OnDirSelectListener mCallback;
    private File mCurrentDir;
    private final ArrayList<File> files;

    public FileSelectAdapter(File currentDir, String pattern,
                             DirSelectDialog.OnDirSelectListener callback) {
        files = new ArrayList<>();
        mCallback = callback;
        mPattern = pattern;
        setCurrentDir(currentDir);
    }

    @NonNull
    public File getCurrentDir() {
        return mCurrentDir;
    }

    public void setCurrentDir(@NonNull File dir) {
        mCurrentDir = dir;
        files.clear();
        File[] list = dir.listFiles();
        for (File o : list) {
            if (o.isDirectory() || o.getName().toLowerCase().endsWith(mPattern)) {
                files.add(o);
            }
        }
        Collections.sort(files, new FileSortComparator());
        files.add(0, new File(dir, ".."));
        notifyDataSetChanged();
    }

    public boolean toParentDir() {
        File parentDir = mCurrentDir.getParentFile();
        if (parentDir == null || !parentDir.canRead()) {
            return false;
        }
        files.clear();
        File[] list = parentDir.listFiles();
        for (File o : list) {
            if (o.isDirectory() || o.getName().toLowerCase().endsWith(mPattern)) {
                files.add(o);
            }
        }
        Collections.sort(files, new FileSortComparator());
        files.add(0, new File(parentDir, ".."));
        int position = files.indexOf(mCurrentDir);
        mCurrentDir = parentDir;
        notifyDataSetChanged();
        return true;
    }

    public boolean setDirectory(File directory) {
        if (!directory.canRead()) {
            return false;
        }
        int position = files.indexOf(directory);
        if (position == -1) {
            setCurrentDir(directory);
            return true;
        }
        if (directory.getName().equals("..")) {
            return toParentDir();
        }
        files.clear();
        File[] list = directory.listFiles();
        for (File o : list) {
            if (o.isDirectory() || o.getName().toLowerCase().endsWith(mPattern)) {
                files.add(o);
            }
        }
        Collections.sort(files, new FileSortComparator());
        files.add(0, new File(directory, ".."));
        mCurrentDir = directory;
        notifyDataSetChanged();
        return true;
    }

    @Override
    public FileViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new FileViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_dir, parent, false), this);
    }

    @Override
    public void onBindViewHolder(FileViewHolder holder, int position) {
        holder.fill(files.get(position));
    }

    @Override
    public int getItemCount() {
        return files.size();
    }

    @Override
    public void onItemClick(FileViewHolder holder) {
        mCallback.onDirSelected(holder.mFile);
    }

    public static class FileViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private File mFile;
        private final TextView mTextView;
        private final FileHolderCallback mCallback;

        public FileViewHolder(View itemView, FileHolderCallback callback) {
            super(itemView);
            mCallback = callback;
            mTextView = (TextView) itemView;
            itemView.setOnClickListener(this);
        }

        protected void fill(File file) {
            mFile = file;
            mTextView.setText(file.getName());
            int icon;
            if ("..".endsWith(file.getName())) {
                icon = R.drawable.ic_return_dark;
            } else if (file.isDirectory()) {
                icon = file.canRead() ? R.drawable.ic_directory_dark : R.drawable.ic_directory_null_dark;
            } else {
                icon = R.drawable.ic_file_dark;
            }
            mTextView.setCompoundDrawablesWithIntrinsicBounds(icon, 0, 0, 0);
        }

        @Override
        public void onClick(View v) {
            mCallback.onItemClick(this);
        }
    }

    protected class FileSortComparator implements Comparator<File> {

        @Override
        public int compare(File lhs, File rhs) {
            if (lhs.isDirectory())
                return rhs.isDirectory() ? lhs.compareTo(rhs) : -1;
            else if (rhs.isDirectory())
                return 1;
            return lhs.compareTo(rhs);
        }
    }


}
