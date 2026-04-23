package com.smartcampus;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

/**
 * JAX-RS Application subclass that establishes the versioned API entry point.
 * The @ApplicationPath annotation defines the base URI for all JAX-RS resources.
 *
 * Note: In our setup, the actual resource registration is handled by Jersey's
 * ResourceConfig in Main.java via package scanning. This Application subclass
 * serves as a standard JAX-RS configuration marker.
 */
@ApplicationPath("/api/v1")
public class SmartCampusApplication extends Application {
    // Jersey uses ResourceConfig (a subclass of Application) in Main.java
    // for programmatic configuration. This class serves as the standard
    // JAX-RS Application entry point with the @ApplicationPath annotation.
}
