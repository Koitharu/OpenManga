package org.nv95.openmanga.feature.read.reader.webtoon;

/**
 * Created by admin on 15.08.17.
 */

class ScrollState {

    final float scale;
    final int scrollX;
    final int scrollY;

    ScrollState(float scale, int scrollX, int scrollY) {
        this.scale = scale;
        this.scrollX = scrollX;
        this.scrollY = scrollY;
    }

    public ScrollState scale(float newScale) {
        return new ScrollState(newScale, scrollX, scrollY);
    }

    public ScrollState offsetX(int newOffsetX) {
        return new ScrollState(scale, newOffsetX, scrollY);
    }

    public ScrollState offsetY(int newOffsetY) {
        return new ScrollState(scale, scrollX, newOffsetY);
    }

    public ScrollState offset(int newOffsetX, int newOffsetY) {
        return new ScrollState(scale, newOffsetX, newOffsetY);
    }

    public ScrollState offsetRel(int deltaX, int deltaY) {
        return new ScrollState(scale, scrollX + deltaX, scrollY + deltaY);
    }
}
