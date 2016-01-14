package org.nv95.openmanga.utils;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;

import org.json.JSONArray;
import org.nv95.openmanga.Constants;
import org.nv95.openmanga.R;
import org.nv95.openmanga.providers.LocalMangaProvider;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

/**
 * Created by nv95 on 14.01.16.
 */
public class BackupHelper {
  private final Context context;

  public static void BackupDialog(final Context context) {
    String[] items = new String[] {
      context.getString(R.string.action_history),
      context.getString(R.string.action_favourites)
    };
    final boolean[] checked = new boolean[] {true, true};
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
                new BackupHelper(context).backup(checked);
              }
            })
            .create().show();
  }

  public static void RestoreDialog(final Context context) {
    final File[] files = getExternalBackupDir().listFiles();
    String[] items = new String[files.length];
    String name;
    for (int i = 0;i < files.length;i++) {
      name = files[i].getName();
      try {
         items[i] = AppHelper.getReadableDateTime(
                 Long.valueOf(name.substring(0, name.lastIndexOf('.')))
         );
      } catch (Exception e) {
        items[i] = name;
      }
    }
    new AlertDialog.Builder(context)
            .setItems(items, new DialogInterface.OnClickListener() {
              @Override
              public void onClick(DialogInterface dialog, int which) {
                new BackupHelper(context).restore(files[which]);
              }
            })
            .setTitle(R.string.restore)
            .setNegativeButton(android.R.string.cancel, null)
            .setCancelable(true)
            .create().show();
  }

  public static File getExternalBackupDir() {
    File dir = new File(Environment.getExternalStorageDirectory(), ".backups");
    if (!dir.exists()) {
      dir.mkdirs();
    }
    dir = new File(dir, "OpenManga");
    if (!dir.exists()) {
      dir.mkdirs();
    }
    return dir;
  }

  private BackupHelper(Context context) {
    this.context = context;
  }

  public void backup(boolean[] what) {
    new BackupTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, false, what[0], what[1]);
  }

  public void restore(File what) {
    new RestoreTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, what);
  }

  private class BackupTask extends AsyncTask<Boolean,Integer,File> {
    private final ProgressDialog processDialog;

    private BackupTask() {
      processDialog = new ProgressDialog(context);
      processDialog.setTitle(context.getString(R.string.backup));
      processDialog.setMessage(context.getString(R.string.preparing));
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
      File dir = context.getExternalFilesDir("backup");
      if (dir == null) {
        return null;
      }
      if (dir.exists()) {
        new FileRemover(dir).run();
      }
      dir.mkdir();
      File file;

      StorageHelper storageHelper = new StorageHelper(context);
      //backup history
      if (params[Constants.CATEGORY_HISTORY]) {
        publishProgress(R.string.action_history);
        file = new File(dir, "history.json");
        jsonArray = storageHelper.extractTableData("history");
        if (jsonArray == null || !writeToFile(file, jsonArray.toString())) {
          errors++;
        }
      }
      //backup favourites
      if (params[Constants.CATEGORY_FAVOURITES]) {
        publishProgress(R.string.action_favourites);
        file = new File(dir, "favourites.json");
        jsonArray = storageHelper.extractTableData("favourites");
        if (jsonArray == null || !writeToFile(file, jsonArray.toString())) {
          errors++;
        }
        file = new File(dir, "updates.json");
        jsonArray = storageHelper.extractTableData("updates");
        if (jsonArray == null || !writeToFile(file, jsonArray.toString())) {
          errors++;
        }
      }
      storageHelper.close();
      publishProgress(R.string.wait);
      String name = System.currentTimeMillis() + ".backup";
      try {
        file = new File(getExternalBackupDir(), name);
        new ZipBuilder(file).addFiles(dir.listFiles()).build();
      } catch (IOException e) {
        file = null;
      }
      new FileRemover(dir).run();
      return file;
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
      super.onProgressUpdate(values);
      processDialog.setMessage(context.getString(values[0]));
    }

    @Override
    protected void onPostExecute(final File file) {
      super.onPostExecute(file);
      processDialog.dismiss();
      if (file != null) {
        new AlertDialog.Builder(context)
                .setMessage(R.string.backup_done)
                .setTitle(R.string.backup)
                .setNegativeButton(R.string.done, null)
                .setPositiveButton(R.string.send, new DialogInterface.OnClickListener() {
                  @Override
                  public void onClick(DialogInterface dialog, int which) {
                    context.startActivity(Intent.createChooser(
                            new Intent(Intent.ACTION_SEND)
                                    .setType("file/*")
                                    .putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file)),
                            context.getString(R.string.export)
                    ));
                  }
                })
                .create().show();
      } else {
        new AlertDialog.Builder(context)
                .setMessage(R.string.error)
                .setTitle(R.string.backup)
                .setPositiveButton(R.string.close, null)
                .create().show();
      }
    }

    private boolean writeToFile(File file, String data) {
      try {
        OutputStreamWriter outputStreamWriter = new OutputStreamWriter(new FileOutputStream(file));
        outputStreamWriter.write(data);
        outputStreamWriter.close();
        return true;
      } catch (IOException e) {
        return false;
      }
    }
  }

  private class RestoreTask extends AsyncTask<File,Integer,Boolean> {
    private final ProgressDialog processDialog;

    private RestoreTask() {
      processDialog = new ProgressDialog(context);
      processDialog.setTitle(context.getString(R.string.restore));
      processDialog.setMessage(context.getString(R.string.preparing));
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
      File dir = context.getExternalFilesDir("restore");
      if (dir == null) {
        return null;
      }
      if (dir.exists()) {
        new FileRemover(dir).run();
      }
      dir.mkdir();
      File file = new File(dir, "backup.zip");
      try {
        LocalMangaProvider.CopyFile(params[0], file);
      } catch (IOException e) {
        return false;
      }
      if (ZipBuilder.UnzipFile(file, dir) == null) {
        new FileRemover(dir).run();
        return null;
      }
      StorageHelper storageHelper = new StorageHelper(context);
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
      new AlertDialog.Builder(context)
              .setMessage(aBoolean ? R.string.restore_done : R.string.error)
              .setTitle(R.string.restore)
              .setPositiveButton(R.string.close, null)
              .create().show();
      MangaChangesObserver.emitChanging(Constants.CATEGORY_HISTORY);
      MangaChangesObserver.emitChanging(Constants.CATEGORY_FAVOURITES);
    }

    @Nullable
    private String readFromFile(File file) {
      try {
        BufferedReader bufferedReader = new BufferedReader(
                new InputStreamReader(new FileInputStream(file))
        );
        String receiveString = "";
        StringBuilder stringBuilder = new StringBuilder();
        while ( (receiveString = bufferedReader.readLine()) != null ) {
          stringBuilder.append(receiveString);
        }
        bufferedReader.close();
        return stringBuilder.toString();
      } catch (Exception e) {
        return null;
      }
    }
  }
}
