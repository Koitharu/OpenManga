package org.nv95.openmanga.filepicker;

import android.support.annotation.NonNull;

import java.io.File;

interface OnFileSelectListener {

	void onFileSelected(@NonNull File file);
}
