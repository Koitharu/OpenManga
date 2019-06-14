package org.nv95.openmanga.utils;

import org.json.JSONArray;
import org.nv95.openmanga.R;
import org.nv95.openmanga.helpers.ContentShareHelper;
import org.nv95.openmanga.helpers.DirRemoveHelper;
import org.nv95.openmanga.helpers.StorageHelper;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Environment;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

/**
 * Created by nv95 on 14.01.16.
 */
public class BackupRestoreUtil {

    public static final int BACKUP_IMPORT_CODE = 72;

    private final Context mContext;

    public BackupRestoreUtil(Context context) {
        mContext = context;
    }

    public static void showBackupDialog(final Context context) {
        String[] items = new String[]{
                context.getString(R.string.action_history),
                context.getString(R.string.action_favourites)
        };
        final boolean[] checked = new boolean[]{true, true};
        new AlertDialog.Builder(context)
                .setMultiChoiceItems(items, checked, new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                        checked[which] = isChecked;
                    }
                })
                .setTitle(R.string.backup)
                .setNegativeButton(android.R.string.cancel, null)
                .setCancelable(true)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (checked[0] || checked[1]) {
                            new BackupRestoreUtil(context).backup(checked);
                        } else {
                            Toast.makeText(context, R.string.nothing_selected, Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .create().show();
    }

    public static void showRestoreDialog(final Context context) {
        final File[] files = getExternalBackupDir().listFiles();
        String[] items = new String[files.length];
        String name;
        for (int i = 0; i < files.length; i++) {
            name = files[i].getName();
            try {
                items[i] = AppHelper.getReadableDateTime(
                        Long.valueOf(name.substring(0, name.lastIndexOf('.')))
                );
            } catch (Exception e) {
                items[i] = name;
            }
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(context)
                .setTitle(R.string.select_backup)
                .setNegativeButton(android.R.string.cancel, null)
                .setCancelable(true);
        if (items.length != 0) {
            builder.setItems(items, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    new BackupRestoreUtil(context).restore(files[which]);
                }
            });
        } else {
            builder.setMessage(R.string.no_backups);
        }
        if (context instanceof Activity) {
            final Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("file/*");
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            if (context.getPackageManager().resolveActivity(intent, 0) != null) {
                builder.setNeutralButton(R.string.import_file, (dialog, which) -> ((Activity) context).startActivityForResult(
                        Intent.createChooser(intent, context.getString(R.string.import_file)),
                        BACKUP_IMPORT_CODE));
            }
        }
        builder.create().show();
    }

    public static File getExternalBackupDir() {
        File dir = new File(Environment.getExternalStorageDirectory(), ".backup");
        if (!dir.exists()) {
            dir.mkdirs();
        }
        dir = new File(dir, "OpenManga");
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return dir;
    }

    public void backup(boolean[] what) {
        new BackupTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, what[0], what[1]);
    }

    public void restore(File what) {
        new RestoreTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, what);
    }

    private class BackupTask extends AsyncTask<Boolean, Integer, File> {

        private final ProgressDialog processDialog;

        private BackupTask() {
            processDialog = new ProgressDialog(mContext);
            processDialog.setTitle(mContext.getString(R.string.backup));
            processDialog.setMessage(mContext.getString(R.string.preparing));
            processDialog.setIndeterminate(true);
            processDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            processDialog.setCancelable(false);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            processDialog.show();
        }

        @Override
        protected File doInBackground(Boolean... params) {
            int errors = 0;
            JSONArray jsonArray;
            File dir = mContext.getExternalFilesDir("backup");
            if (dir == null) {
                return null;
            }
            if (dir.exists()) {
                new DirRemoveHelper(dir).run();
            }
            dir.mkdir();
            File file;

            StorageHelper storageHelper = new StorageHelper(mContext);
            //backup history
            if (params[0]) {
                publishProgress(R.string.action_history);
                file = new File(dir, "history.json");
                jsonArray = storageHelper.extractTableData("history", null);
                if (jsonArray == null || !writeToFile(file, jsonArray.toString())) {
                    errors++;
                }
            }
            //backup favourites
            if (params[1]) {
                publishProgress(R.string.action_favourites);
                file = new File(dir, "favourites.json");
                jsonArray = storageHelper.extractTableData("favourites", null);
                if (jsonArray == null || !writeToFile(file, jsonArray.toString())) {
                    errors++;
                }
            }
            storageHelper.close();
            publishProgress(R.string.wait);
            String name = System.currentTimeMillis() + ".backup";
            ZipBuilder zipBuilder = null;
            try {
                file = new File(getExternalBackupDir(), name);
                zipBuilder = new ZipBuilder(file);
                zipBuilder.addFiles(dir.listFiles()).build();
            } catch (IOException e) {
                file = null;
            } finally {
                if (zipBuilder != null) {
                    zipBuilder.close();
                }
            }
            new DirRemoveHelper(dir).run();
            return file;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            processDialog.setMessage(mContext.getString(values[0]));
        }

        @Override
        protected void onPostExecute(final File file) {
            super.onPostExecute(file);
            processDialog.dismiss();
            if (file != null) {
                new AlertDialog.Builder(mContext)
                        .setMessage(R.string.backup_done)
                        .setTitle(R.string.backup)
                        .setNegativeButton(R.string.done, null)
                        .setPositiveButton(R.string.export_file,
                                (dialog, which) -> new ContentShareHelper(mContext).exportFile(file))
                        .create().show();
            } else {
                new AlertDialog.Builder(mContext)
                        .setMessage(R.string.error)
                        .setTitle(R.string.backup)
                        .setPositiveButton(R.string.close, null)
                        .create().show();
            }
        }

        private boolean writeToFile(File file, String data) {
            OutputStreamWriter outputStreamWriter = null;
            try {
                outputStreamWriter = new OutputStreamWriter(new FileOutputStream(file));
                outputStreamWriter.write(data);
                outputStreamWriter.close();
                return true;
            } catch (IOException e) {
                return false;
            } finally {
                try {
                    if (outputStreamWriter != null) {
                        outputStreamWriter.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private class RestoreTask extends AsyncTask<File, Integer, Boolean> {

        private final ProgressDialog processDialog;

        private RestoreTask() {
            processDialog = new ProgressDialog(mContext);
            processDialog.setTitle(mContext.getString(R.string.restore));
            processDialog.setMessage(mContext.getString(R.string.preparing));
            processDialog.setIndeterminate(true);
            processDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            processDialog.setCancelable(false);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            processDialog.show();
        }

        @Override
        protected Boolean doInBackground(File... params) {
            int errors = 0;
            File dir = mContext.getExternalFilesDir("restore");
            if (dir == null) {
                return null;
            }
            if (dir.exists()) {
                new DirRemoveHelper(dir).run();
            }
            dir.mkdir();
            File file = new File(dir, "backup.zip");
            try {
                StorageUtils.copyFile(params[0], file);
            } catch (IOException e) {
                return false;
            }
            if (ZipBuilder.unzipFiles(file, dir) == null) {
                new DirRemoveHelper(dir).run();
                return null;
            }
            StorageHelper storageHelper = new StorageHelper(mContext);
            JSONArray data;
            //restore history
            file = new File(dir, "history.json");
            if (file.exists()) {
                publishProgress(R.string.action_history);
                try {
                    data = new JSONArray(readFromFile(file));
                    if (!storageHelper.insertTableData("history", data)) {
                        errors++;
                    }
                } catch (Exception e) {
                    errors++;
                }
            }
            //restore favourites
            file = new File(dir, "favourites.json");
            if (file.exists()) {
                publishProgress(R.string.action_favourites);
                try {
                    data = new JSONArray(readFromFile(file));
                    if (!storageHelper.insertTableData("favourites", data)) {
                        errors++;
                    }
                } catch (Exception e) {
                    errors++;
                }
            }
            storageHelper.close();
            new DirRemoveHelper(dir).run();
            return errors == 0;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            processDialog.dismiss();
            new AlertDialog.Builder(mContext)
                    .setMessage(aBoolean ? R.string.restore_done : R.string.error)
                    .setTitle(R.string.restore)
                    .setPositiveButton(R.string.close, null)
                    .create().show();
        }

        @Nullable
        private String readFromFile(File file) {
            BufferedReader bufferedReader = null;
            try {
                bufferedReader = new BufferedReader(
                        new InputStreamReader(new FileInputStream(file))
                );
                String receiveString = "";
                StringBuilder stringBuilder = new StringBuilder();
                while ((receiveString = bufferedReader.readLine()) != null) {
                    stringBuilder.append(receiveString);
                }
                return stringBuilder.toString();
            } catch (Exception e) {
                return null;
            } finally {
                try {
                    if (bufferedReader != null) {
                        bufferedReader.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
