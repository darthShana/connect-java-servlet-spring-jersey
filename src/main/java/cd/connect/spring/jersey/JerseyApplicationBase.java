package cd.connect.spring.jersey;


import cd.connect.jackson.JacksonObjectProvider;
import cd.connect.spring.jersey.log.JerseyFiltering;
import com.bluetrainsoftware.prometheus.PrometheusFilter;
import io.swagger.jaxrs.listing.ApiListingResource;
import io.swagger.jaxrs.listing.SwaggerSerializers;
import org.glassfish.jersey.CommonProperties;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.message.GZipEncoder;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.spring.scope.RequestContextFilter;
import org.glassfish.jersey.server.wadl.internal.WadlResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import javax.ws.rs.core.Configurable;
import java.util.stream.Stream;

import static org.glassfish.jersey.servlet.ServletProperties.PROVIDER_WEB_APP;

/**
 * @author Richard Vowles - https://plus.google.com/+RichardVowles
 */
public class JerseyApplicationBase extends ResourceConfig implements JerseyApplication {
	private static final Logger logger = LoggerFactory.getLogger(JerseyApplicationBase.class);

	public void init(ApplicationContext context, Stream<Class<?>> resources) {
		property(CommonProperties.METAINF_SERVICES_LOOKUP_DISABLE, true);
		property(CommonProperties.FEATURE_AUTO_DISCOVERY_DISABLE, true);
		property(CommonProperties.MOXY_JSON_FEATURE_DISABLE, true);

		property(PROVIDER_WEB_APP, false); // do not scan!

		register(RequestContextFilter.class);
		register(JacksonFeature.class);
		register(MultiPartFeature.class);
		register(GZipEncoder.class);
		register(JacksonObjectProvider.class);
		register(PrometheusFilter.class);

		// allow generation of WADLs.
		register(WadlResource.class);

		// support swagger requests
		this.register(ApiListingResource.class);
		this.register(SwaggerSerializers.class);

		registerLogging(context);

		if (resources != null) {
			resources.forEach(c -> {
				logger.debug("registering jersey resource: {}", c.getName());
				register(context.getBean(c));
			});
		}
	}

	private void registerLogging(ApplicationContext context) {
		try {
			context.getBean(JerseyFiltering.class).registerFilters(Configurable.class.cast(this));
		} catch (Exception ex) {
			logger.warn("Unable register logging or path exclusion - perhaps you didn't include `{}` class?", JerseyConfig.class.getName());
		}
	}

	/*
	 * override this if you wish to register further stuff
	 */
	protected void enhance(ApplicationContext context, Stream<Class<?>> resources) {
	}
}
