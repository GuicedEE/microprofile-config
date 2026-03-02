package com.guicedee.microprofile.config.implementations;

import com.google.inject.gee.NamedAnnotationProvider;
import com.google.inject.name.Named;
import com.google.inject.name.Names;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.lang.annotation.Annotation;

/**
 * Converts {@code @ConfigProperty(name = "x")} annotations into Guice
 * {@code @Named("x")} bindings so that the property name is used as the
 * Guice binding key.
 */
public class ConfigPropertyNamedProvider implements NamedAnnotationProvider
{
    @Override
    public Named getNamedAnnotation(Annotation annotation)
    {
        if (annotation instanceof ConfigProperty configProperty)
        {
            return Names.named(configProperty.name());
        }
        return null;
    }

    @Override
    public Named getNamedAnnotation(Class<? extends Annotation> annotationType)
    {
        if (annotationType == ConfigProperty.class)
        {
            return Names.named("configProperty");
        }
        return null;
    }
}

