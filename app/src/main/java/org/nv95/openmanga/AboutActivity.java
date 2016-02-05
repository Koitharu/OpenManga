package org.nv95.openmanga;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.view.MenuItem;
import android.widget.TextView;

import org.nv95.openmanga.utils.AppHelper;
import org.nv95.openmanga.utils.InternalLinkMovement;

/**
 * Created by nv95 on 12.01.16.
 */
public class AboutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        TextView textView = (TextView) findViewById(R.id.textView);
        textView.setText(Html.fromHtml(AppHelper.getRawString(this, R.raw.about)));
        textView.setMovementMethod(InternalLinkMovement.getInstance());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home && !getFragmentManager().popBackStackImmediate()) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}
