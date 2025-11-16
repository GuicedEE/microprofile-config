package com.guicedee.microprofile.config.implementations;

import com.google.inject.gee.InjectionPointProvider;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;

public class InjectionPointProvision implements InjectionPointProvider
{
    @Override
    public Class<? extends Annotation> injectionPoint(AnnotatedElement member)
    {
        return ConfigProperty.class;
    }
}
