package org.nv95.openmanga.components;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.FrameLayout;

/**
 * Created by nv95 on 25.01.16.
 */
public class ProportionalFrameLayout extends FrameLayout {
    private static final double ASPECT_RATIO = 5f/3f;

    public ProportionalFrameLayout(Context context) {
        super(context);
    }

    public ProportionalFrameLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ProportionalFrameLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public ProportionalFrameLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int originalWidth = MeasureSpec.getSize(widthMeasureSpec);
        int originalHeight = MeasureSpec.getSize(heightMeasureSpec);
        int calculatedHeight = (int) (originalWidth * ASPECT_RATIO);
        int finalWidth, finalHeight;
        if (calculatedHeight > originalHeight) {
            finalWidth = (int) (originalHeight / ASPECT_RATIO);
            finalHeight = originalHeight;
        } else {
            finalWidth = originalWidth;
            finalHeight = calculatedHeight;
        }
        super.onMeasure(
                MeasureSpec.makeMeasureSpec(finalWidth, MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(finalHeight, MeasureSpec.EXACTLY)
        );
    }
}
