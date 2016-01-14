package org.nv95.openmanga;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import org.nv95.openmanga.fragments.MangaListFragment;
import org.nv95.openmanga.items.MangaList;
import org.nv95.openmanga.providers.MangaProvider;

/**
 * Created by nv95 on 01.10.15.
 */
public class SearchActivity extends AppCompatActivity implements
        MangaListFragment.MangaListListener, DialogInterface.OnClickListener {
  private MangaListFragment listFragment;
  private String query;
  @Nullable
  private String title;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_search);
    setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
    listFragment = new MangaListFragment();
    listFragment.setArguments(getIntent().getExtras());
    getFragmentManager().beginTransaction().add(R.id.frame_main, listFragment).commit();
    query = getIntent().getStringExtra("query");
    title = getIntent().getStringExtra("title");
    if (title != null) {
      setTitle(query);
    }
    ActionBar actionBar = getSupportActionBar();
    if (actionBar != null) {
      actionBar.setDisplayHomeAsUpEnabled(true);
      actionBar.setHomeButtonEnabled(true);
      actionBar.setSubtitle(title == null ? query : title);
    }
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.menu_search, menu);
    menu.findItem(R.id.action_search).setVisible(title == null);
    return super.onCreateOptionsMenu(menu);
  }


  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    if (item.getItemId() == android.R.id.home) {
      finish();
    } else if (item.getItemId() == R.id.action_search) {
      onClick(null, 0);
    }
    return super.onOptionsItemSelected(item);
  }

  @Override
  public MangaList onListNeeded(MangaProvider provider, int page) throws Exception {
    return query == null ? MangaList.Empty() : provider.search(query, page);
  }

  @Override
  public String onEmptyList(MangaProvider provider) {
    if (title == null) {
      new AlertDialog.Builder(this)
              .setMessage(R.string.search_more_confirm)
              .setPositiveButton(android.R.string.ok, this)
              .setCancelable(true)
              .setNegativeButton(android.R.string.no, null)
              .create().show();
    }
    return getString(R.string.no_manga_found);
  }

  @Override
  public void onClick(DialogInterface dialog, int which) {
    startActivity(new Intent(SearchActivity.this, MultipleSearchActivity.class)
            .putExtra("query", query));
  }
}
