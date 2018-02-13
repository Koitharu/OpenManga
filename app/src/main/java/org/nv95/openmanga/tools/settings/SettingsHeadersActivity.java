package org.nv95.openmanga.tools.settings;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.AdapterView;

import org.nv95.openmanga.AppBaseActivity;
import org.nv95.openmanga.OpenMangaApp;
import org.nv95.openmanga.R;
import org.nv95.openmanga.tools.settings.providers.ProvidersSettingsActivity;

import java.util.ArrayList;

/**
 * Created by koitharu on 12.01.18.
 */

public final class SettingsHeadersActivity extends AppBaseActivity implements AdapterView.OnItemClickListener {

	private static final int REQUEST_SETTINGS = 12;

	private RecyclerView mRecyclerView;
	private ArrayList<SettingsHeader> mHeaders;
	private SettingsAdapter mAdapter;

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_settings_headers);
		setSupportActionBar(R.id.toolbar);
		enableHomeAsUp();

		mRecyclerView = findViewById(R.id.recyclerView);
		mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
		mRecyclerView.setHasFixedSize(true);

		mHeaders = new ArrayList<>();
		//mHeaders.add(new SettingsHeader(activity, 0, R.string.general, R.drawable.ic_home_white));
		mHeaders.add(new SettingsHeader(this, 1, R.string.appearance, R.drawable.ic_appearance_white));
		mHeaders.add(new SettingsHeader(this, 2, R.string.manga_catalogues, R.drawable.ic_network_white));
		mHeaders.add(new SettingsHeader(this, 3, R.string.downloads, R.drawable.ic_download_white));
		mHeaders.add(new SettingsHeader(this, 4, R.string.action_reading_options, R.drawable.ic_read_white));
		mHeaders.add(new SettingsHeader(this, 5, R.string.checking_new_chapters, R.drawable.ic_notify_new_white));
		mHeaders.add(new SettingsHeader(this, 6, R.string.sync, R.drawable.ic_cloud_sync_white));
		mHeaders.add(new SettingsHeader(this, 7, R.string.additional, R.drawable.ic_braces_white));
		mHeaders.add(new SettingsHeader(this, 8, R.string.help, R.drawable.ic_help_white));

		mAdapter = new SettingsAdapter(mHeaders, this);
		mRecyclerView.setAdapter(mAdapter);
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		SettingsHeader header = mHeaders.get(position);
		Intent intent;
		switch (header.id) {
			case 2:
				intent = new Intent(view.getContext(), ProvidersSettingsActivity.class);
				break;
			case 1:
				intent = new Intent(view.getContext(), SettingsActivity.class)
						.setAction(SettingsActivity.ACTION_SETTINGS_APPEARANCE);
				break;
			case 4:
				intent = new Intent(view.getContext(), SettingsActivity.class)
						.setAction(SettingsActivity.ACTION_SETTINGS_READER);
				break;
			default:
				return;
		}
		startActivityForResult(intent, REQUEST_SETTINGS);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == REQUEST_SETTINGS && resultCode == SettingsActivity.RESULT_RESTART) {
			new AlertDialog.Builder(this)
					.setMessage(R.string.need_restart)
					.setNegativeButton(R.string.postpone, null)
					.setPositiveButton(R.string.restart, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							OpenMangaApp.from(SettingsHeadersActivity.this).restart();
						}
					})
					.create()
					.show();
		}
	}
}
