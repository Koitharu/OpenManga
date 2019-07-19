package org.nv95.openmanga.feature.welcome;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Html;
import android.view.View;
import android.widget.Button;
import android.widget.Checkable;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.ViewCompat;

import org.nv95.openmanga.BuildConfig;
import org.nv95.openmanga.R;
import org.nv95.openmanga.core.activities.BaseAppActivity;
import org.nv95.openmanga.feature.settings.main.SettingsActivity2;
import org.nv95.openmanga.feature.settings.main.dialog.DirSelectDialog;
import org.nv95.openmanga.feature.settings.main.dialog.StorageSelectDialog;
import org.nv95.openmanga.providers.staff.Providers;
import org.nv95.openmanga.utils.AppHelper;
import org.nv95.openmanga.utils.BackupRestoreUtil;
import org.nv95.openmanga.utils.MangaStore;

import java.io.File;

/**
 * Created by nv95 on 18.10.15.
 */
public class WelcomeActivity extends BaseAppActivity {

    public static final int REQUEST_ONBOARDING = 131;
    public static final int REQUEST_SOURCES = 132;

    private static final int WELCOME_CHANGELOG = 1;
    private static final int WELCOME_ONBOARDING = 2;

    private TextView mTextViewSources;
    private TextView mTextViewTheme;
    private TextView mTextViewStorage;

    /**
     *
     * @param context
     * @return true if first call
     */
    public static boolean show(Activity context) {
        SharedPreferences prefs = context.getSharedPreferences(WelcomeActivity.class.getName(), MODE_PRIVATE);
        int version = BuildConfig.VERSION_CODE;
        int lastVersion = prefs.getInt("version", -1);
        if (lastVersion == -1) {
            context.startActivityForResult(
                    new Intent(context, WelcomeActivity.class)
                            .putExtra("mode", WELCOME_ONBOARDING),
                    REQUEST_ONBOARDING
            );
        }
        prefs.edit().putInt("version", version).apply();
        return lastVersion == -1;
    }

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //init view
        mTextViewSources = (TextView) findViewById(R.id.textViewSources);
        mTextViewStorage = (TextView) findViewById(R.id.textViewStorage);
        mTextViewTheme = (TextView) findViewById(R.id.textViewTheme);
        //---------
        int mode = getIntent().getIntExtra("mode", WELCOME_CHANGELOG);
        switch (mode) {
            case WELCOME_CHANGELOG:
                findViewById(R.id.page_changelog).setVisibility(View.VISIBLE);
                ((TextView) findViewById(R.id.textView)).setText(
                        Html.fromHtml(AppHelper.getRawString(this, R.raw.changelog))
                );
                findViewById(R.id.button_close).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        finish();
                    }
                });
                findViewById(R.id.checkBox_showChangelog).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (v instanceof Checkable) {
                            getSharedPreferences(WelcomeActivity.class.getName(), MODE_PRIVATE)
                                    .edit().putBoolean("showChangelog", ((Checkable) v).isChecked()).apply();
                        }
                    }
                });
                break;
            case WELCOME_ONBOARDING:
                ViewCompat.setElevation(toolbar, 0);
                findViewById(R.id.page_onboarding1).setVisibility(View.VISIBLE);
                findViewById(R.id.button_done).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        finish();
                    }
                });
                final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
                final String[] themes = getResources().getStringArray(R.array.themes_names);
                int selTheme = Integer.parseInt(prefs.getString("theme", "0"));
                mTextViewTheme.setText(getString(R.string.theme) + ": " + themes[selTheme]);
                mTextViewTheme.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        new AlertDialog.Builder(WelcomeActivity.this)
                                .setItems(themes, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        mTextViewTheme.setText(getString(R.string.theme) + ": " + themes[i]);
                                        prefs.edit()
                                                .putString("theme", String.valueOf(i))
                                                .apply();
                                        dialogInterface.dismiss();
                                    }
                                })
                                .create()
                                .show();
                    }
                });
                Button buttonRestore = (Button) findViewById(R.id.buttonRestore);
                buttonRestore.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (checkPermission(Manifest.permission.READ_EXTERNAL_STORAGE)) {
                            BackupRestoreUtil.showRestoreDialog(WelcomeActivity.this);
                        }
                    }
                });
                Button buttonSync = (Button) findViewById(R.id.buttonSync);
                buttonSync.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        SettingsActivity2.openSyncSettings(WelcomeActivity.this, 0);
                    }
                });
                int active = Math.min(
                        getSharedPreferences("providers", Context.MODE_PRIVATE).getInt("count", Providers.getCount()),
                        Providers.getCount()
                );
                mTextViewSources.setText(getString(R.string.sources) + ": " + getString(R.string.providers_pref_summary, active, Providers.getCount()));
                mTextViewSources.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        SettingsActivity2.openProvidersSettings(WelcomeActivity.this, REQUEST_SOURCES);
                    }
                });
                mTextViewStorage.setText(MangaStore.getMangasDir(this).getPath());
                mTextViewStorage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        new StorageSelectDialog(WelcomeActivity.this, false)
                                .setDirSelectListener(new DirSelectDialog.OnDirSelectListener() {
                                    @Override
                                    public void onDirSelected(File dir) {
                                        prefs.edit()
                                                .putString("mangadir", dir.getPath())
                                                .apply();
                                        mTextViewStorage.setText(dir.getPath());
                                    }
                                })
                                .show();
                    }
                });
                break;
            default:
                finish();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case BackupRestoreUtil.BACKUP_IMPORT_CODE:
                if (resultCode == RESULT_OK) {
                    File file = AppHelper.getFileFromUri(this, data.getData());
                    if (file != null) {
                        new BackupRestoreUtil(this).restore(file);
                    } else {
                        Toast.makeText(this, R.string.error, Toast.LENGTH_SHORT).show();
                    }
                }
                break;
            case REQUEST_SOURCES:
                int active = Math.min(
                        getSharedPreferences("providers", Context.MODE_PRIVATE).getInt("count", Providers.getCount()),
                        Providers.getCount()
                );
                mTextViewSources.setText(getString(R.string.sources) + ": " + getString(R.string.providers_pref_summary, active, Providers.getCount()));
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onPermissionGranted(String permission) {
        if (Manifest.permission.READ_EXTERNAL_STORAGE.equals(permission)) {
            BackupRestoreUtil.showRestoreDialog(WelcomeActivity.this);
        }
    }
}
