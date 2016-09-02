package org.nv95.openmanga;

import android.app.Application;
import android.content.res.Resources;

import org.nv95.openmanga.items.ThumbSize;
import org.nv95.openmanga.utils.FileLogger;
import org.nv95.openmanga.utils.ImageUtils;

/**
 * Created by nv95 on 10.12.15.
 */
public class OpenMangaApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        FileLogger.init(this);
        Resources resources = getResources();
        float aspectRatio = 6f / 4f;
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

        ImageUtils.init(this);
        ScheduledServiceReceiver.enable(this);
    }
}
