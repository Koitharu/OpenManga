package org.nv95.openmanga;

import android.app.Application;

import org.nv95.openmanga.components.AsyncImageView;

/**
 * Created by nv95 on 10.12.15.
 */
public class OpenMangaApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        AsyncImageView.IMAGE_HOLDER = getResources().getDrawable(R.drawable.placeholder);
    }
}
