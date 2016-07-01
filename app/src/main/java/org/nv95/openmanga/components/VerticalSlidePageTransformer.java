package org.nv95.openmanga.components;

import android.support.v4.view.ViewPager;
import android.view.View;

/**
 * Created by nv95 on 01.07.16.
 */

public class VerticalSlidePageTransformer  implements ViewPager.PageTransformer {

    @Override
    public void transformPage(View page, float position) {
        if (position < -1) { // [-Infinity,-1)
            // This page is way off-screen to the left.
            page.setAlpha(0);
        } else if (position <= 0) { // [-1,0]
            // Use the default slide transition when moving to the left page
            page.setAlpha(1);
            float yPosition = position * page.getHeight();
            page.setTranslationY(yPosition);
            page.setTranslationX(page.getWidth() * -position);
        } else if (position <= 1) { // (0,1]
            // Fade the page out.
            page.setAlpha(1);
            // Counteract the default slide transition
            page.setTranslationX(page.getWidth() * -position);
            //set Y position to swipe in from top
            page.setTranslationY(0);

        } else { // (1,+Infinity]
            // This page is way off-screen to the right.
            page.setAlpha(0);
        }
    }
}