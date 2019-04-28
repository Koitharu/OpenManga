package org.nv95.openmanga.components;

import android.content.Context;
import android.graphics.Matrix;
import android.util.AttributeSet;

/**
 * ImageView to display top-crop scale of an image view.
 * https://gist.github.com/arriolac/3843346
 *
 * @author Chris Arriola
 */
public class TopCropImageView extends androidx.appcompat.widget.AppCompatImageView {

    public TopCropImageView(Context context) {
        this(context, null);
    }

    public TopCropImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TopCropImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private float mScale = -1f;

    protected void init(){
        setScaleType(ScaleType.MATRIX);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        recomputeImgMatrix();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        recomputeImgMatrix();
    }

    @Override
    protected boolean setFrame(int l, int t, int r, int b) {
        recomputeImgMatrix();
        return super.setFrame(l, t, r, b);
    }

    private void recomputeImgMatrix() {
        if (getDrawable() == null) {
            return;
        }
        final Matrix matrix = getImageMatrix();

        float scale;
        final int viewWidth = getWidth() - getPaddingLeft() - getPaddingRight();
        final int drawableWidth = getDrawable().getIntrinsicWidth();

        scale = (float) viewWidth / (float) drawableWidth;

        if (scale != mScale) {
            mScale = scale;
            matrix.setScale(scale, scale);
            setImageMatrix(matrix);
        }
    }
}