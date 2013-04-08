package com.precog.api;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

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
    public static final Service ProductionHttp = new Service() {
        @Override public URL serviceUrl() {
            try {
                return new URL("https", "api.precog.com", 443, "/v1/");
            } catch (MalformedURLException ex) {
                Logger.getLogger(Service.class.getName()).log(Level.SEVERE, "Invalid client URL", ex);
            }

            return null;
        }
    };

    /**
     * The default production https service.
     */
    public static final Service ProductionHttps = ServiceBuilder.service("api.precog.com");

    /**
     * The default beta https service.
     */
    public static final Service BetaPrecogHttps = ServiceBuilder.service("beta.precog.com");

    /**
     * Development https service.
     */
    public static final Service DevPrecogHttps = ServiceBuilder.service("devapi.precog.com");
}
