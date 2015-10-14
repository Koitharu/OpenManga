package org.nv95.openmanga;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.nv95.openmanga.components.InlayoutNotify;
import org.nv95.openmanga.providers.MangaProviderManager;

/**
 * Created by nv95 on 14.10.15.
 */
public class ProviderSelectActivity extends Activity implements AdapterView.OnItemClickListener {
    private ListView listView;
    private ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_provselect);
        ((InlayoutNotify)findViewById(R.id.disclaimer_notify)).setText(R.string.disclaimer);
        listView = (ListView) findViewById(R.id.listView);
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_multiple_choice, MangaProviderManager.allProviders);
        listView.setAdapter(adapter);
        SharedPreferences prefs = getSharedPreferences("providers", MODE_PRIVATE);
        for (int i=0; i<adapter.getCount(); i++) {
            listView.setItemChecked(i, prefs.getBoolean(adapter.getItem(i), true));
        }
        listView.setOnItemClickListener(this);
        getActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        getSharedPreferences("providers", MODE_PRIVATE).edit().putBoolean(adapter.getItem(position), listView.isItemChecked(position)).apply();
    }
}
