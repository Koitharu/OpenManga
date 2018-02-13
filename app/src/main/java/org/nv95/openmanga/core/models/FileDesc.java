package org.nv95.openmanga.core.models;

import java.io.File;

public final class FileDesc {

	public final File file;
	public final int entryCount;

	public FileDesc(File file, int entryCount) {
		this.file = file;
		this.entryCount = entryCount;
	}
}
