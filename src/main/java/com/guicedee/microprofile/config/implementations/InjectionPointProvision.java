package com.guicedee.microprofile.config.implementations;

import com.google.inject.gee.InjectionPointProvider;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;

/**
 * Supplies the {@link ConfigProperty} annotation type for Guice injection
 * point processing.
 */
public class InjectionPointProvision implements InjectionPointProvider
{
    /**
     * Returns the annotation class used to mark configuration injection points.
     *
     * @param member the annotated element being inspected
     * @return {@link ConfigProperty}.class
     */
    @Override
    public Class<? extends Annotation> injectionPoint(AnnotatedElement member)
    {
        return ConfigProperty.class;
    }
}
