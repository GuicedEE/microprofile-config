package com.guicedee.microprofile.config;

import com.guicedee.guicedinjection.interfaces.IGuicePreStartup;
import com.guicedee.vertx.spi.VertXPreStartup;
import io.smallrye.config.SmallRyeConfig;
import io.smallrye.config.SmallRyeConfigBuilder;
import io.vertx.core.Future;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;

import java.util.List;

@Log4j2
@Getter
public class MicroProfileConfigContext implements IGuicePreStartup<MicroProfileConfigContext>
{
    @Getter
    private static SmallRyeConfig config;

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
