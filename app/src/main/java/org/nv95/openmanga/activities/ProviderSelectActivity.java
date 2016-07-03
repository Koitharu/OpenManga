package org.nv95.openmanga.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.ListView;

import org.nv95.openmanga.R;
import org.nv95.openmanga.providers.MangaProviderManager;

/**
 * Selecting used manga providers
 * Created by nv95 on 14.10.15.
 */
public class ProviderSelectActivity extends BaseAppActivity implements AdapterView.OnItemClickListener {

    private MangaProviderManager.ProviderSelectAdapter mAdapter;
    private MangaProviderManager mProviderManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_provselect);
        setSupportActionBar(R.id.toolbar);
        ListView listView = (ListView) findViewById(R.id.listView);
        View footerDisclaimer = getLayoutInflater().inflate(R.layout.footer_disclaimer, listView, false);
        assert listView != null;
        listView.addFooterView(footerDisclaimer, null, false);
        mProviderManager = new MangaProviderManager(this);
        mAdapter = mProviderManager.getAdapter();
        listView.setAdapter(mAdapter);
        for (int i = 0; i < mAdapter.getCount(); i++) {
            listView.setItemChecked(i, mProviderManager.isProviderEnabled(mAdapter.getItem(i).name));
        }
        listView.setOnItemClickListener(this);
        enableHomeAsUp();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        CheckBox cb = ((CheckBox) view.findViewById(android.R.id.checkbox));
        cb.setChecked(!cb.isChecked());
        mProviderManager.setProviderEnabled(mAdapter.getItem(position).name, cb.isChecked());
    }
}
