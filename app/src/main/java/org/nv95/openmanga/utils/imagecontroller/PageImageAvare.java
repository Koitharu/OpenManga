package org.nv95.openmanga.utils.imagecontroller;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.view.View;

import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;
import com.nostra13.universalimageloader.core.imageaware.ViewAware;

/**
 * Created by Владимир on 24.03.2016.
 */
public class PageImageAvare extends ViewAware {

    public PageImageAvare(SubsamplingScaleImageView view) {
        super(view);
        view.setParallelLoadingEnabled(true);
    }

    @Override
    protected void setImageDrawableInto(Drawable drawable, View view) {

    }

    @Override
    protected void setImageBitmapInto(Bitmap bitmap, View view) {
        ImageSource source = ImageSource.cachedBitmap(bitmap).tilingDisabled();
        ((SubsamplingScaleImageView)view).setImage(source);
    }
}
