package org.nv95.openmanga;

import android.app.Application;
import android.content.Context;

import org.nv95.openmanga.components.AsyncImageView;
import org.nv95.openmanga.utils.ErrorReporter;

/**
 * Created by nv95 on 10.12.15.
 */
public class OpenMangaApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        ErrorReporter.Init(this);
        AsyncImageView.IMAGE_HOLDER = getResources().getDrawable(R.drawable.placeholder);
    }

    public static int getVersion(Context context) {
        try {
            return context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionCode;
        } catch (Exception e) {
            return -1;
        }
    }
}
