package cd.connect.spring.jersey;


import org.slf4j.LoggerFactory;

/**
 * This has to be JUL because Jersey uses JUL. This should get mapped based on the logging framework
 * you actually use.
 *
 * @author Richard Vowles - https://plus.google.com/+RichardVowles
 */
public class JerseyLoggerPoint {
	public static final String LOGGER_POINT = "jersey-logging";

	public static final org.slf4j.Logger logger = LoggerFactory.getLogger(LOGGER_POINT);
}
