package com.guicedee.microprofile.config;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;

/**
 * Diagnostics for MicroProfile Config property files.
 * <p>
 * SmallRye Config loads every {@code META-INF/microprofile-config.properties} it can find on the
 * class path / module path using {@link Properties#load(InputStream)}. When one of those files
 * contains an invalid {@code \\uXXXX} escape, the JDK throws
 * {@code IllegalArgumentException: Malformed \\uxxxx encoding.} — <b>without telling you which file
 * or which line</b>, because {@code io.smallrye.config.common.utils.ConfigSourceUtil#urlToMap}
 * does not decorate the exception with the URL it was reading.
 * <p>
 * This class re-walks the exact same resources and reports the offending URL, line number and
 * column so the problem can actually be fixed.
 * <p>
 * The most common causes are:
 * <ul>
 *     <li>An un-escaped Windows path, e.g. {@code base.dir=C:\\users\\dev} — {@code \\u} starts a
 *         unicode escape and {@code sers} is not valid hex. Fix with {@code C:\\\\users\\\\dev} or
 *         forward slashes.</li>
 *     <li>A regex or template containing {@code \\u} that is not followed by four hex digits.</li>
 *     <li>A truncated escape such as {@code \\u00} at the end of a line.</li>
 *     <li>The file being saved as UTF-16, which {@link Properties#load(InputStream)} reads as
 *         ISO-8859-1 and mangles.</li>
 * </ul>
 */
public final class ConfigPropertiesValidator
{
    /**
     * The resource name SmallRye's {@code addDefaultSources()} scans the whole class path for.
     */
    public static final String DEFAULT_RESOURCE = "META-INF/microprofile-config.properties";

    private ConfigPropertiesValidator()
    {
        // utility
    }

    /**
     * Scans every {@link #DEFAULT_RESOURCE} visible to the current class loaders and returns a
     * human readable problem report for each file that cannot be parsed.
     *
     * @return the list of problems, empty when every discovered file parses cleanly
     */
    public static List<String> validateDefaultSources()
    {
        return validate(DEFAULT_RESOURCE);
    }

    /**
     * Scans every occurrence of the supplied resource name and returns a problem report per file
     * that fails to parse as a {@link Properties} file.
     *
     * @param resourceName the class path relative resource name, e.g. {@link #DEFAULT_RESOURCE}
     * @return the list of problems, empty when every discovered file parses cleanly
     */
    public static List<String> validate(String resourceName)
    {
        List<String> problems = new ArrayList<>();
        for (URL url : locate(resourceName))
        {
            describeProblem(url).ifPresent(problems::add);
        }
        return problems;
    }

    /**
     * Finds every occurrence of the resource across the thread context class loader, this class'
     * class loader and the system class loader, de-duplicated and order preserved.
     *
     * @param resourceName the class path relative resource name
     * @return the discovered URLs
     */
    public static Set<URL> locate(String resourceName)
    {
        Set<URL> urls = new LinkedHashSet<>();
        List<ClassLoader> loaders = new ArrayList<>();
        loaders.add(Thread.currentThread().getContextClassLoader());
        loaders.add(ConfigPropertiesValidator.class.getClassLoader());
        loaders.add(ClassLoader.getSystemClassLoader());

        for (ClassLoader loader : loaders)
        {
            if (loader == null)
            {
                continue;
            }
            try
            {
                Enumeration<URL> found = loader.getResources(resourceName);
                while (found.hasMoreElements())
                {
                    urls.add(found.nextElement());
                }
            }
            catch (IOException e)
            {
                // a broken class path entry should never mask the real config problem
            }
        }
        return urls;
    }

    /**
     * Attempts to parse the given URL as a properties file and, on failure, produces a report that
     * names the file, the line number, the line content and a caret pointing at the bad escape.
     *
     * @param url the properties resource to inspect
     * @return the problem description, or empty when the file parses cleanly
     */
    public static Optional<String> describeProblem(URL url)
    {
        byte[] bytes;
        try (InputStream in = url.openStream())
        {
            bytes = readAll(in);
        }
        catch (IOException e)
        {
            return Optional.of(url + " -> could not be read: " + e);
        }

        // Properties.load(InputStream) reads ISO-8859-1, so mirror that exactly.
        String text = new String(bytes, StandardCharsets.ISO_8859_1);

        Optional<String> encodingProblem = detectByteOrderMark(bytes);
        if (encodingProblem.isPresent())
        {
            return Optional.of(url + " -> " + encodingProblem.get());
        }

        Properties probe = new Properties();
        try
        {
            probe.load(new java.io.ByteArrayInputStream(bytes));
            return Optional.empty();
        }
        catch (IllegalArgumentException | IOException e)
        {
            String detail = findBadEscape(text)
                    .orElse("could not pin-point the offending line; the file may use an "
                            + "unexpected character encoding");
            return Optional.of(url + " -> " + e.getMessage() + System.lineSeparator() + detail);
        }
    }

