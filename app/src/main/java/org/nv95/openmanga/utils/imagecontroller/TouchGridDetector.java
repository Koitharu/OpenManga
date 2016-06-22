package org.nv95.openmanga.utils.imagecontroller;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Build;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import org.nv95.openmanga.R;

/**
 * Created by Владимир on 06.06.2016.
 */

public class TouchGridDetector extends View {
    private boolean mIsStatusBar;
    private int mLimitYstatusBar;
    private int mAreaSize;
    private int mHeight, mWidth;
    private final Rect[] mAreas = new Rect[6];

    public TouchGridDetector(Context context) {
        this(context, null);
    }

    public TouchGridDetector(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TouchGridDetector(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public TouchGridDetector(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        mLimitYstatusBar = getContext().getResources().getDimensionPixelOffset(R.dimen.activity_vertical_margin);
        mAreaSize = getContext().getResources().getDimensionPixelOffset(R.dimen.overscroll_size); //ну тут другое нужно подобрать
        setBounds(0, 0);
    }

    /**
     * Для устройств с системными кнопками
     * При проведении снизу в верх или сверзу вниз у края экрана, для отображения системных кнопок
     * @param ev
     * @return
     */

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:

                if (ev.getY() < mLimitYstatusBar || (getHeight() - ev.getY() < (mLimitYstatusBar * 2))) {
                    mIsStatusBar = true;
                    return true;
                }
            case MotionEvent.ACTION_MOVE:
            case MotionEvent.ACTION_HOVER_MOVE:
                if (mIsStatusBar)
                    return true;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                mIsStatusBar = false;
                //Toast.makeText(getContext(), "area: " + getPointArea(new Point((int)ev.getX(), (int)ev.getY())), Toast.LENGTH_SHORT).show();
                break;
        }
        return mIsStatusBar || super.onTouchEvent(ev);
    }

    public void setBounds(int height, int width) {
        mHeight = height;
        mWidth = width;
        mAreas[0] = new Rect(0, 0, width / 2, mAreaSize);   //top left
        mAreas[1] = new Rect(width / 2, 0, width, mAreaSize);   //top right
        mAreas[2] = new Rect(0, mAreaSize, mAreaSize, height - mAreaSize); //left
        mAreas[3] = new Rect(width - mAreaSize, mAreaSize, width, height - mAreaSize); //right
        mAreas[4] = new Rect(0, height - mAreaSize, width / 2, height);   //bottom left
        mAreas[5] = new Rect(width / 2, height - mAreaSize, width, height);   //bottom right
    }

    private int getPointArea(Point point) {
        for (int i=0;i<6;i++) {
            if (isPointInRect(point, mAreas[i])) {
                return i;
            }
        }
        return -1;
    }

    public static boolean isPointInRect(Point point, Rect rect) {
        return point.x >= rect.left
                && point.x <= rect.right
                && point.y >= rect.top
                && point.y <= rect.bottom;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setBounds(getMeasuredHeight(), getMeasuredWidth());
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        /*Paint paint = new Paint();
        paint.setColor(Color.BLUE);
        paint.setStrokeWidth(2);
        for (int i=0;i<6;i++) {
            canvas.drawRect(mAreas[i], paint);
        }*/
    }
}
