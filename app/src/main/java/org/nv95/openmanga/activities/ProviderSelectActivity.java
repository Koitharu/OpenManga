package org.nv95.openmanga.activities;

import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.ListView;

import org.nv95.openmanga.R;
import org.nv95.openmanga.components.InlayoutNotify;
import org.nv95.openmanga.providers.MangaProviderManager;

/**
 * Created by nv95 on 14.10.15.
 */
public class ProviderSelectActivity extends BaseAppActivity implements AdapterView.OnItemClickListener {
    private ListView listView;
    private MangaProviderManager.ProviderSelectAdapter adapter;
    private MangaProviderManager providerManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_provselect);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        ((InlayoutNotify) findViewById(R.id.disclaimer_notify)).setText(R.string.disclaimer);
        listView = (ListView) findViewById(R.id.listView);
        providerManager = new MangaProviderManager(this);
        adapter = providerManager.getAdapter();
        listView.setAdapter(adapter);
        for (int i = 0; i < adapter.getCount(); i++) {
            listView.setItemChecked(i, providerManager.isProviderEnabled(adapter.getItem(i).name));
        }
        listView.setOnItemClickListener(this);
        enableHomeAsUp();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        CheckBox cb = ((CheckBox) view.findViewById(android.R.id.checkbox));
        cb.setChecked(!cb.isChecked());
        providerManager.setProviderEnabled(adapter.getItem(position).name, cb.isChecked());
    }
}