    /**
     * Walks the file the same way {@code java.util.Properties.loadConvert} does, skipping comment
     * lines and honouring escaped backslashes and line continuations, to locate the first
     * {@code \\u} that is not followed by four hexadecimal digits.
     *
     * @param text the raw ISO-8859-1 decoded file content
     * @return a formatted description of the bad escape, or empty when none was found
     */
    private static Optional<String> findBadEscape(String text)
    {
        String[] lines = text.split("\r\n|\r|\n", -1);
        boolean continued = false;

        for (int lineNo = 0; lineNo < lines.length; lineNo++)
        {
            String line = lines[lineNo];

            if (!continued)
            {
                String trimmed = line.stripLeading();
                if (trimmed.isEmpty() || trimmed.startsWith("#") || trimmed.startsWith("!"))
                {
                    continue;
                }
            }

            boolean endsWithContinuation = false;
            int i = 0;
            while (i < line.length())
            {
                if (line.charAt(i) != '\\')
                {
                    i++;
                    continue;
                }
                if (i + 1 >= line.length())
                {
                    endsWithContinuation = true;
                    break;
                }
                if (line.charAt(i + 1) == 'u')
                {
                    if (!isHex(line, i + 2, 4))
                    {
                        return Optional.of(render(lineNo + 1, line, i));
                    }
                    i += 6;
                }
                else
                {
                    // any other escape is harmless: Properties simply drops the backslash
                    i += 2;
                }
            }
            continued = endsWithContinuation;
        }
        return Optional.empty();
    }

    /**
     * Renders a compiler style pointer at the offending column.
     *
     * @param lineNo the one based line number
     * @param line   the raw line content
     * @param column the zero based index of the backslash that starts the bad escape
     * @return the formatted snippet
     */
    private static String render(int lineNo, String line, int column)
    {
        StringBuilder caret = new StringBuilder();
        for (int i = 0; i < column; i++)
        {
            caret.append(line.charAt(i) == '\t' ? '\t' : ' ');
        }
        caret.append("^-- invalid \\uXXXX escape");

        return "    at line " + lineNo + ", column " + (column + 1) + ':' + System.lineSeparator()
                + "    " + line + System.lineSeparator()
                + "    " + caret + System.lineSeparator()
                + "    Hint: escape the backslash (\\\\), use a forward slash, or complete the "
                + "escape with four hex digits.";
    }

    /**
     * Checks that {@code count} characters starting at {@code from} are hexadecimal digits.
     *
     * @param text  the text to inspect
     * @param from  the start index
     * @param count the number of digits required
     * @return true when all required digits are present and valid
     */
    private static boolean isHex(String text, int from, int count)
    {
        if (from + count > text.length())
        {
            return false;
        }
        for (int i = from; i < from + count; i++)
        {
            if (Character.digit(text.charAt(i), 16) < 0)
            {
                return false;
            }
        }
        return true;
    }

    /**
     * Detects byte order marks that indicate the file is not the ISO-8859-1 / UTF-8 subset that
     * {@link Properties#load(InputStream)} expects.
     *
     * @param bytes the raw file bytes
     * @return a description of the encoding problem, or empty when the encoding looks usable
     */
    private static Optional<String> detectByteOrderMark(byte[] bytes)
    {
        if (bytes.length >= 2)
        {
            int b0 = bytes[0] & 0xFF;
            int b1 = bytes[1] & 0xFF;
            if ((b0 == 0xFE && b1 == 0xFF) || (b0 == 0xFF && b1 == 0xFE))
            {
                return Optional.of("file starts with a UTF-16 byte order mark. "
                        + "Properties.load(InputStream) reads ISO-8859-1 — re-save the file as "
                        + "UTF-8 without a BOM.");
            }
        }
        return Optional.empty();
    }

    /**
     * Reads a stream fully into memory.
     *
     * @param in the stream to drain
     * @return the stream content
     * @throws IOException when the stream cannot be read
     */
    private static byte[] readAll(InputStream in) throws IOException
    {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buffer = new byte[8192];
        int read;
        while ((read = in.read(buffer)) != -1)
        {
            out.write(buffer, 0, read);
        }
        return out.toByteArray();
    }

    /**
     * Standalone entry point so the class path can be inspected without booting the container.
     * <p>
     * {@code java -cp <your-classpath> com.guicedee.microprofile.config.ConfigPropertiesValidator}
     *
     * @param args optional resource names to scan, defaults to {@link #DEFAULT_RESOURCE}
     */
    public static void main(String[] args)
    {
        String[] resources = args.length == 0 ? new String[]{DEFAULT_RESOURCE} : args;
        boolean failed = false;
        for (String resource : resources)
        {
            Set<URL> urls = locate(resource);
            System.out.println("Scanning " + urls.size() + " occurrence(s) of " + resource);
            for (URL url : urls)
            {
                Optional<String> problem = describeProblem(url);
                if (problem.isPresent())
                {
                    failed = true;
                    System.out.println("  [BAD] " + problem.get());
                }
                else
                {
                    System.out.println("  [ ok] " + url);
                }
            }
        }
        if (failed)
        {
            System.exit(1);
        }
    }
}

