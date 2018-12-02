# dropwizard-https-redirect

[![Build Status](https://travis-ci.org/gruelbox/dropwizard-https-redirect.svg?branch=master)](https://travis-ci.org/gruelbox/dropwizard-https-redirect)
[![Sonarcloud Security Rating](https://sonarcloud.io/api/project_badges/measure?project=com.gruelbox%3Adropwizard-https-redirect&metric=security_rating)](https://sonarcloud.io/dashboard?id=com.gruelbox%3Adropwizard-https-redirect)
[![Sonarcloud Vulnerabilities](https://sonarcloud.io/api/project_badges/measure?project=com.gruelbox%3Adropwizard-https-redirect&metric=vulnerabilities)](https://sonarcloud.io/dashboard?id=com.gruelbox%3Adropwizard-https-redirect)
[![Sonarcloud Coverage](https://sonarcloud.io/api/project_badges/measure?project=com.gruelbox%3Adropwizard-https-redirect&metric=coverage)](https://sonarcloud.io/dashboard?id=com.gruelbox%3Adropwizard-https-redirect)
[![Sonarcloud Status](https://sonarcloud.io/api/project_badges/measure?project=com.gruelbox%3Adropwizard-https-redirect&metric=alert_status)](https://sonarcloud.io/dashboard?id=com.gruelbox%3Adropwizard-https-redirect)

A small dropwizard bundle which will redirect any incoming HTTP requests to the equivalent HTTPS URL.  Handles both the cases where the application is hosting both HTTP and HTTPS directly or where it is sitting behind an SSL proxy and receiving all traffic as HTTP.

Also protects against [HTTP Response Splitting](https://resources.infosecinstitute.com/http-response-splitting-attack/) attacks.

## Usage

### Add to your pom.xml

```
<dependency>
  <groupId>com.gruelbox</groupId>
  <artifactId>dropwizard-https-redirect</artifactId>
  <version>0.0.4</version>
</dependency>
```

### Set up configuration

Modify your application configuration class so that it `implements HttpEnforcementConfiguration`.

The `isHttpsOnly()` property enables or disables the redirection.  It usually makes sense to disable it in configuration when testing locally.

The `getHttpResponsibility()` property is extremely important:

 - If your application is exposed directly to the internet without any sort of proxy (so your application manages its own SSL certificates) set this to `HTTPS_DIRECT`.  The bundle will check the servlet request directly to ensure that it is secure.
 - If your application is hosted behind a proxy, and the proxy is managing the SSL side of things and forwarding both HTTP and HTTPS to your application as plain old HTTP (common with platforms such as [Heroku](https://www.heroku.com/)), we have to rely on the proxy to tell us what the original protocol was using the `X-Forwarded-Proto` header.  Most proxies do this, but _do check yours_.  To enabled this, use `HTTPS_AT_PROXY`.
 
### Install the bundle

In your `Application`, modify `initialise()`:

```
  @Override
  public void initialize(final Bootstrap<MyConfiguration> bootstrap) {
    bootstrap.addBundle(new HttpsEnforcementBundle());
  }
```

That's it!
