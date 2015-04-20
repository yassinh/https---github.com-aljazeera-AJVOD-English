package com.brightcove.consulting.util;

import org.slf4j.Logger;

public abstract class LogUtils {

	public static void debug(Logger logger, String message) {
		if (logger.isDebugEnabled()) {
			logger.debug(message);
		}
	}
}
