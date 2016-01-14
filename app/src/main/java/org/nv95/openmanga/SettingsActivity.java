package org.nv95.openmanga;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.Toast;

import org.nv95.openmanga.adapters.SearchHistoryAdapter;
import org.nv95.openmanga.providers.LocalMangaProvider;
import org.nv95.openmanga.utils.BackupHelper;
import org.nv95.openmanga.utils.ErrorReporter;
import org.nv95.openmanga.utils.FileRemover;

import java.io.File;

/**
 * Created by nv95 on 03.10.15.
 * Activity with settings fragments
 */
public class SettingsActivity extends AppCompatActivity implements Preference.OnPreferenceClickListener {
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_settings);
    setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
    ActionBar actionBar = getSupportActionBar();
    if (actionBar != null) {
      actionBar.setDisplayHomeAsUpEnabled(true);
    }

    getFragmentManager().beginTransaction()
            .replace(R.id.content, new CommonSettingsFragment())
            .commit();

  }


  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    if (item.getItemId() == android.R.id.home && !getFragmentManager().popBackStackImmediate()) {
      finish();
    }
    return super.onOptionsItemSelected(item);
  }

  @Override
  public boolean onPreferenceClick(Preference preference) {
    switch (preference.getKey()) {
      case "readeropt":
        new ReaderOptionsDialog(this).show();
        return true;
      case "srcselect":
        startActivity(new Intent(this, ProviderSelectActivity.class));
        return true;
      case "bugreport":
        ErrorReporter.sendLog(this);
        return true;
      case "csearchhist":
        SearchHistoryAdapter.clearHistory(this);
        Toast.makeText(this, R.string.done, Toast.LENGTH_SHORT).show();
        return true;
      case "about":
        startActivity(new Intent(this, AboutActivity.class));
        return true;
      case "backup":
        BackupHelper.BackupDialog(this);
        return true;
      case "ccache":
        new CacheClearTask(preference).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        return true;
      default:
        return false;
    }
  }

  @Override
  public void onBackPressed() {
    if (!getFragmentManager().popBackStackImmediate()) {
      super.onBackPressed();
    }
  }

  public static class CommonSettingsFragment extends PreferenceFragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);

      // Load the preferences from an XML resource
      addPreferencesFromResource(R.xml.pref_common);
    }

    @Override
    public void onResume() {
      super.onResume();
      findPreference("chupd").setSummary(
              getPreferenceManager().getSharedPreferences().getBoolean("chupd", false) ?
                      R.string.enabled : R.string.disabled
      );
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
      super.onActivityCreated(savedInstanceState);
      Activity activity = getActivity();
      findPreference("srcselect").setOnPreferenceClickListener((Preference.OnPreferenceClickListener) activity);
      findPreference("readeropt").setOnPreferenceClickListener((Preference.OnPreferenceClickListener) activity);
      findPreference("ccache").setOnPreferenceClickListener((Preference.OnPreferenceClickListener) activity);
      findPreference("csearchhist").setOnPreferenceClickListener((Preference.OnPreferenceClickListener) activity);
      findPreference("backup").setOnPreferenceClickListener((Preference.OnPreferenceClickListener) activity);
      findPreference("bugreport").setOnPreferenceClickListener((Preference.OnPreferenceClickListener) activity);

      Preference p = findPreference("about");
      p.setOnPreferenceClickListener((Preference.OnPreferenceClickListener) getActivity());
      String version;
      try {
        version = activity.getPackageManager().getPackageInfo(activity.getPackageName(), 0).versionName;
      } catch (PackageManager.NameNotFoundException e) {
        version = "unknown";
      }
      p.setSummary(String.format(activity.getString(R.string.version), version));

      p = findPreference("mangadir");
      p.setSummary(LocalMangaProvider.getMangaDir(getActivity()).getPath());
      p.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
        @Override
        public boolean onPreferenceClick(final Preference preference) {
          new DirSelectDialog(getActivity())
                  .setDirSelectListener(new DirSelectDialog.OnDirSelectListener() {
                    @Override
                    public void onDirSelected(File dir) {
                      if (!dir.canWrite()) {
                        Toast.makeText(getActivity(), R.string.dir_no_access, Toast.LENGTH_SHORT).show();
                      }
                      preference.setSummary(dir.getPath());
                      preference.getEditor()
                              .putString("mangadir", dir.getPath()).apply();
                    }
                  })
                  .show();
          return true;
        }
      });

      new AsyncTask<Void, Void, Float>() {

        @Override
        protected void onPreExecute() {
          super.onPreExecute();
          findPreference("ccache").setSummary(R.string.size_calculating);
        }

        @Override
        protected Float doInBackground(Void... params) {
          return LocalMangaProvider.DirSize(getActivity().getExternalCacheDir()) / 1048576f;
        }

        @Override
        protected void onPostExecute(Float aFloat) {
          super.onPostExecute(aFloat);
          Preference preference = findPreference("ccache");
          if (preference != null) {
            preference.setSummary(String.format(preference.getContext().getString(R.string.cache_size), aFloat));
          }
        }
      }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }
  }

  private static class CacheClearTask extends AsyncTask<Void, Void, Void> {
    private Preference preference;

    public CacheClearTask(Preference preference) {
      this.preference = preference;
    }

    @Override
    protected void onPreExecute() {
      preference.setSummary(R.string.cache_clearing);
      super.onPreExecute();
    }

    @Override
    protected Void doInBackground(Void... params) {
      File dir = preference.getContext().getExternalCacheDir();
      new FileRemover(dir).run();
      return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
      preference.setSummary(String.format(preference.getContext().getString(R.string.cache_size), 0f));
      super.onPostExecute(aVoid);
    }
  }
}
