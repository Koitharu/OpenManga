package org.nv95.openmanga.components;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;

/**
 * Created by nv95 on 25.01.16.
 */
public class AutoHeightLayout extends FrameCheckLayout {

    private double mAspectRatio = 18f / 13f;

    public AutoHeightLayout(Context context) {
        super(context);
    }

    public AutoHeightLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AutoHeightLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public AutoHeightLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public void setAspectRatio(float value) {
        mAspectRatio = value;
    }

    public void setAspectRatio(int height, int width) {
        mAspectRatio = height / (float)width;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int originalWidth = MeasureSpec.getSize(widthMeasureSpec);
        int calculatedHeight = (int) (originalWidth * mAspectRatio);
        super.onMeasure(
                MeasureSpec.makeMeasureSpec(originalWidth, MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(calculatedHeight, MeasureSpec.EXACTLY)
        );
    }
}
