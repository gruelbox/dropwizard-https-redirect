package com.gruelbox.tools.dropwizard.httpsredirect;

import javax.servlet.FilterRegistration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.dropwizard.ConfiguredBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

/**
 * Dropwizard bundle which redirects any access to the application using the
 * {@code https://} protocol to the same location using the {@code https://} protocol.
 * 
 * <P>Usage:</p>
 * 
 * TODO
 * 
 * @author Graham Crockford
 */
public class HttpsEnforcementBundle implements ConfiguredBundle<HttpEnforcementConfiguration> {

  private static final Logger LOGGER = LoggerFactory.getLogger(HttpsEnforcementBundle.class);

  @Override
  public void initialize(Bootstrap<?> bootstrap) {
    // No-op
  }

  @Override
  public void run(HttpEnforcementConfiguration configuration, Environment environment) throws Exception {
    if (configuration.isHttpsOnly()) {
      LOGGER.info("Restricting to HTTPS only.");
      FilterRegistration.Dynamic httpsEnforcer = environment.servlets().addFilter(HttpsEnforcer.class.getSimpleName(),
          new HttpsEnforcer(configuration.getHttpResponsibility()));
      httpsEnforcer.addMappingForUrlPatterns(null, true, "/*");
    }
  }
}