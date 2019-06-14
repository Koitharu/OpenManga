package org.nv95.openmanga.feature.settings.main.dialog;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.PowerManager;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import android.text.format.Formatter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.nv95.openmanga.R;
import org.nv95.openmanga.items.LocalMangaInfo;
import org.nv95.openmanga.providers.LocalMangaProvider;
import org.nv95.openmanga.utils.MangaStore;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by nv95 on 02.07.16.
 */

public class LocalMoveDialog {

    private final Context mContext;
    private final long[] mIds;
    @Nullable
    private String mDestinaton = null;

    public LocalMoveDialog(Context context, long... ids) {
        mContext = context;
        mIds = ids;
    }

    public LocalMoveDialog setDestination(String path) {
        mDestinaton = path;
        return this;
    }

    public void showSelectSource(@Nullable String exclude) {
        new LoadDataTask(exclude).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public void showSelectDestination(final LocalMangaInfo[] mangas) {
        new DirSelectDialog(mContext)
                .setDirSelectListener(new DirSelectDialog.OnDirSelectListener() {
                    @Override
                    public void onDirSelected(File dir) {
                        mDestinaton = dir.getPath();
                        showMove(mangas);
                    }
                }).show();
    }

    public void showMove(LocalMangaInfo[] mangas) {
        new MoveMangaTask(mDestinaton).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, mangas);
    }

    private class MoveMangaTask extends AsyncTask<LocalMangaInfo, Object, Long> {

        private final ProgressDialog mProgressDialog;
        private PowerManager.WakeLock mWakeLock;
        private final String mDest;

        MoveMangaTask(String dest) {
            mDest = dest;
            mWakeLock = ((PowerManager) mContext.getSystemService(Context.POWER_SERVICE)).newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "Moving manga");
            mProgressDialog = new ProgressDialog(mContext);
            mProgressDialog.setTitle(R.string.moving_files);
            mProgressDialog.setMessage(mContext.getString(R.string.loading));
            mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            mProgressDialog.setCancelable(false);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mProgressDialog.show();
            mWakeLock.acquire();
        }

        @Override
        protected Long doInBackground(LocalMangaInfo... params) {
            MangaStore store = new MangaStore(mContext);
            long totalSize = 0;
            for (int i = 0; i < params.length; i++) {
                publishProgress(i, params.length, params[i].name);
                if (params[i].path.equals(mDest)) {
                    continue;
                }
                if (!store.moveManga(params[i].id, mDest)) {
                    return null;
                }
                totalSize += params[i].size;
            }
            return totalSize;
        }

        @Override
        protected void onProgressUpdate(Object... values) {
            super.onProgressUpdate(values);
            mProgressDialog.setMax((Integer) values[1]);
            mProgressDialog.setProgress((Integer) values[0]);
            mProgressDialog.setMessage((CharSequence) values[2]);
        }

        @Override
        protected void onPostExecute(Long aLong) {
            super.onPostExecute(aLong);
            mProgressDialog.dismiss();
            mWakeLock.release();
            String msg = aLong == null ?
                    mContext.getString(R.string.error) : mContext.getString(
                    R.string.mangas_moved_done, mDest,
                    Formatter.formatFileSize(mContext, aLong)
            );
            new AlertDialog.Builder(mContext)
                    .setMessage(msg)
                    .setPositiveButton(android.R.string.ok, null)
                    .setCancelable(true)
                    .create().show();
        }
    }

    private class LoadDataTask extends AsyncTask<Void, Void, List<LocalMangaInfo>> {

        private final ProgressDialog mProgressDialog;
        private final String mExclude;

        LoadDataTask(String exclude) {
            mExclude = exclude;
            mProgressDialog = new ProgressDialog(mContext);
            mProgressDialog.setMessage(mContext.getString(R.string.loading));
            mProgressDialog.setIndeterminate(true);
            mProgressDialog.setCancelable(false);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mProgressDialog.show();
        }

        @Override
        protected List<LocalMangaInfo> doInBackground(Void... params) {
            LocalMangaInfo[] infos = LocalMangaProvider.getInstance(mContext)
                    .getLocalInfo(mIds);
            ArrayList<LocalMangaInfo> res = new ArrayList<>();
            for (LocalMangaInfo o : infos) {
                if (o != null && !o.path.equals(mExclude)) {
                    res.add(o);
                }
            }
            return res;
        }

        @Override
        protected void onPostExecute(List<LocalMangaInfo> localMangaInfos) {
            super.onPostExecute(localMangaInfos);
            mProgressDialog.dismiss();
            if (localMangaInfos.size() == 0) {
                Toast.makeText(mContext, R.string.no_manga_found, Toast.LENGTH_SHORT).show();
                return;
            }
            final SelectAdapter adapter = new SelectAdapter(mContext, localMangaInfos);
            ListView listView = new ListView(mContext);
            listView.setAdapter(adapter);
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    adapter.toggle(position);
                }
            });
            new AlertDialog.Builder(mContext)
                    .setView(listView)
                    .setTitle(R.string.move_saved)
                    .setPositiveButton(R.string.next, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            LocalMangaInfo[] items = adapter.getCheckedItems();
                            if (items.length == 0) {
                                Toast.makeText(mContext, R.string.nothing_selected, Toast.LENGTH_SHORT).show();
                            } else {
                                if (mDestinaton == null) {
                                    showSelectDestination(items);
                                } else {
                                    showMove(items);
                                }
                            }
                        }
                    })
                    .setNegativeButton(android.R.string.cancel, null)
                    .setCancelable(true)
                    .create().show();
        }
    }

    private static class SelectAdapter extends ArrayAdapter<LocalMangaInfo> {

        final boolean[] mChecked;

        SelectAdapter(Context context, List<LocalMangaInfo> objects) {
            super(context, R.layout.item_adapter_checkable, objects);
            mChecked = new boolean[objects.size()];
            Arrays.fill(mChecked, true);
        }

        @NonNull
        @SuppressLint("SetTextI18n")
        @Override
        public View getView(int position, View convertView, @NonNull ViewGroup parent) {
            View v = convertView != null ? convertView : LayoutInflater.from(getContext())
                    .inflate(R.layout.item_adapter_checkable, parent, false);
            LocalMangaInfo item = getItem(position);
            ((TextView) v.findViewById(android.R.id.text1)).setText(item.name);
            ((TextView) v.findViewById(android.R.id.text2)).setText(
                    new File(item.path).getParent()
                            + "\n"
                            + Formatter.formatFileSize(getContext(), item.size)
            );
            ((CheckBox) v.findViewById(android.R.id.checkbox)).setChecked(mChecked[position]);
            return v;
        }

        public LocalMangaInfo[] getCheckedItems() {
            ArrayList<LocalMangaInfo> list = new ArrayList<>();
            for (int i = 0; i < getCount(); i++) {
                if (mChecked[i]) {
                    list.add(getItem(i));
                }
            }
            return list.toArray(new LocalMangaInfo[list.size()]);
        }

        public void toggle(int position) {
            mChecked[position] = !mChecked[position];
            notifyDataSetChanged();
        }
    }
}
