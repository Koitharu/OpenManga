package org.nv95.openmanga;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.view.View;
import android.widget.Checkable;
import android.widget.TextView;

import org.nv95.openmanga.utils.AppHelper;

/**
 * Created by nv95 on 18.10.15.
 */
public class WelcomeActivity extends AppCompatActivity {
    private static final int WELCOME_CHANGELOG = 1;
    private static final int WELCOME_LANGS = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        //init view

        //---------
        int mode = getIntent().getIntExtra("mode", WELCOME_CHANGELOG);
        switch (mode) {
            case WELCOME_CHANGELOG:
                findViewById(R.id.page_changelog).setVisibility(View.VISIBLE);
                ((TextView)findViewById(R.id.textView)).setText(
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
            case WELCOME_LANGS:
                break;
            default:
                finish();
        }
    }

    public static void ShowChangelog(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(WelcomeActivity.class.getName(), MODE_PRIVATE);
        int version = OpenMangaApplication.getVersion(context);
        if (prefs.getInt("version", -1) < version) {
            if (prefs.getBoolean("showChangelog", true)) {
                context.startActivity(
                        new Intent(context, WelcomeActivity.class)
                                .putExtra("mode", WELCOME_CHANGELOG)
                );
            }
            prefs.edit().putInt("version", version).apply();
        }
    }

    public static void ShowLangSelect(Context context) {
        context.startActivity(
                new Intent(context, WelcomeActivity.class)
                        .putExtra("mode", WELCOME_LANGS)
        );
    }
}
