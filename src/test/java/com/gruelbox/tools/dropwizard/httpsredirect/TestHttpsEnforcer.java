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

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Enumeration;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

public class TestHttpsEnforcer {
  
  private static final String X_FORWARDED_PROTO = "X-Forwarded-Proto";
  private static final String STRICT_CONTENT_SECURITY = "Strict-Transport-Security";
  private static final String CONTENT_SECURITY_HEADER = "max-age=63072000; includeSubDomains; preload";

  @Mock private FilterConfig filterConfig;
  @Mock private HttpServletRequest request;
  @Mock private HttpServletResponse response;
  @Mock private FilterChain filterChain;

  @Before
  public void setup() {
    MockitoAnnotations.initMocks(this);
  }
  
  @Test
  public void testNoInit() throws IOException, ServletException {
    HttpsEnforcer httpsEnforcer = new HttpsEnforcer(HttpsResponsibility.HTTPS_DIRECT);
    httpsEnforcer.init(filterConfig);
    verifyZeroInteractions(filterConfig);
  }
  
  @Test
  public void testNonHttpRequest() throws IOException, ServletException {
    HttpsEnforcer httpsEnforcer = new HttpsEnforcer(HttpsResponsibility.HTTPS_DIRECT);
    ServletRequest servletRequest = Mockito.mock(ServletRequest.class);
    ServletResponse servletResponse = Mockito.mock(ServletResponse.class);
    httpsEnforcer.doFilter(servletRequest, servletResponse, filterChain);
    verifyZeroInteractions(servletRequest, servletResponse, filterChain);
  }

  @Test
  public void testUnproxiedPassthrough() throws IOException, ServletException {
    HttpsEnforcer httpsEnforcer = new HttpsEnforcer(HttpsResponsibility.HTTPS_DIRECT);
    when(request.isSecure()).thenReturn(true);

    httpsEnforcer.doFilter(request, response, filterChain);

    verify(filterChain).doFilter(request, response);
    verify(response).addHeader(STRICT_CONTENT_SECURITY, CONTENT_SECURITY_HEADER);
    verifyNoMoreInteractions(response);
  }

  @Test
  public void testUnproxiedRedirectSimple() throws IOException, ServletException {
    HttpsEnforcer httpsEnforcer = new HttpsEnforcer(HttpsResponsibility.HTTPS_DIRECT);
    when(request.isSecure()).thenReturn(false);
    when(request.getProtocol()).thenReturn("http");
    when(request.getServerName()).thenReturn("foo.com");
    when(request.getRequestURI()).thenReturn("/here/and/there");

    httpsEnforcer.doFilter(request, response, filterChain);

    verify(response).sendRedirect("https://foo.com/here/and/there");
    verifyNoMoreInteractions(response, filterChain);
  }

  @Test(expected=IllegalStateException.class)
  public void testUnproxiedRedirectButIsHttps() throws IOException, ServletException {
    HttpsEnforcer httpsEnforcer = new HttpsEnforcer(HttpsResponsibility.HTTPS_DIRECT);
    when(request.isSecure()).thenReturn(false);
    when(request.getProtocol()).thenReturn("https");
    when(request.getServerName()).thenReturn("foo.com");
    when(request.getRequestURI()).thenReturn("/here/and/there");

    httpsEnforcer.doFilter(request, response, filterChain);
  }

  @Test
  public void testProxiedPassthrough() throws IOException, ServletException {
    HttpsEnforcer httpsEnforcer = new HttpsEnforcer(HttpsResponsibility.HTTPS_AT_PROXY);
    when(request.getHeader(X_FORWARDED_PROTO)).thenReturn("https");

    httpsEnforcer.doFilter(request, response, filterChain);

    verify(filterChain).doFilter(request, response);
    verify(response).addHeader(STRICT_CONTENT_SECURITY, CONTENT_SECURITY_HEADER);
    verifyNoMoreInteractions(response);
  }

  @Test
  public void testProxiedRedirectSimple() throws IOException, ServletException {
    HttpsEnforcer httpsEnforcer = new HttpsEnforcer(HttpsResponsibility.HTTPS_AT_PROXY);
    when(request.getHeader(X_FORWARDED_PROTO)).thenReturn("http");
    when(request.getServerName()).thenReturn("foo.com");
    when(request.getRequestURI()).thenReturn("/here/and/there");

    httpsEnforcer.doFilter(request, response, filterChain);

    verify(response).sendRedirect("https://foo.com/here/and/there");
    verifyNoMoreInteractions(response, filterChain);
  }
  
  @Test
  public void testProxiedRedirectPort80() throws IOException, ServletException {
    HttpsEnforcer httpsEnforcer = new HttpsEnforcer(HttpsResponsibility.HTTPS_AT_PROXY);
    when(request.getHeader(X_FORWARDED_PROTO)).thenReturn("http");
    when(request.getServerName()).thenReturn("foo.com");
    when(request.getRequestURI()).thenReturn("/here/and/there");
    when(request.getServerPort()).thenReturn(80);

    httpsEnforcer.doFilter(request, response, filterChain);

    verify(response).sendRedirect("https://foo.com/here/and/there");
    verifyNoMoreInteractions(response, filterChain);
  }


  @Test
  public void testProxiedRedirectComplex() throws IOException, ServletException {
    HttpsEnforcer httpsEnforcer = new HttpsEnforcer(HttpsResponsibility.HTTPS_AT_PROXY);
    when(request.getHeader(X_FORWARDED_PROTO)).thenReturn("http");
    when(request.getServerName()).thenReturn("foo.com");
    when(request.getServerPort()).thenReturn(5634);
    when(request.getRequestURI()).thenReturn("/here/and/there");
    when(request.getQueryString()).thenReturn("do=this&do=that");

    httpsEnforcer.doFilter(request, response, filterChain);

    verify(response).sendRedirect("https://foo.com:5634/here/and/there?do=this&do=that");
    verifyNoMoreInteractions(response, filterChain);
  }

  @Test
  public void testProxiedRedirectWithSplitAttack() throws IOException, ServletException {
    HttpsEnforcer httpsEnforcer = new HttpsEnforcer(HttpsResponsibility.HTTPS_AT_PROXY);
    when(request.getHeader(X_FORWARDED_PROTO)).thenReturn("http");
    when(request.getServerName()).thenReturn("foo.com");
    when(request.getRequestURI()).thenReturn("/here/and/there");
    when(request.getQueryString()).thenReturn("do=this&do=that\nAHA I HAVE YOU NOW");

    httpsEnforcer.doFilter(request, response, filterChain);
    
    verify(response).sendError(Mockito.eq(400), Mockito.anyString());
  }

  @Test(expected=IllegalStateException.class)
  public void testProxiedMissingHeader() throws IOException, ServletException {
    HttpsEnforcer httpsEnforcer = new HttpsEnforcer(HttpsResponsibility.HTTPS_AT_PROXY);
    when(request.getHeaderNames()).thenReturn(new Enumeration<String>() {
      @Override
      public String nextElement() {
        return null;
      }
      @Override
      public boolean hasMoreElements() {
        return false;
      }
    });

    httpsEnforcer.doFilter(request, response, filterChain);
  }
}
