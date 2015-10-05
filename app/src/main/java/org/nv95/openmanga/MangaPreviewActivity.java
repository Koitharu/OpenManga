package org.nv95.openmanga;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import org.nv95.openmanga.providers.FavouritesProvider;
import org.nv95.openmanga.providers.HistoryProvider;
import org.nv95.openmanga.providers.MangaInfo;
import org.nv95.openmanga.providers.MangaProvider;
import org.nv95.openmanga.providers.MangaSummary;

/**
 * Created by nv95 on 30.09.15.
 */
public class MangaPreviewActivity extends Activity implements View.OnClickListener, DialogInterface.OnClickListener {
    protected MangaSummary mangaSummary;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preview);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);
        mangaSummary = new MangaSummary(new MangaInfo(getIntent().getExtras()));
        ((TextView) findViewById(R.id.textView_title)).setText(mangaSummary.getName());
        ((TextView)findViewById(R.id.textView_summary)).setText(mangaSummary.getSummary());
        findViewById(R.id.button_read).setOnClickListener(this);
        new ImageLoadTask((ImageView) findViewById(R.id.imageView),mangaSummary.getPreview(), false, android.R.drawable.btn_default).execute();
        new LoadInfoTask().execute();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_read:
                //startActivity(new Intent(this,ReadActivity.class).putExtras(mangaSummary.toBundle()));
                showChaptersList();
                break;
        }
    }

    private void showChaptersList() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, mangaSummary.getChapters().getNames()), this);
        builder.setTitle(R.string.chapters_list);
        builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.create().show();
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        HistoryProvider.addToHistory(this, mangaSummary);
        startActivity(new Intent(this, ReadActivity.class).putExtra("chapter", which).putExtras(mangaSummary.toBundle()));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_preview, menu);
        if (FavouritesProvider.has(this, mangaSummary))
            menu.findItem(R.id.action_favourite).setIcon(R.drawable.ic_action_action_favorite);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_favourite:
                FavouritesProvider favouritesProvider = new FavouritesProvider(this);
                if (favouritesProvider.has(mangaSummary)) {
                    if (favouritesProvider.remove(mangaSummary)) {
                        item.setIcon(R.drawable.ic_action_action_favorite_outline);
                    }
                } else {
                    if (favouritesProvider.add(mangaSummary)) {
                        item.setIcon(R.drawable.ic_action_action_favorite);
                    }
                }
                return true;

            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private class LoadInfoTask extends AsyncTask<Void,Void,MangaSummary> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(MangaSummary mangaSummary) {
            super.onPostExecute(mangaSummary);
            MangaPreviewActivity.this.mangaSummary = mangaSummary;
            findViewById(R.id.progressBar).setVisibility(View.GONE);
            findViewById(R.id.button_read).setEnabled(true);
            ((TextView)findViewById(R.id.textView_description)).setText(mangaSummary.getDescription());
            new ImageLoadTask((ImageView) findViewById(R.id.imageView),mangaSummary.getPreview(), false, 0).execute();
        }

        @Override
        protected MangaSummary doInBackground(Void... params) {
            try {
                MangaProvider provider = (MangaProvider) mangaSummary.getProvider().newInstance();
                return provider.getDetailedInfo(mangaSummary);
            } catch (Exception ignored) {
                return new MangaSummary(mangaSummary);
            }
        }
    }
}
