package org.glassfish.jersey.logging;

import cd.connect.spring.jersey.log.JerseyFiltering;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Base for both client and server jersey tracing.
 */
abstract class BaseFilteringLogger extends LoggingInterceptor {


	protected final JerseyFiltering jerseyFiltering;

	/**
	 * Creates a logging filter with custom logger and entity logging turned on, but potentially limiting the size
	 * of entity to be buffered and logged.
	 *
	 * @param logger        the logger to log messages to.
	 * @param level         level at which the messages will be logged.
	 * @param verbosity     verbosity of the logged messages.
	 * @param maxEntitySize maximum number of entity bytes to be logged (and buffered) - if the entity is larger,
	 *                      logging filter will print (and buffer in memory) only the specified number of bytes
	 */
	BaseFilteringLogger(JerseyFiltering jerseyFiltering, Logger logger, Level level, LoggingFeature.Verbosity verbosity, int maxEntitySize) {
		super(logger, level, verbosity, maxEntitySize);

		this.jerseyFiltering = jerseyFiltering;
	}


}
