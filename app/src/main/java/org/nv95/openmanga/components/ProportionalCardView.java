package org.nv95.openmanga.components;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.util.AttributeSet;

/**
 * Created by nv95 on 25.01.16.
 */
public class ProportionalCardView extends CardView {
    private static final double ASPECT_RATIO = 5f/3f;

    public ProportionalCardView(Context context) {
        super(context);
    }

    public ProportionalCardView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ProportionalCardView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
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
