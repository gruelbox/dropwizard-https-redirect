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
   * The application is being accessed directly and is serving HTTPS itself.
   */
  HTTPS_DIRECT
}