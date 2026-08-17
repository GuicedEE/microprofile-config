package com.guicedee.microprofile.config.test;

import com.guicedee.microprofile.config.ConfigPropertiesValidator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ConfigPropertiesValidatorTest
{
    @TempDir
    Path tempDirectory;

    @Test
    void reportsMalformedUnicodeEscapeWithLocation() throws IOException
    {
        Path properties = tempDirectory.resolve("malformed.properties");
        Files.writeString(properties, "valid=true\nbase.dir=C:\\users\\dev\n", StandardCharsets.ISO_8859_1);

        Optional<String> problem = ConfigPropertiesValidator.describeProblem(properties.toUri().toURL());

        assertTrue(problem.isPresent());
        assertTrue(problem.get().contains(properties.toUri().toURL().toString()));
        assertTrue(problem.get().contains("line 2, column 12"));
        assertTrue(problem.get().contains("invalid \\uXXXX escape"));
    }

    @Test
    void acceptsEscapedWindowsPath() throws IOException
    {
        Path properties = tempDirectory.resolve("valid.properties");
        Files.writeString(properties, "base.dir=C:\\\\users\\\\dev\n", StandardCharsets.ISO_8859_1);

        assertTrue(ConfigPropertiesValidator.describeProblem(properties.toUri().toURL()).isEmpty());
    }

    @Test
    void reportsUtf16ByteOrderMark() throws IOException
    {
        Path properties = tempDirectory.resolve("utf16.properties");
        Files.write(properties, "test=value\n".getBytes(StandardCharsets.UTF_16));

        Optional<String> problem = ConfigPropertiesValidator.describeProblem(properties.toUri().toURL());

        assertTrue(problem.isPresent());
        assertTrue(problem.get().contains("UTF-16 byte order mark"));
    }
}
