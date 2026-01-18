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
            config = configBuilder.build();
            return true;
        }));
    }

}
