package org.nv95.openmanga.feature.about;

import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;
import android.text.Html;
import android.widget.TextView;

import org.nv95.openmanga.R;
import org.nv95.openmanga.activities.BaseAppActivity;
import org.nv95.openmanga.utils.AppHelper;
import org.nv95.openmanga.utils.InternalLinkMovement;

/**
 * Created by nv95 on 12.01.16.
 */
public class AboutActivity extends BaseAppActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        enableHomeAsUp();
        TextView textView = (TextView) findViewById(R.id.textView);
        assert textView != null;
        textView.setText(Html.fromHtml(AppHelper.getRawString(this, R.raw.about)));
        textView.setMovementMethod(new InternalLinkMovement(null));
    }
}
