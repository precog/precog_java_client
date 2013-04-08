package com.precog.api;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Convenience methods to build a {@link Service}.
 *
 * @author Gabriel Claramunt <gabriel@precog.com>
 */
public class ServiceBuilder {
    public static final Service fromHost(String url) {
        final String fUrl=url;
        return new Service() {
            @Override public URL serviceUrl() {
                try {
                    return new URL("https", fUrl, 443, "/");
                } catch (MalformedURLException ex) {
                    Logger.getLogger(Service.class.getName()).log(Level.SEVERE, "Invalid client URL", ex);
                }

                return null;
            }
        };
    }
}
