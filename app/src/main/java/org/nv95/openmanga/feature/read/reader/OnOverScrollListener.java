package org.nv95.openmanga.feature.read.reader;

/**
 * Created by nv95 on 18.11.16.
 */

public interface OnOverScrollListener {

    int LEFT = 0;
    int RIGHT = 1;
    int TOP = 2;
    int BOTTOM = 3;

    void onOverScrollFlying(int direction, int distance);
    boolean onOverScrollFinished(int direction, int distance);
    void onOverScrollStarted(int direction);
    void onOverScrolled(int direction);
}
