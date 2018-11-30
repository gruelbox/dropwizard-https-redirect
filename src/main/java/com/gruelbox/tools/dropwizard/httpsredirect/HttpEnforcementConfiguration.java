package com.gruelbox.tools.dropwizard.httpsredirect;

/**
 * Configuration for {@link HttpsEnforcementBundle}.
 * 
 * @author Graham Crockford
 */
public interface HttpEnforcementConfiguration {
  
  /**
   * @return true if HTTP-to-HTTPS redirection is enabled.
   */
  public boolean isHttpsOnly();
  
  /**
   * @return Indicates how the application is deployed.
   */
  public HttpsResponsibility getHttpResponsibility();

}
