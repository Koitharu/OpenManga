package org.nv95.openmanga.common.utils.network;

import java.io.IOException;

/**
 * Created by koitharu on 19.01.18.
 */

public final class HttpException extends IOException {

	private final int mStatusCode;

	public HttpException(int statusCode) {
		this.mStatusCode = statusCode;
	}

	public int getStatusCode() {
		return mStatusCode;
	}
}
