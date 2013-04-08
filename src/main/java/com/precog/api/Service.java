package com.precog.api;

import java.net.URL;

/**
 * An interface wrapping a URL for a ReportGrid or Precog service.
 *
 * @author Kris Nuttycombe <kris@precog.com>
 */
public interface Service {
    public URL serviceUrl();
    

    /**
     * The deprecated production http service.
     *
     * @deprecated use {@link ProductionHttps}
     */
    @Deprecated
    public static final Service ProductionHttp = ServiceBuilder.fromHost("api.precog.com");

    /**
     * The default production https service.
     */
    public static final Service ProductionHttps = ServiceBuilder.fromHost("api.precog.com");

    /**
     * The default beta https service.
     */
    public static final Service BetaPrecogHttps = ServiceBuilder.fromHost("beta.precog.com");

    /**
     * Development https service.
     */
    public static final Service DevPrecogHttps = ServiceBuilder.fromHost("devapi.precog.com");
}
