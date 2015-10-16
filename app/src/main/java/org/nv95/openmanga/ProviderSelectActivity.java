package org.nv95.openmanga;

import android.app.Activity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.ListView;

import org.nv95.openmanga.components.InlayoutNotify;
import org.nv95.openmanga.providers.MangaProviderManager;

/**
 * Created by nv95 on 14.10.15.
 */
public class ProviderSelectActivity extends Activity implements AdapterView.OnItemClickListener {
    private ListView listView;
    private MangaProviderManager.ProviderSelectAdapter adapter;
    private MangaProviderManager providerManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_provselect);
        ((InlayoutNotify)findViewById(R.id.disclaimer_notify)).setText(R.string.disclaimer);
        listView = (ListView) findViewById(R.id.listView);
        providerManager = new MangaProviderManager(this);
        adapter = providerManager.getAdapter();
        listView.setAdapter(adapter);
        for (int i=0; i<adapter.getCount(); i++) {
            listView.setItemChecked(i, providerManager.isProviderEnabled(adapter.getItem(i)));
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
        CheckBox cb = ((CheckBox)view.findViewById(android.R.id.checkbox));
        cb.setChecked(!cb.isChecked());
        providerManager.setProviderEnabled(adapter.getItem(position), cb.isChecked());
    }
}
