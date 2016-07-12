package org.nv95.openmanga.activities;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import org.nv95.openmanga.R;
import org.nv95.openmanga.adapters.ProvidersAdapter;
import org.nv95.openmanga.components.DividerItemDecoration;

/**
 * Selecting used manga providers
 * Created by nv95 on 14.10.15.
 */
public class ProviderSelectActivity extends BaseAppActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_provselect);
        setSupportActionBar(R.id.toolbar);
        enableHomeAsUp();

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        assert recyclerView != null;
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(new ProvidersAdapter(this));
        recyclerView.addItemDecoration(new DividerItemDecoration(this));
    }
}
