package com.guicedee.microprofile.config.implementations;

import com.guicedee.client.services.IGuiceConfig;
import com.guicedee.client.services.lifecycle.IGuiceConfigurator;

/**
 * Enables classpath, annotation, and member scanning required for
 * MicroProfile Config injection.
 */
public class ScanConfig implements IGuiceConfigurator
{
    /**
     * Adjusts the Guice configuration to allow field scanning and access.
     *
     * @param config configuration to mutate
     * @return the same configuration instance for chaining
     */
    @Override
    public IGuiceConfig<?> configure(IGuiceConfig<?> config)
    {
        config.setClasspathScanning(true)
              .setAnnotationScanning(true)
              .setFieldScanning(true)
              .setIgnoreClassVisibility(true)
              .setIgnoreFieldVisibility(true)
              .setIgnoreMethodVisibility(true)
              .setClasspathScanning(true);
        return config;
    }
}
