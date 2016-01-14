package org.nv95.openmanga;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ExpandableListView;

import org.nv95.openmanga.adapters.MultipleSearchAdapter;
import org.nv95.openmanga.items.MangaInfo;

/**
 * Created by nv95 on 12.01.16.
 */
public class MultipleSearchActivity extends AppCompatActivity implements AdapterView.OnItemClickListener, ExpandableListView.OnChildClickListener {
  private String query;
  private MultipleSearchAdapter adapter;
  private ExpandableListView expandableListView;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_multisearch);
    setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
    query = getIntent().getStringExtra("query");
    ActionBar actionBar = getSupportActionBar();
    if (actionBar != null) {
      actionBar.setDisplayHomeAsUpEnabled(true);
      actionBar.setHomeButtonEnabled(true);
      actionBar.setSubtitle(query);
    }
    expandableListView = (ExpandableListView) findViewById(R.id.expandableListView);
    adapter = new MultipleSearchAdapter(this, query, this);
    expandableListView.setAdapter(adapter);
    expandableListView.setOnChildClickListener(this);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    if (item.getItemId() == android.R.id.home)
      finish();
    return super.onOptionsItemSelected(item);
  }

  @Override
  public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
    startActivity(new Intent(MultipleSearchActivity.this, SearchActivity.class)
            .putExtra("query", query)
            .putExtra("title", adapter.getGroup(position).name)
            .putExtra("provider", position));
  }

  @Override
  public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
    Intent intent = new Intent(this, MangaPreviewActivity.class);
    MangaInfo info = adapter.getChild(groupPosition, childPosition);
    intent.putExtras(info.toBundle());
    startActivity(intent);
    return true;
  }
}
