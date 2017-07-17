package cd.connect.spring.jersey.log;

import cd.connect.context.ConnectContext;
import cd.connect.spring.jersey.JerseyLoggerPoint;
import com.bluetrainsoftware.common.config.ConfigKey;
import net.stickycode.stereotype.configured.PostConfigured;
import org.glassfish.jersey.logging.Constants;
import org.glassfish.jersey.logging.FilteringServerLoggingFilter;
import org.glassfish.jersey.logging.LoggingFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletProperties;

import java.util.Objects;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Being here and in a component allows for it to be reloaded by the configuration system as necessary.
 *
 * Many of these things cannot be reloaded however. Once a ResourceConfig is loaded, it stays loaded. We may
 * support servlet un/reloading later.
 *
 * These are only GLOBAL settings for all jersey contexts. If you load multiple of them and you want individual
 * configuration you will need to do this another way.
 *
 * @author Richard Vowles - https://plus.google.com/+RichardVowles
 */

public class JerseyFilteringConfiguration implements JerseyFiltering {
	private static final Logger logRef = JerseyLoggerPoint.julLogger;

	private Set<String> excludeUri;
	private Set<String> excludeEntirelyUri;

	@ConfigKey("jersey.logging.exclude-body-uris")
	protected String excludeBodyUris = "";

	@ConfigKey("jersey.logging.exclude-entirely-uris")
	protected String excludeEntirelyUris = "";

	@ConfigKey("jersey.exclude")
	protected String exclude = ""; // e.g. /(apibrowser|metrics|service|status).*

	@ConfigKey("jersey.tracing")
	protected String tracing = ""; // e.g. ON_DEMAND

	@ConfigKey("jersey.bufferSize")
	protected Integer bufferSize = 8192;

	@PostConfigured
	public void init() {
		/*
		* we are default using Kubernetes and Prometheus, so we should ignore at least these by default.
		 */
		excludeUri = deconstructConfiguration(excludeBodyUris);
		excludeEntirelyUri = deconstructConfiguration(excludeEntirelyUris);
	}

	private Set<String> deconstructConfiguration(String toSplit)  {
		return Stream.of(toSplit.split(","))
			.filter(Objects::nonNull)
			.map(String::trim)
			.filter(s -> s.length() > 0)
			.collect(Collectors.toSet());
	}

	/**
	 * Return true if the body payload should not be logged.
	 */
	public boolean excludePayloadForUri(String uriPath) {
		if (excludeUri.contains(uriPath)) {
			if (logRef.isLoggable(Level.FINE)) {
				// mention we are excluding payload logging
				ConnectContext.set(Constants.REST_CONTEXT, "exclude payload logging for uriPath:" + uriPath);
				logRef.fine("no payload");
				ConnectContext.remove(Constants.REST_CONTEXT);
			}
			return true;
		}
		return false;
	}

	/**
	 * should we exclude this reference entirely?
	 */
	public boolean excludeForUri(String uriPath) {
		return excludeEntirelyUri.contains(uriPath);
	}

	@Override
	public void registerFilters(ResourceConfig resourceConfig) {
		if (exclude.length() > 0) {
			resourceConfig.property(ServletProperties.FILTER_STATIC_CONTENT_REGEX, exclude);
		}

		if (tracing.length() > 0) {
			resourceConfig.property("jersey.config.server.tracing.type", tracing);
		}

		// determine if we need to log any of the Jersey stuff. See the TracingJerseyLogger for details.
		if (JerseyLoggerPoint.logger.isTraceEnabled()) {
			resourceConfig.register(newLogger(this, LoggingFeature.Verbosity.PAYLOAD_ANY));
		} else if (JerseyLoggerPoint.logger.isDebugEnabled()) {
			resourceConfig.register(newLogger(this, LoggingFeature.Verbosity.HEADERS_ONLY));
		}
	}

	private FilteringServerLoggingFilter newLogger(JerseyFiltering jerseyFiltering, LoggingFeature.Verbosity verbosity) {
		return new FilteringServerLoggingFilter(jerseyFiltering, JerseyLoggerPoint.julLogger, Level.ALL, verbosity, bufferSize);
	}
}
