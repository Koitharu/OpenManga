package org.nv95.openmanga;

import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.nv95.openmanga.adapters.UpdatesAdapter;
import org.nv95.openmanga.utils.ErrorReporter;
import org.nv95.openmanga.utils.SerialExecutor;
import org.nv95.openmanga.utils.StorageHelper;
import org.nv95.openmanga.utils.UpdatesChecker;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by nv95 on 03.01.16.
 */
public class UpdatesActivity extends AppCompatActivity implements UpdatesChecker.OnMangaUpdatedListener, AdapterView.OnItemClickListener {
  private final ArrayList<UpdatesChecker.MangaUpdate> list = new ArrayList<>();
  private final SerialExecutor serialExecutor = new SerialExecutor();
  private UpdatesAdapter adapter;
  private ListView listView;
  private ProgressBar progressBar;
  private TextView textViewHolder;

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_updates);
    setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
    ActionBar actionBar = getSupportActionBar();
    if (actionBar != null) {
      actionBar.setDisplayHomeAsUpEnabled(true);
    }
    listView = (ListView) findViewById(R.id.listView);
    progressBar = (ProgressBar) findViewById(R.id.progressBar);
    textViewHolder = (TextView) findViewById(R.id.textView_holder);
    adapter = new UpdatesAdapter(this, list);
    listView.setAdapter(adapter);
    listView.setOnItemClickListener(this);
    UpdatesChecker.CheckUpdates(this, this);
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.updates, menu);
    return super.onCreateOptionsMenu(menu);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case android.R.id.home:
        finish();
        return true;
      case R.id.action_checkall:
        new AlertDialog.Builder(this)
                .setMessage(R.string.mark_all_viewed_confirm)
                .setNegativeButton(android.R.string.no, null)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                  @Override
                  public void onClick(DialogInterface dialog, int which) {
                    for (UpdatesChecker.MangaUpdate o : list) {
                      o.lastChapters = o.chapters;
                    }
                    adapter.notifyDataSetChanged();
                    serialExecutor.execute(new Runnable() {
                      @Override
                      public void run() {
                        markAllAsRead();
                      }
                    });
                  }
                })
                .create().show();
      default:
        return super.onOptionsItemSelected(item);
    }
  }

  @Override
  public void onMangaUpdated(@NonNull UpdatesChecker.MangaUpdate[] updates) {
    list.clear();
    Collections.addAll(list, updates);
    progressBar.setVisibility(View.GONE);
    textViewHolder.setVisibility(updates.length == 0 ? View.VISIBLE : View.GONE);
    adapter.notifyDataSetChanged();
  }

  private void markAllAsRead() {
    StorageHelper storageHelper = null;
    SQLiteDatabase database = null;
    try {
      storageHelper = new StorageHelper(this);
      database = storageHelper.getWritableDatabase();
      database.beginTransaction();
      for (UpdatesChecker.MangaUpdate o : list) {
        ContentValues cv = new ContentValues();
        cv.put("id", o.manga.hashCode());
        cv.put("chapters", o.chapters);
        if (database.update("updates", cv, "id=?", new String[]{String.valueOf(o.manga.hashCode())}) == 0) {
          database.insert("updates", null, cv);
        }
      }
      database.setTransactionSuccessful();
      database.endTransaction();
    } catch (Exception e) {
      ErrorReporter.getInstance().report(e);
    } finally {
      if (database != null) {
        database.close();
      }
      if (storageHelper != null) {
        storageHelper.close();
      }
    }
  }

  @Override
  public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
    UpdatesChecker.MangaUpdate mangaUpdate = list.get(position);
    Intent intent = new Intent(this, MangaPreviewActivity.class);
    intent.putExtras(mangaUpdate.manga.toBundle());
    mangaUpdate.lastChapters = mangaUpdate.chapters;
    adapter.notifyDataSetChanged();
    markAsRead(mangaUpdate);
    startActivity(intent);
  }

  private void markAsRead(final UpdatesChecker.MangaUpdate mangaUpdate) {
    serialExecutor.execute(new Runnable() {
      @Override
      public void run() {
        UpdatesChecker.rememberChaptersCount(UpdatesActivity.this,
                mangaUpdate.manga.hashCode(),
                mangaUpdate.chapters);
      }
    });
  }
}
