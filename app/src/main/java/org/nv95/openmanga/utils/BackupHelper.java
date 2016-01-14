package org.nv95.openmanga.utils;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.widget.Toast;

import org.json.JSONArray;
import org.nv95.openmanga.Constants;
import org.nv95.openmanga.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
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

  private BackupHelper(Context context) {
    this.context = context;
  }

  private void backup(boolean[] what) {
    new BackupTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, false, what[0], what[1]);
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
      try {
        file = new File(context.getExternalFilesDir("temp"), "openmanga.backup");
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
    protected void onPostExecute(File file) {
      super.onPostExecute(file);
      processDialog.dismiss();
      if (file != null) {
        Toast.makeText(context, R.string.backup_done, Toast.LENGTH_SHORT).show();
        context.startActivity(Intent.createChooser(
                new Intent(Intent.ACTION_SEND)
                        .setType("file/*")
                        .putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file)),
                context.getString(R.string.export)
        ));
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
}
