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

import java.io.IOException;
import java.util.regex.Pattern;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections4.iterators.EnumerationIterator;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.jetty.util.URIUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.FluentIterable;

/**
 * Servlet filter which redirects any access to the application using the
 * {@code https://} protocol to the same location using the {@code https://} protocol.
 *
 * @author Graham Crockford
 */
public final class HttpsEnforcer implements Filter {

  private static final Logger LOGGER = LoggerFactory.getLogger(HttpsEnforcer.class);

  private static final String HTTPS = "https";
  private static final String X_FORWARDED_PROTO = "X-Forwarded-Proto";
  private static final String STRICT_CONTENT_SECURITY = "Strict-Transport-Security";

  private static final String CONTENT_SECURITY_HEADER = "max-age=63072000; includeSubDomains; preload";
  private static final Pattern CR_OR_LF = Pattern.compile("\\r|\\n");

  private final HttpsResponsibility httpsResponsibility;

  /**
   * @param httpsResponsibility Indicates how the application is deployed.
   */
  public HttpsEnforcer(HttpsResponsibility httpsResponsibility) {
    this.httpsResponsibility = httpsResponsibility;
  }

  @Override
  public void init(FilterConfig filterConfig) throws ServletException {
    // No-op
  }

  @Override
  public final void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
    if (!HttpServletResponse.class.isInstance(response)) {
      return;
    }
    apply((HttpServletRequest) request, (HttpServletResponse) response, chain);
  }

  @Override
  public void destroy() {
    // No-op
  }

  private void apply(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws IOException, ServletException {
    switch (httpsResponsibility) {
      case HTTPS_AT_PROXY:
        if (StringUtils.isEmpty(request.getHeader(X_FORWARDED_PROTO))) {
          throw new IllegalStateException(
              "Configured to assume application is behind a proxy but the forward header has not been provided. "
                  + "Headers available: " + listForRequest(request).toList());
        }
        if (!request.getHeader(X_FORWARDED_PROTO).equalsIgnoreCase(HTTPS)) {
          switchToHttps(request, response);
          return;
        }
        break;
      case HTTPS_DIRECT:
        if (!request.isSecure()) {
          if (request.getProtocol().equalsIgnoreCase(HTTPS)) {
            throw new IllegalStateException(
                "Configured to assume application is accessed directly but connection is not secure and "
                    + "protocol is already https");
          }
          switchToHttps(request, response);
          return;
        }
        break;
      default:
        throw new UnsupportedOperationException("Unknown HTTP responsibility: " + httpsResponsibility);
    }

    response.addHeader(STRICT_CONTENT_SECURITY, CONTENT_SECURITY_HEADER);
    filterChain.doFilter(request, response);

  }

  private void switchToHttps(HttpServletRequest request, HttpServletResponse response) throws IOException {

    // Reform the URL
    final StringBuilder url = new StringBuilder(128);
    URIUtil.appendSchemeHostPort(url, HTTPS, request.getServerName(), request.getServerPort() == 80 ? 443 : request.getServerPort());
    url.append(request.getRequestURI());
    if (request.getQueryString() != null) {
      url.append("?");
      url.append(request.getQueryString());
    }

    // Check against response split attacks
    String redirect = sanitize(url.toString());
    if (redirect == null) {
      response.sendError(400, "Malformed request");
    }

    // All good
    LOGGER.error("Unsecured access redirected to [{}]", redirect);
    response.sendRedirect(redirect);

  }

  String sanitize(String url) {
    if (CR_OR_LF.matcher(url).find()) {
      LOGGER.warn("Attempted response split attack");
      return null;
    }
    return url;
  }

  private static FluentIterable<String> listForRequest(HttpServletRequest request) {
    return FluentIterable.from(() -> new EnumerationIterator<>(request.getHeaderNames()));
  }
}
