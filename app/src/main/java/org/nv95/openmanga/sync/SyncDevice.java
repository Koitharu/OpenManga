package org.nv95.openmanga.sync;

/**
 * Created by koitharu on 31.12.17.
 */

public class SyncDevice {

	public int id;
	public String name;
	public long created_at;

	public SyncDevice(int id, String name, long created_at) {
		this.id = id;
		this.name = name;
		this.created_at = created_at;
	}
}