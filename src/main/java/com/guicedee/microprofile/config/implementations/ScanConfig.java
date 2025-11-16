package com.guicedee.microprofile.config.implementations;

import com.guicedee.guicedinjection.interfaces.IGuiceConfig;
import com.guicedee.guicedinjection.interfaces.IGuiceConfigurator;

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
