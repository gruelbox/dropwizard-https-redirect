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
 * Indicates how the application is deployed.
 *
 * @author Graham Crockford
 */
public enum HttpsResponsibility {

  /**
   * The application is serving HTTP only. HTTPS is provided by a proxy, so we
   * need to rely on forwarding headers from the proxy to determine what protocol
   * was used.
   */
  HTTPS_AT_PROXY,

  /**
   * The application is exposed directly to the internet without any sort of proxy
   * (the application manages its own SSL certificates). The bundle will check the
   * servlet request directly to ensure that it is secure.
   */
  HTTPS_DIRECT
}
