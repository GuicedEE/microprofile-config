package com.guicedee.microprofile.config.implementations;

import com.google.common.base.Strings;
import com.google.inject.AbstractModule;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;
import com.guicedee.client.IGuiceContext;
import com.guicedee.guicedinjection.interfaces.IGuiceModule;
import com.guicedee.microprofile.config.MicroProfileConfigContext;
import io.github.classgraph.ClassInfo;
import io.github.classgraph.ClassInfoList;
import io.github.classgraph.FieldInfo;
import io.smallrye.config.SmallRyeConfig;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.extern.java.Log;
import lombok.extern.log4j.Log4j2;
import org.eclipse.microprofile.config.ConfigValue;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Log4j2
public class MicroProfileConfigBinder extends AbstractModule implements IGuiceModule<MicroProfileConfigBinder>
{
    @Override
    protected void configure()
    {
        bind(SmallRyeConfig.class).toProvider(SmallRyeConfigProvider.class);
        ClassInfoList classesWithConfigProperties = IGuiceContext.instance()
                                                                 .getScanResult()
                                                                 .getClassesWithFieldAnnotation(ConfigProperty.class);
        Set<ClassKeyPair> bound = new HashSet<>();
        for (ClassInfo classesWithConfigProperty : classesWithConfigProperties)
        {
            for (FieldInfo fieldInfo : classesWithConfigProperty.getFieldInfo())
            {
                if (fieldInfo.hasAnnotation(ConfigProperty.class))
                {
                    Class<?> aClass = classesWithConfigProperty.loadClass();
                    try
                    {
                        Field declaredField = aClass.getDeclaredField(fieldInfo.getName());
                        ConfigProperty annotation = declaredField.getAnnotation(ConfigProperty.class);
                        ClassKeyPair kp = new ClassKeyPair(declaredField.getType(), annotation.name());
                        if (!bound.contains(kp))
                        {
                            if (String.class.isAssignableFrom(declaredField.getType()))
                            {
                                bound.add(kp);
                                bind(String.class).annotatedWith(annotation)
                                                  .toProvider(() -> {
                                                      ConfigValue configValue = MicroProfileConfigContext
                                                              .getConfig()
                                                              .getConfigValue(annotation.name());
                                                      return configValue.getValue();
                                                  });
                                bind(Key.get(new TypeLiteral<Optional<String>>()
                                {
                                }, annotation))
                                        .toProvider(() -> {
                                            ConfigValue configValue = MicroProfileConfigContext
                                                    .getConfig()
                                                    .getConfigValue(annotation.name());
                                            if (Strings.isNullOrEmpty(configValue.getValue()))
                                            {
                                                return Optional.of(configValue.getValue());
                                            }
                                            return Optional.empty();
                                        });
                            }
                            else if (Boolean.class.isAssignableFrom(declaredField.getType()) || boolean.class.isAssignableFrom(declaredField.getType()))
                            {
                                bound.add(new ClassKeyPair(Boolean.class, annotation.name()));
                                bound.add(new ClassKeyPair(boolean.class, annotation.name()));
                                bind(Boolean.class).annotatedWith(annotation)
                                                   .toProvider(() -> {
                                                       ConfigValue configValue = MicroProfileConfigContext
                                                               .getConfig()
                                                               .getConfigValue(annotation.name());
                                                       return Boolean.parseBoolean(configValue.getValue());
                                                   });
                                bind(Key.get(new TypeLiteral<Optional<Boolean>>()
                                {
                                }, annotation))
                                        .toProvider(() -> {
                                            ConfigValue configValue = MicroProfileConfigContext
                                                    .getConfig()
                                                    .getConfigValue(annotation.name());
                                            if (Strings.isNullOrEmpty(configValue.getValue()))
                                            {
                                                return Optional.of(Boolean.parseBoolean(configValue.getValue()));
                                            }
                                            return Optional.empty();
                                        });
                            }
                            else if (Integer.class.isAssignableFrom(declaredField.getType()) || int.class.isAssignableFrom(declaredField.getType()))
                            {
                                bound.add(new ClassKeyPair(Integer.class, annotation.name()));
                                bound.add(new ClassKeyPair(int.class, annotation.name()));

                                bind(Integer.class).annotatedWith(annotation)
                                                   .toProvider(() -> {
                                                       ConfigValue configValue = MicroProfileConfigContext
                                                               .getConfig()
                                                               .getConfigValue(annotation.name());
                                                       return Integer.parseInt(configValue.getValue());
                                                   });
                                bind(Key.get(new TypeLiteral<Optional<Integer>>()
                                {
                                }, annotation))
                                        .toProvider(() -> {
                                            ConfigValue configValue = MicroProfileConfigContext
                                                    .getConfig()
                                                    .getConfigValue(annotation.name());
                                            if (Strings.isNullOrEmpty(configValue.getValue()))
                                            {
                                                return Optional.of(Integer.parseInt(configValue.getValue()));
                                            }
                                            return Optional.empty();
                                        });
                            }
                            else if (Double.class.isAssignableFrom(declaredField.getType()) || double.class.isAssignableFrom(declaredField.getType()))
                            {
                                bound.add(new ClassKeyPair(Double.class, annotation.name()));
                                bound.add(new ClassKeyPair(double.class, annotation.name()));
                                bind(Double.class).annotatedWith(annotation)
                                                  .toProvider(() -> {
                                                      ConfigValue configValue = MicroProfileConfigContext
                                                              .getConfig()
                                                              .getConfigValue(annotation.name());
                                                      return Double.parseDouble(configValue.getValue());
                                                  });
                                bind(Key.get(new TypeLiteral<Optional<Double>>()
                                {
                                }, annotation))
                                        .toProvider(() -> {
                                            ConfigValue configValue = MicroProfileConfigContext
                                                    .getConfig()
                                                    .getConfigValue(annotation.name());
                                            if (Strings.isNullOrEmpty(configValue.getValue()))
                                            {
                                                return Optional.of(Double.parseDouble(configValue.getValue()));
                                            }
                                            return Optional.empty();
                                        });
                            }
                            else if (Float.class.isAssignableFrom(declaredField.getType()) || float.class.isAssignableFrom(declaredField.getType()))
                            {
                                bound.add(new ClassKeyPair(Float.class, annotation.name()));
                                bound.add(new ClassKeyPair(float.class, annotation.name()));
                                bind(Float.class).annotatedWith(annotation)
                                                 .toProvider(() -> {
                                                     ConfigValue configValue = MicroProfileConfigContext
                                                             .getConfig()
                                                             .getConfigValue(annotation.name());
                                                     return Float.parseFloat(configValue.getValue());
                                                 });
                                bind(Key.get(new TypeLiteral<Optional<Float>>()
                                {
                                }, annotation))
                                        .toProvider(() -> {
                                            ConfigValue configValue = MicroProfileConfigContext
                                                    .getConfig()
                                                    .getConfigValue(annotation.name());
                                            if (Strings.isNullOrEmpty(configValue.getValue()))
                                            {
                                                return Optional.of(Float.parseFloat(configValue.getValue()));
                                            }
                                            return Optional.empty();
                                        });
                            }
                            else if (Long.class.isAssignableFrom(declaredField.getType()) || long.class.isAssignableFrom(declaredField.getType()))
                            {
                                bound.add(new ClassKeyPair(Long.class, annotation.name()));
                                bound.add(new ClassKeyPair(long.class, annotation.name()));
                                bind(Long.class).annotatedWith(annotation)
                                                .toProvider(() -> {
                                                    ConfigValue configValue = MicroProfileConfigContext
                                                            .getConfig()
                                                            .getConfigValue(annotation.name());
                                                    return Long.parseLong(configValue.getValue());
                                                });
                                bind(Key.get(new TypeLiteral<Optional<Long>>()
                                {
                                }, annotation))
                                        .toProvider(() -> {
                                            ConfigValue configValue = MicroProfileConfigContext
                                                    .getConfig()
                                                    .getConfigValue(annotation.name());
                                            if (Strings.isNullOrEmpty(configValue.getValue()))
                                            {
                                                return Optional.of(Long.parseLong(configValue.getValue()));
                                            }
                                            return Optional.empty();
                                        });
                            }
                        }
                    }
                    catch (NoSuchFieldException e)
                    {
                        throw new RuntimeException(e);
                    }
                }
            }
        }
        super.configure();
    }

    @EqualsAndHashCode(of = {"clazz",
            "name"})
    @AllArgsConstructor
    static final class ClassKeyPair
    {
        private final Class clazz;
        private final String name;
    }
}
