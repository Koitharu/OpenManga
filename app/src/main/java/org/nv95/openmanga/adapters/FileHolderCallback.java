package org.nv95.openmanga.adapters;

import org.nv95.openmanga.feature.fileselect.adapter.FileSelectAdapter;

/**
 * Created by nv95 on 09.02.16.
 */
public interface FileHolderCallback {
    void onItemClick(FileSelectAdapter.FileViewHolder holder);
}
