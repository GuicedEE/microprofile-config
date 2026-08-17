package com.guicedee.microprofile.config;

import com.guicedee.client.services.lifecycle.IGuicePreStartup;
import com.guicedee.vertx.spi.VertXPreStartup;
import io.smallrye.config.SmallRyeConfig;
import io.smallrye.config.SmallRyeConfigBuilder;
import io.vertx.core.Future;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;

import java.util.List;

/**
 * Initializes and exposes the {@link SmallRyeConfig} instance used by the
 * application during Guice pre-startup.
 */
@Log4j2
@Getter
public class MicroProfileConfigContext implements IGuicePreStartup<MicroProfileConfigContext>
{
    /**
     * Shared configuration instance built at startup.
     */
    @Getter
    private static SmallRyeConfig config;

    /**
     * Builds the {@link SmallRyeConfig} on a Vert.x worker thread so startup
     * does not block the event loop.
     *
     * @return a list containing the asynchronous startup task
     */
    @Override
    public List<Future<Boolean>> onStartup()
    {
        return List.of(VertXPreStartup.getVertx().executeBlocking(() -> {
            log.debug("Starting MicroProfileConfigContext");
            SmallRyeConfigBuilder configBuilder = new SmallRyeConfigBuilder()
                    .addDefaultSources()
                    .addDefaultInterceptors()
                    .addDiscoveredSources()
                    .addDiscoveredConverters()
                    .addDiscoveredInterceptors();
            try
            {
                config = configBuilder.build();
            }
            catch (RuntimeException e)
            {
                throw enrich(e);
            }
            return true;
        }));
    }

    /**
     * SmallRye loads every {@code META-INF/microprofile-config.properties} on the class path with
     * {@link java.util.Properties#load(java.io.InputStream)} but discards the URL when the parse
     * fails, producing a bare {@code Malformed \\uxxxx encoding.} with no indication of the file at
     * fault. This re-scans the same resources and appends the offending file, line and column to
     * the failure so it can actually be diagnosed.
     *
     * @param cause the original configuration build failure
     * @return the original exception when nothing could be pin-pointed, otherwise an enriched one
     */
    private static RuntimeException enrich(RuntimeException cause)
    {
        List<String> problems;
        try
        {
            problems = ConfigPropertiesValidator.validateDefaultSources();
        }
        catch (RuntimeException scanFailure)
        {
            return cause;
        }

        if (problems.isEmpty())
        {
            return cause;
        }

        StringBuilder message = new StringBuilder(String.valueOf(cause.getMessage()))
                .append(System.lineSeparator())
                .append("Invalid ")
                .append(ConfigPropertiesValidator.DEFAULT_RESOURCE)
                .append(" file(s) detected on the class path:");
        for (String problem : problems)
        {
            message.append(System.lineSeparator())
                   .append("  - ")
                   .append(problem);
        }

        log.error(message);
        return new IllegalStateException(message.toString(), cause);
    }

}
