package org.nv95.openmanga.utils.log;

import org.jetbrains.annotations.Nullable;
import org.nv95.openmanga.BuildConfig;

import timber.log.Timber;

public class OpenMangaLogTree extends Timber.DebugTree {

    @Override
    protected boolean isLoggable(@Nullable final String tag, final int priority) {
        return BuildConfig.DEBUG;
    }
}
