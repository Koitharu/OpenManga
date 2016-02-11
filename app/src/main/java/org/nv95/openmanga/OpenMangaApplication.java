package org.nv95.openmanga;

import android.app.Application;
import android.content.Context;
import android.content.res.Resources;
import android.support.v4.content.ContextCompat;

import org.nv95.openmanga.components.AsyncImageView;
import org.nv95.openmanga.items.ThumbSize;
import org.nv95.openmanga.utils.ErrorReporter;

/**
 * Created by nv95 on 10.12.15.
 */
public class OpenMangaApplication extends Application {

    public static int getVersion(Context context) {
        try {
            return context.getPackageManager().getPackageInfo(context.getPackageName(), 0)
                    .versionCode;
        } catch (Exception e) {
            return -1;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        ErrorReporter.Init(this);
        AsyncImageView.IMAGE_HOLDER = ContextCompat.getDrawable(this, R.drawable.placeholder);
        final Resources resources = getResources();
        final float aspectRatio = 6f / 4f;
        ThumbSize.THUMB_SIZE_LIST = new ThumbSize(
                resources.getDimensionPixelSize(R.dimen.thumb_width_list),
                resources.getDimensionPixelSize(R.dimen.thumb_height_list)
        );
        ThumbSize.THUMB_SIZE_SMALL = new ThumbSize(
                resources.getDimensionPixelSize(R.dimen.thumb_width_small),
                aspectRatio
        );
        ThumbSize.THUMB_SIZE_MEDIUM = new ThumbSize(
                resources.getDimensionPixelSize(R.dimen.thumb_width_medium),
                aspectRatio
        );
        ThumbSize.THUMB_SIZE_LARGE = new ThumbSize(
                resources.getDimensionPixelSize(R.dimen.thumb_width_large),
                aspectRatio
        );
    }
}
