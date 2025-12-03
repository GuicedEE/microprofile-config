package com.guicedee.microprofile.config.implementations;

import com.guicedee.client.services.IGuiceConfig;
import com.guicedee.client.services.lifecycle.IGuiceConfigurator;

public class ScanConfig implements IGuiceConfigurator
{
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
