package org.nv95.openmanga.components;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.nv95.openmanga.R;

/**
 * Created by nv95 on 19.04.16.
 */
public class ActionAreasGrid extends LinearLayout {
    private final TextView[] mAreas = new TextView[9];

    public ActionAreasGrid(Context context) {
        this(context, null, 0);
    }

    public ActionAreasGrid(Context context, AttributeSet attrs) {
        this(context, null, 0);
    }

    public ActionAreasGrid(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public ActionAreasGrid(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context);
    }

    private void init(Context context) {
        LayoutInflater.from(context)
                .inflate(R.layout.grid_clickable, this, true);
//        mAreas[0] = (TextView) findViewById(R.id.textView_0);
//        mAreas[1] = (TextView) findViewById(R.id.textView_1);
//        mAreas[2] = (TextView) findViewById(R.id.textView_2);
//        mAreas[3] = (TextView) findViewById(R.id.textView_3);
//        mAreas[4] = (TextView) findViewById(R.id.textView_4);
//        mAreas[5] = (TextView) findViewById(R.id.textView_5);
//        mAreas[6] = (TextView) findViewById(R.id.textView_6);
//        mAreas[7] = (TextView) findViewById(R.id.textView_7);
//        mAreas[8] = (TextView) findViewById(R.id.textView_8);
    }
}
