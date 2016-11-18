package org.nv95.openmanga.components.reader;

/**
 * Created by nv95 on 18.11.16.
 */

public interface OnOverScrollListener {

    int LEFT = 0;
    int RIGHT = 1;
    int TOP = 2;
    int BOTTOM = 3;

    void onOverScrollFlying(int direction, float factor);
    void onOverScrollCancelled(int direction);
    void onOverScrollStarted(int direction);
    void onOverScrolled(int direction);
}
