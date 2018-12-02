package com.gruelbox.tools.dropwizard.httpsredirect;

/*-
 * ===============================================================================L
 * dropwizard-https-redirect
 * ================================================================================
 * Copyright (C) 2018 Graham Crockford
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ===============================================================================E
 */

import javax.servlet.FilterRegistration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.dropwizard.Application;
import io.dropwizard.ConfiguredBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

/**
 * Dropwizard bundle which redirects any access to the application using the
 * {@code https://} protocol to the same location using the {@code https://} protocol.
 *
 * <P>Usage:</p>
 *
 * <p>Modify your application configuration class so that it implements {@link HttpEnforcementConfiguration},
 * then add the bundle in your {@link Application#initialize(Bootstrap)} method:</p>
 *
 * <pre> @Override
 * public void initialize(final Bootstrap&lt;MyConfiguration&gt; bootstrap) {
 *   bootstrap.addBundle(new HttpsEnforcementBundle());
 * }</pre>
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
