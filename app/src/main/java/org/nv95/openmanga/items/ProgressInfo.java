package org.nv95.openmanga.items;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by nv95 on 17.03.16.
 */
public class ProgressInfo {
    public final AtomicInteger progress = new AtomicInteger();
    public final AtomicInteger max = new AtomicInteger();
}
