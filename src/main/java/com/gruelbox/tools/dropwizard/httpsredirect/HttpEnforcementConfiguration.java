/**
 * dropwizard-https-redirect
 * Copyright 2018-2019 Graham Crockford
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.gruelbox.tools.dropwizard.httpsredirect;

/**
 * Configuration for {@link HttpsEnforcementBundle}.
 *
 * @author Graham Crockford
 */
public interface HttpEnforcementConfiguration {

  /**
   * @return true if HTTP-to-HTTPS redirection is enabled. It is common to disable
   *         the feature in local testing environments.
   */
  public boolean isHttpsOnly();

  /**
   * @return Indicates how the application is deployed.
   */
  public HttpsResponsibility getHttpResponsibility();

}
