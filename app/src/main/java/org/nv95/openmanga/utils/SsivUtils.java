package org.nv95.openmanga.utils;

import android.graphics.PointF;

import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;

/**
 * Created by nv95 on 18.11.16.
 */

public class SsivUtils {

    public static void setScaleWidthTop(SubsamplingScaleImageView ssiv) {
        ssiv.setMinimumScaleType(SubsamplingScaleImageView.SCALE_TYPE_CUSTOM);
        ssiv.setMinScale(ssiv.getWidth() / (float)ssiv.getSWidth());
        ssiv.setScaleAndCenter(
                ssiv.getMinScale(),
                new PointF(ssiv.getSWidth() / 2f, 0)
        );
    }

    public static void setScaleHeightLeft(SubsamplingScaleImageView ssiv) {
        ssiv.setMinimumScaleType(SubsamplingScaleImageView.SCALE_TYPE_CUSTOM);
        ssiv.setMinScale(ssiv.getHeight() / (float)ssiv.getSHeight());
        ssiv.setScaleAndCenter(
                ssiv.getMinScale(),
                new PointF(0, ssiv.getSHeight() / 2f)
        );
    }

    public static void setScaleHeightRight(SubsamplingScaleImageView ssiv) {
        ssiv.setMinimumScaleType(SubsamplingScaleImageView.SCALE_TYPE_CUSTOM);
        ssiv.setMinScale(ssiv.getHeight() / (float)ssiv.getSHeight());
        ssiv.setScaleAndCenter(
                ssiv.getMinScale(),
                new PointF(ssiv.getSWidth(), ssiv.getSHeight() / 2f)
        );
    }

    public static void setScaleFit(SubsamplingScaleImageView ssiv) {
        ssiv.setMinimumScaleType(SubsamplingScaleImageView.SCALE_TYPE_CENTER_INSIDE);
        ssiv.resetScaleAndCenter();
    }

    public static void setScaleZoomSrc(SubsamplingScaleImageView ssiv) {
        ssiv.setMinimumScaleType(SubsamplingScaleImageView.SCALE_TYPE_CENTER_INSIDE);
        ssiv.setScaleAndCenter(
                ssiv.getMaxScale(),
                new PointF(ssiv.getSWidth(), 0)
        );
    }
}
