package org.nv95.openmanga.components.pager;

import android.support.v4.view.ViewPager;
import android.view.View;

/**
 * Created by nv95 on 01.07.16.
 */

public class VerticalSlidePageTransformer  implements ViewPager.PageTransformer {

    @Override
    public void transformPage(View page, float position) {
        if (position < -1) { // [-Infinity,-1)
            page.setAlpha(0);
        } else if (position <= 0) { // [-1,0]
            page.setAlpha(1);
            float yPosition = position * page.getHeight();
            page.setTranslationY(yPosition);
            page.setTranslationX(page.getWidth() * -position);
        } else if (position <= 1) { // (0,1]
            page.setAlpha(1);
            page.setTranslationX(page.getWidth() * -position);
            page.setTranslationY(0);
        } else { // (1,+Infinity]
            page.setAlpha(0);
        }
    }
}