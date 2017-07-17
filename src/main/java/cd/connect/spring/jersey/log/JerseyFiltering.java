package cd.connect.spring.jersey.log;

import org.glassfish.jersey.server.ResourceConfig;

import java.util.function.Consumer;

/**
 * @author Richard Vowles - https://plus.google.com/+RichardVowles
 */
public interface JerseyFiltering {
	boolean excludePayloadForUri(String uriPath);
	boolean excludeForUri(String uriPath);

	void registerFilters(ResourceConfig resourceConfig);
}
