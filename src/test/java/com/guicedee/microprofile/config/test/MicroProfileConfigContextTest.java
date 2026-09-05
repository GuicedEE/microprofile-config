package com.guicedee.microprofile.config.test;

import com.google.common.base.Strings;
import com.google.inject.Injector;
import com.guicedee.client.IGuiceContext;
import com.guicedee.microprofile.config.MicroProfileConfigContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MicroProfileConfigContextTest
{
    @TempDir
    Path temporaryDirectory;

    @Test
    void loadsAllDefaultPropertySourcesThroughClassGraph() throws IOException
    {
        String originalUserDirectory = System.getProperty("user.dir");
        String originalProfile = System.getProperty("mp.config.profile");
        String originalLocations = System.getProperty("smallrye.config.locations");
        try
        {
            Path configDirectory = Files.createDirectories(temporaryDirectory.resolve("config"));
            Files.writeString(configDirectory.resolve("application.properties"), """
                    config.source.filesystem=filesystem
                    config.source.precedence=filesystem
                    """);
            System.setProperty("user.dir", temporaryDirectory.toString());
            System.setProperty("mp.config.profile", "develop");
            System.setProperty("smallrye.config.locations", "jrt:/java.base/sun/net/www/content-types.properties");

            Injector injector = IGuiceContext.getContext().inject();
            ConfigTest instance = injector.getInstance(ConfigTest.class);

            assertFalse(Strings.isNullOrEmpty(instance.getTest()));
            assertEquals("filesystem", MicroProfileConfigContext.getConfig().getValue("config.source.filesystem", String.class));
            assertEquals("application", MicroProfileConfigContext.getConfig().getValue("config.source.application", String.class));
            assertEquals("microprofile", MicroProfileConfigContext.getConfig().getValue("config.source.microprofile", String.class));
            assertEquals("application-develop", MicroProfileConfigContext.getConfig().getValue("config.source.application.profile", String.class));
            assertEquals("microprofile-develop", MicroProfileConfigContext.getConfig().getValue("config.source.microprofile.profile", String.class));
            assertEquals("filesystem", MicroProfileConfigContext.getConfig().getValue("config.source.precedence", String.class));

            assertFalse(IGuiceContext.getContext().getScanResult().getResourcesWithPath("application.properties").isEmpty());
            assertFalse(IGuiceContext.getContext().getScanResult()
                    .getResourcesWithPath("META-INF/microprofile-config.properties").isEmpty());
            assertTrue(java.util.stream.StreamSupport.stream(MicroProfileConfigContext.getConfig().getConfigSources().spliterator(), false)
                    .anyMatch(source -> source.getName().contains("jrt:/java.base/sun/net/www/content-types.properties")));
        }
        finally
        {
            restoreSystemProperty("user.dir", originalUserDirectory);
            restoreSystemProperty("mp.config.profile", originalProfile);
            restoreSystemProperty("smallrye.config.locations", originalLocations);
        }
    }

    private static void restoreSystemProperty(String name, String value)
    {
        if (value == null)
        {
            System.clearProperty(name);
        }
        else
        {
            System.setProperty(name, value);
        }
    }
}
